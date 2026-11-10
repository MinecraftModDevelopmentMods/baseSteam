package zone.moddev.mc.basesteam.resources;

import java.io.InputStream;
import java.util.List;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import static org.junit.Assert.*;

public class DedicatedSideContractTest {
    private static final String ROOT = "/zone/moddev/mc/basesteam/";
    private static final String ONLY_IN = "Lnet/minecraftforge/api/distmarker/OnlyIn;";

    @Test public void machineMenusStayCommonAndDecorationMethodsStayClientOnly() throws Exception {
        for (String name : List.of("BlastFurnace", "CoalBoiler", "ElectricBoiler", "GeothermalBoiler",
                "OilBoiler", "RockCrusher", "SteamDrill", "SteamPump", "SteamStill", "SteamTank")) {
            ClassNode menu = read("gui/" + name + "GUI");
            assertFalse("Common menu must remain loadable on the server: " + name,
                    clientOnly(menu.visibleAnnotations) || clientOnly(menu.invisibleAnnotations));
            var clientSignatures = menu.methods.stream()
                    .filter(method -> method.desc.contains("GUIContainer") || method.desc.contains("net/minecraft/client/"))
                    .toList();
            assertFalse("Expected at least the decoration override: " + name, clientSignatures.isEmpty());
            for (var method : clientSignatures) {
                assertTrue(name + "#" + method.name + " leaks a client class on the server",
                        clientOnly(method.visibleAnnotations) || clientOnly(method.invisibleAnnotations));
            }
        }
    }

    @Test public void renderingHelpersAndRegistrationAreEntirelyClientOnly() throws Exception {
        for (String name : List.of("gui/GUIHelper", "graphics/ClientRegistration", "graphics/DrillBitRenderer")) {
            ClassNode node = read(name);
            assertTrue(name, clientOnly(node.visibleAnnotations) || clientOnly(node.invisibleAnnotations));
        }
    }

    private static boolean clientOnly(List<AnnotationNode> annotations) {
        return annotations != null && annotations.stream().anyMatch(annotation -> ONLY_IN.equals(annotation.desc)
                && annotation.values != null && annotation.values.stream()
                        .anyMatch(value -> value instanceof String[] marker && marker[1].equals("CLIENT")));
    }

    private static ClassNode read(String path) throws Exception {
        try (InputStream input = DedicatedSideContractTest.class.getResourceAsStream(ROOT + path + ".class")) {
            assertNotNull(path, input);
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, ClassReader.SKIP_CODE);
            return node;
        }
    }
}
