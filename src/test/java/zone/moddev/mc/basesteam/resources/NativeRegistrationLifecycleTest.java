package zone.moddev.mc.basesteam.resources;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.Assert.*;

public class NativeRegistrationLifecycleTest {
    private static final String ROOT = "zone/moddev/mc/basesteam/";
    private static final String INIT = ROOT + "init/";

    @Test public void modConstructionDoesNotInitializeRegistriesOrGuis() throws Exception {
        assertFalse(calls("SteamAdvantage", "<init>").stream().anyMatch(call -> call.owner.startsWith(INIT)));
    }

    @Test public void owningRegisterEventsInitializeBlocksItemsAndEnchantments() throws Exception {
        assertTrue(calls("init/Blocks", "registerBlocks").stream().anyMatch(call -> initializes(call, "Blocks")));
        assertTrue(calls("init/Items", "register").stream().anyMatch(call -> initializes(call, "Items")));
        assertTrue(calls("init/Enchantments", "registerEnchantments").stream()
                .anyMatch(call -> initializes(call, "Enchantments")));
        assertFalse(calls("init/Blocks", "registerBlockItems").stream().anyMatch(call -> initializes(call, "Blocks")));
        assertFalse(calls("init/Items", "init").stream().anyMatch(call -> initializes(call, "Blocks")));
        assertFalse(calls("init/Recipes", "init").stream()
                .anyMatch(call -> initializes(call, "Blocks") || initializes(call, "Items")));
    }

    @Test public void commonSetupQueuesGuiDefinitions() throws Exception {
        var instructions = method("SteamAdvantage", "commonSetup").instructions.toArray();
        for (int i = 0; i + 1 < instructions.length; i++) {
            if (!(instructions[i] instanceof InvokeDynamicInsnNode runnable)) continue;
            if (Arrays.stream(runnable.bsmArgs).anyMatch(arg -> arg instanceof Handle handle
                    && handle.getOwner().equals(INIT + "GUI") && handle.getName().equals("init"))) {
                assertTrue(instructions[i + 1] instanceof MethodInsnNode call && call.name.equals("enqueueWork"));
                return;
            }
        }
        fail("Expected GUI initialization on Forge's common setup work queue");
    }

    private static boolean initializes(MethodInsnNode call, String type) {
        return call.owner.equals(INIT + type) && call.name.equals("init");
    }

    private static List<MethodInsnNode> calls(String type, String name) throws Exception {
        return Arrays.stream(method(type, name).instructions.toArray())
                .filter(MethodInsnNode.class::isInstance).map(MethodInsnNode.class::cast).toList();
    }

    private static MethodNode method(String type, String name) throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream stream = NativeRegistrationLifecycleTest.class.getResourceAsStream("/" + ROOT + type + ".class")) {
            assertNotNull(stream);
            new ClassReader(stream).accept(node, 0);
        }
        return node.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow();
    }
}
