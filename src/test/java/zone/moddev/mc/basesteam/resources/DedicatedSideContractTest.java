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

    @Test public void machineMenusRetainLegacyClientCallbacksWithoutUnsupportedSideMarkers() throws Exception {
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
                assertFalse(name + "#" + method.name + " must not rely on unsupported mod-side stripping",
                        clientOnly(method.visibleAnnotations) || clientOnly(method.invisibleAnnotations));
            }
        }
    }

    @Test public void renderingHelpersRemainBehindTheActualClientDistributionGuard() throws Exception {
        for (String name : List.of("gui/GUIHelper", "graphics/ClientRegistration", "graphics/DrillBitRenderer")) {
            ClassNode node = read(name);
            assertFalse(name, clientOnly(node.visibleAnnotations) || clientOnly(node.invisibleAnnotations));
        }
        var constructor = read("SteamAdvantage").methods.stream()
                .filter(method -> method.name.equals("<init>")).findFirst().orElseThrow();
        var instructions = java.util.Arrays.asList(constructor.instructions.toArray());
        int call = -1;
        int guard = -1;
        boolean literalClient = false;
        for (int index = 0; index < instructions.size(); index++) {
            var instruction = instructions.get(index);
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode field) {
                if (field.owner.equals("net/minecraftforge/fml/loading/FMLEnvironment") && field.name.equals("dist"))
                    guard = index;
                if (field.owner.equals("net/minecraftforge/api/distmarker/Dist") && field.name.equals("CLIENT"))
                    literalClient = true;
            }
            if (instruction instanceof org.objectweb.asm.tree.MethodInsnNode method
                    && method.owner.equals("zone/moddev/mc/basesteam/graphics/ClientRegistration")
                    && method.name.equals("register")) call = index;
        }
        assertTrue("Client registration must follow the native distribution read", guard >= 0 && guard < call && literalClient);
        boolean guarded = false;
        for (int index = guard + 1; index < call; index++) {
            if (instructions.get(index) instanceof org.objectweb.asm.tree.JumpInsnNode jump
                    && jump.getOpcode() == org.objectweb.asm.Opcodes.IF_ACMPNE
                    && instructions.indexOf(jump.label) > call) guarded = true;
        }
        assertTrue("The non-client branch must skip the renderer registration call", guarded);
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
            new ClassReader(input).accept(node, 0);
            return node;
        }
    }
}
