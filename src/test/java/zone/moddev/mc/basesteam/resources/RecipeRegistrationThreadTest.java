package zone.moddev.mc.basesteam.resources;

import java.io.InputStream;
import java.util.Arrays;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.Assert.*;

public class RecipeRegistrationThreadTest {
    private static final String CONDITIONS = "zone/moddev/mc/basesteam/recipe/RecipeConditions";

    @Test public void codecRegistrationUsesTheForgeModEventBusExactlyOnce() throws Exception {
        ClassNode mod = read("zone/moddev/mc/basesteam/SteamAdvantage");
        assertEquals(1L, mod.methods.stream().filter(method -> method.name.equals("<init>"))
                .flatMap(RecipeRegistrationThreadTest::calls)
                .filter(call -> call.owner.equals(CONDITIONS) && call.name.equals("register")
                        && call.desc.equals("(Lnet/minecraftforge/eventbus/api/bus/BusGroup;)V"))
                .count());
        assertFalse(mod.methods.stream().filter(method -> method.name.equals("commonSetup"))
                .flatMap(RecipeRegistrationThreadTest::calls).anyMatch(call -> call.owner.equals(CONDITIONS)));
        ClassNode conditions = read(CONDITIONS);
        assertTrue(conditions.methods.stream().filter(method -> method.name.equals("register"))
                .flatMap(RecipeRegistrationThreadTest::calls)
                .anyMatch(call -> call.owner.equals("net/minecraftforge/registries/DeferredRegister")
                        && call.name.equals("register")));
    }

    private static java.util.stream.Stream<MethodInsnNode> calls(MethodNode method) {
        return Arrays.stream(method.instructions.toArray()).filter(MethodInsnNode.class::isInstance)
                .map(MethodInsnNode.class::cast);
    }

    private static ClassNode read(String name) throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = RecipeRegistrationThreadTest.class.getResourceAsStream("/" + name + ".class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        return node;
    }
}
