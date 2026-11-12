package zone.moddev.mc.basesteam.testing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import static org.junit.Assert.*;

public class EclipseTestInventoryTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void includesEveryAnnotatedClassWithoutPackageOrFilenameFilters() throws Exception {
        Path classes = temporary.newFolder("classes").toPath();
        Path results = temporary.newFolder("results").toPath();
        type(classes, "zone/moddev/mc/basesteam/NormalTest", "java/lang/Object", null, false, "one");
        type(classes, "outside/prefix/UnusualSpec", "java/lang/Object", null, false, "first", "second");
        xml(results, "zone.moddev.mc.basesteam.NormalTest", 0, "one");
        xml(results, "outside.prefix.UnusualSpec", 0, "first", "second");
        var inventory = EclipseTestInventory.verify(classes, results);
        assertEquals(2, inventory.suites().size());
        assertEquals(3, inventory.tests());
    }

    @Test public void rejectsPrefixFilteredAndPartialMethodResults() throws Exception {
        Path classes = temporary.newFolder("classes").toPath();
        Path results = temporary.newFolder("results").toPath();
        type(classes, "owned/NormalTest", "java/lang/Object", null, false, "one");
        type(classes, "outside/UnusualSpec", "java/lang/Object", null, false, "first", "second");
        xml(results, "owned.NormalTest", 0, "one");
        assertThrows(AssertionError.class, () -> EclipseTestInventory.verify(classes, results));
        xml(results, "outside.UnusualSpec", 0, "first");
        assertThrows(AssertionError.class, () -> EclipseTestInventory.verify(classes, results));
        xml(results, "outside.UnusualSpec", 0, "first", "second");
        assertEquals(3, EclipseTestInventory.verify(classes, results).tests());
    }

    @Test public void rejectsStaleSuitesAndSameCountWrongMethods() throws Exception {
        Path classes = temporary.newFolder("classes").toPath();
        Path results = temporary.newFolder("results").toPath();
        type(classes, "owned/Example", "java/lang/Object", null, false, "actual");
        xml(results, "owned.Stale", 0, "actual");
        assertThrows(AssertionError.class, () -> EclipseTestInventory.verify(classes, results));
        Files.delete(results.resolve("TEST-owned.Stale.xml"));
        xml(results, "owned.Example", 0, "wrong");
        assertThrows(AssertionError.class, () -> EclipseTestInventory.verify(classes, results));
    }

    @Test public void rejectsSkippedFailedAndEmptyResults() throws Exception {
        Path classes = temporary.newFolder("classes").toPath();
        Path results = temporary.newFolder("results").toPath();
        type(classes, "owned/Example", "java/lang/Object", null, false, "actual");
        assertThrows(AssertionError.class, () -> EclipseTestInventory.verify(classes, results));
        xml(results, "owned.Example", 1, "actual");
        assertThrows(AssertionError.class, () -> EclipseTestInventory.verify(classes, results));
        Path suite = results.resolve("TEST-owned.Example.xml");
        Files.writeString(suite, Files.readString(suite).replace("skipped=\"1\"", "skipped=\"0\"")
                .replace("failures=\"0\"", "failures=\"1\""));
        assertThrows(AssertionError.class, () -> EclipseTestInventory.verify(classes, results));
    }

    @Test public void rejectsCustomNestedAndInheritedDiscovery() throws Exception {
        int index = 0;
        for (String[] fixture : List.of(
                new String[]{"owned/Custom", "java/lang/Object", "Lorg/junit/runner/RunWith;"},
                new String[]{"owned/Outer$Nested", "java/lang/Object", ""},
                new String[]{"owned/Inherited", "owned/Parent", ""},
                new String[]{"owned/OldStyle", "junit/framework/TestCase", ""})) {
            Path classes = temporary.newFolder("classes" + index).toPath();
            Path results = temporary.newFolder("results" + index++).toPath();
            type(classes, fixture[0], fixture[1], fixture[2].isEmpty() ? null : fixture[2], false, "one");
            xml(results, fixture[0].replace('/', '.'), 0, "one");
            assertThrows(AssertionError.class, () -> EclipseTestInventory.verify(classes, results));
        }
    }

    @Test public void rejectsIgnoredMethodsAndIgnoredClasses() throws Exception {
        for (boolean classIgnore : List.of(false, true)) {
            Path classes = temporary.newFolder("classes" + classIgnore).toPath();
            Path results = temporary.newFolder("results" + classIgnore).toPath();
            type(classes, "owned/Ignored", "java/lang/Object",
                    classIgnore ? "Lorg/junit/Ignore;" : null, !classIgnore, "one");
            xml(results, "owned.Ignored", 0, "one");
            assertThrows(AssertionError.class, () -> EclipseTestInventory.verify(classes, results));
        }
    }

    private static void type(Path root, String name, String parent, String annotation, boolean ignoreMethod,
                             String... methods) throws Exception {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V25, Opcodes.ACC_PUBLIC, name, null, parent, null);
        if (annotation != null) writer.visitAnnotation(annotation, true).visitEnd();
        for (String nameOfMethod : methods) {
            var method = writer.visitMethod(Opcodes.ACC_PUBLIC, nameOfMethod, "()V", null, null);
            method.visitAnnotation("Lorg/junit/Test;", true).visitEnd();
            if (ignoreMethod) method.visitAnnotation("Lorg/junit/Ignore;", true).visitEnd();
            method.visitCode();
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(0, 1);
            method.visitEnd();
        }
        writer.visitEnd();
        Path file = root.resolve(name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    private static void xml(Path root, String name, int skipped, String... methods) throws Exception {
        StringBuilder xml = new StringBuilder("<testsuite name=\"").append(name).append("\" tests=\"")
                .append(methods.length).append("\" failures=\"0\" errors=\"0\" skipped=\"").append(skipped).append("\">");
        for (String method : methods) xml.append("<testcase classname=\"").append(name)
                .append("\" name=\"").append(method).append("\"/>");
        Files.writeString(root.resolve("TEST-" + name + ".xml"), xml.append("</testsuite>").toString());
    }
}
