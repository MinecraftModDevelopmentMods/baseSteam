package zone.moddev.mc.basesteam.testing;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.w3c.dom.Element;

/** Compares all fresh compiled JUnit methods with the complete Gradle XML inventory. */
public final class EclipseTestInventory {
    private static final Set<String> REVIEWED_PARAMETERIZED = Set.of(
            "zone.moddev.mc.basesteam.machines.WaterBoilerFluidBoundaryTest",
            "zone.moddev.mc.basesteam.machines.WaterBoilerTankNotificationTest",
            "zone.moddev.mc.basesteam.resources.NativeBlockPropertiesTest");
    private EclipseTestInventory() {}

    public record Inventory(Map<String, Set<String>> suites, int tests, Set<String> reviewedParameterized) {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 3) throw new IllegalArgumentException("classes, Gradle XML directory, receipt");
        Inventory inventory = verify(Path.of(arguments[0]), Path.of(arguments[1]));
        Files.writeString(Path.of(arguments[2]), new GsonBuilder().setPrettyPrinting().create().toJson(inventory) + "\n");
        System.out.println("ECJ_COMPLETE_INVENTORY suites=" + inventory.suites().size() + " tests=" + inventory.tests());
    }

    public static Inventory verify(Path classes, Path results) throws Exception {
        Map<String, Set<String>> compiled = compiledTests(classes);
        Map<String, Set<String>> reported = reportedTests(results);
        if (!compiled.equals(reported)) {
            throw new AssertionError("Incomplete or stale Gradle inventory: compiled=" + compiled + ", reported=" + reported);
        }
        Set<String> reviewed = new TreeSet<>(compiled.keySet());
        reviewed.retainAll(REVIEWED_PARAMETERIZED);
        return new Inventory(compiled, compiled.values().stream().mapToInt(Set::size).sum(), reviewed);
    }

    private static Map<String, Set<String>> compiledTests(Path root) throws Exception {
        Map<String, Set<String>> suites = new TreeMap<>();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".class")).sorted().toList()) {
                ClassNode type = new ClassNode();
                new ClassReader(Files.readAllBytes(path)).accept(type,
                        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                String name = type.name.replace('/', '.');
                boolean parameterized = false;
                if (has(type.visibleAnnotations, "Lorg/junit/runner/RunWith;")) {
                    AnnotationNode runner = type.visibleAnnotations.stream()
                            .filter(annotation -> "Lorg/junit/runner/RunWith;".equals(annotation.desc)).findFirst().orElseThrow();
                    parameterized = REVIEWED_PARAMETERIZED.contains(name) && runner.values != null
                            && runner.values.size() == 2 && "value".equals(runner.values.get(0))
                            && org.objectweb.asm.Type.getType("Lorg/junit/runners/Parameterized;").equals(runner.values.get(1));
                    if (!parameterized) throw new AssertionError("Review custom JUnit discovery: " + type.name);
                }
                if ("junit/framework/TestCase".equals(type.superName)) {
                    throw new AssertionError("Review custom JUnit discovery: " + type.name);
                }
                Set<String> methods = new TreeSet<>();
                for (var method : type.methods) {
                    if (!has(method.visibleAnnotations, "Lorg/junit/Test;")) continue;
                    if (has(type.visibleAnnotations, "Lorg/junit/Ignore;")
                            || has(method.visibleAnnotations, "Lorg/junit/Ignore;")
                            || (method.access & Opcodes.ACC_PUBLIC) == 0
                            || (method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT)) != 0
                            || !"()V".equals(method.desc) || !methods.add(method.name)) {
                        throw new AssertionError("Ignored or unsupported JUnit method: " + type.name + "." + method.name);
                    }
                }
                if (methods.isEmpty()) continue;
                if (type.name.contains("$") || (type.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE)) != 0
                        || !"java/lang/Object".equals(type.superName)) {
                    throw new AssertionError("Review nested or inherited JUnit discovery: " + type.name);
                }
                if (parameterized) {
                    Set<String> expanded = new TreeSet<>();
                    collectParameterized(Request.aClass(Class.forName(name)).getRunner().getDescription(), name, expanded);
                    for (String method : methods) {
                        if (expanded.stream().noneMatch(test -> test.startsWith(method + "["))) {
                            throw new AssertionError("Missing parameterized method: " + name + "." + method);
                        }
                    }
                    for (String test : expanded) {
                        int bracket = test.indexOf('[');
                        if (bracket < 0 || !methods.contains(test.substring(0, bracket))) {
                            throw new AssertionError("Unexpected parameterized method: " + name + "." + test);
                        }
                    }
                    methods = expanded;
                }
                if (suites.put(name, methods) != null) {
                    throw new AssertionError("Duplicate compiled test class: " + type.name);
                }
            }
        }
        if (suites.isEmpty()) throw new AssertionError("Empty compiled JUnit inventory");
        return suites;
    }

    private static void collectParameterized(Description description, String type, Set<String> methods) {
        if (description.isTest()) {
            if (!type.equals(description.getClassName()) || description.getMethodName() == null
                    || !methods.add(description.getMethodName())) {
                throw new AssertionError("Invalid parameterized description: " + description);
            }
        } else {
            description.getChildren().forEach(child -> collectParameterized(child, type, methods));
        }
    }

    private static Map<String, Set<String>> reportedTests(Path root) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Map<String, Set<String>> suites = new TreeMap<>();
        try (var paths = Files.list(root)) {
            for (Path path : paths.filter(file -> file.getFileName().toString().startsWith("TEST-")
                    && file.toString().endsWith(".xml")).sorted().toList()) {
                Element suite = factory.newDocumentBuilder().parse(path.toFile()).getDocumentElement();
                String name = suite.getAttribute("name");
                if (!"testsuite".equals(suite.getTagName()) || name.isEmpty()
                        || !path.getFileName().toString().equals("TEST-" + name + ".xml")) {
                    throw new AssertionError("Invalid Gradle test suite: " + path);
                }
                for (String attribute : List.of("failures", "errors", "skipped")) {
                    if (!"0".equals(suite.getAttribute(attribute))) {
                        throw new AssertionError("Non-green Gradle inventory: " + path + " " + attribute);
                    }
                }
                Set<String> methods = new TreeSet<>();
                var cases = suite.getElementsByTagName("testcase");
                for (int index = 0; index < cases.getLength(); index++) {
                    Element test = (Element) cases.item(index);
                    String method = test.getAttribute("name");
                    if (!name.equals(test.getAttribute("classname")) || method.isEmpty()
                            || !methods.add(method) || test.getElementsByTagName("skipped").getLength() != 0
                            || test.getElementsByTagName("failure").getLength() != 0
                            || test.getElementsByTagName("error").getLength() != 0) {
                        throw new AssertionError("Invalid Gradle test case: " + path + " " + method);
                    }
                }
                if (methods.isEmpty() || methods.size() != Integer.parseInt(suite.getAttribute("tests"))
                        || suites.put(name, methods) != null) {
                    throw new AssertionError("Duplicate or inconsistent Gradle suite: " + path);
                }
            }
        }
        if (suites.isEmpty()) throw new AssertionError("Empty Gradle JUnit inventory");
        return suites;
    }

    private static boolean has(List<AnnotationNode> annotations, String descriptor) {
        return annotations != null && annotations.stream().anyMatch(annotation -> descriptor.equals(annotation.desc));
    }
}
