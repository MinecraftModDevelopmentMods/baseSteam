package zone.moddev.mc.basesteam.resources;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.RuntimeDistCleaner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.Assert.*;

public class NativeDistCleanerContractTest {
    private java.lang.reflect.Field nativeSide;
    private Object previousSide;

    @Before public void captureSide() throws ReflectiveOperationException {
        nativeSide = RuntimeDistCleaner.class.getDeclaredField("DIST");
        nativeSide.setAccessible(true);
        previousSide = nativeSide.get(null);
    }

    @After public void restoreSide() throws IllegalAccessException {
        nativeSide.set(null, previousSide);
    }

    @Test public void nativeClientCleanerAcceptsTheThreeClientDispatchedHelpers() throws Exception {
        for (String name : List.of("gui/GUIHelper", "graphics/ClientRegistration", "graphics/DrillBitRenderer"))
            assertEquals(name, ILaunchPluginService.ComputeFlags.NO_REWRITE, clean(read(name), Dist.CLIENT));
    }

    @Test public void nativeServerCleanerAcceptsCommonMenusAndMachineAccessors() throws Exception {
        for (String name : List.of("BlastFurnace", "CoalBoiler", "ElectricBoiler", "GeothermalBoiler",
                "OilBoiler", "RockCrusher", "SteamDrill", "SteamPump", "SteamStill", "SteamTank")) {
            ClassNode menu = read("gui/" + name + "GUI");
            assertEquals(name, ILaunchPluginService.ComputeFlags.NO_REWRITE, clean(menu, Dist.DEDICATED_SERVER));
            for (var field : menu.fields)
                assertFalse(name + " field " + field.name, field.desc.contains("net/minecraft/client/") || field.desc.contains("GUIContainer"));
        }
        assertEquals(ILaunchPluginService.ComputeFlags.NO_REWRITE,
                clean(read("machines/BlastFurnaceTileEntity"), Dist.DEDICATED_SERVER));
    }

    @Test public void matchingClientTypeAnnotationIsRejectedByTheActualNativeCleaner() {
        ClassNode node = fixture();
        node.visibleAnnotations = new ArrayList<>(List.of(clientMarker()));
        assertThrows(UnsupportedOperationException.class, () -> clean(node, Dist.CLIENT));
    }

    @Test public void nativeServerRejectsUnsupportedClientMethodMarkersInsteadOfStrippingThem() {
        ClassNode node = fixture();
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "clientOnly", "()V", null, null);
        method.visibleAnnotations = new ArrayList<>(List.of(clientMarker()));
        node.methods.add(method);
        assertThrows(UnsupportedOperationException.class, () -> clean(node, Dist.DEDICATED_SERVER));
    }

    private static int clean(ClassNode node, Dist side) {
        RuntimeDistCleaner cleaner = new RuntimeDistCleaner();
        cleaner.getExtension().accept(side);
        return cleaner.processClassWithFlags(ILaunchPluginService.Phase.AFTER, node,
                Type.getObjectType(node.name), "baseSteam native side contract");
    }

    private static AnnotationNode clientMarker() {
        AnnotationNode marker = new AnnotationNode(Type.getDescriptor(OnlyIn.class));
        marker.values = new ArrayList<>(List.of("value", new String[] {Type.getDescriptor(Dist.class), "CLIENT"}));
        return marker;
    }

    private static ClassNode fixture() {
        ClassNode node = new ClassNode();
        node.version = 69;
        node.access = Opcodes.ACC_PUBLIC;
        node.name = "zone/moddev/mc/basesteam/testing/UnsupportedMarkerFixture";
        node.superName = "java/lang/Object";
        return node;
    }

    private static ClassNode read(String name) throws Exception {
        try (InputStream input = NativeDistCleanerContractTest.class.getResourceAsStream(
                "/zone/moddev/mc/basesteam/" + name + ".class")) {
            assertNotNull(name, input);
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, ClassReader.SKIP_CODE);
            return node;
        }
    }
}
