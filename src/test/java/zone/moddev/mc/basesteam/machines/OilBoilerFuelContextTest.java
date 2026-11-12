package zone.moddev.mc.basesteam.machines;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import net.minecraft.world.level.material.Fluid;
import org.junit.Rule;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.testing.NativeMachineFixture;
import static org.junit.Assert.*;

public class OilBoilerFuelContextTest {
    @Rule public final NativeMachineFixture machines = new NativeMachineFixture();

    @Test public void sharedBucketFuelLookupPassesThisMachineAndCannotUseAGlobalFluidCache() throws Exception {
        var method = OilBoilerTileEntity.class.getDeclaredMethod("getBurnTimePerBucketFor", Fluid.class);
        assertFalse(Modifier.isStatic(method.getModifiers()));
        ClassNode type = new ClassNode();
        try (var input = OilBoilerTileEntity.class.getResourceAsStream("OilBoilerTileEntity.class")) {
            new ClassReader(input).accept(type, 0);
        }
        assertTrue(type.fields.stream().noneMatch(field -> "Ljava/util/Map;".equals(field.desc)));
        int calls = 0;
        for (var body : type.methods) for (var instruction : body.instructions) {
            if (!(instruction instanceof MethodInsnNode call)
                    || !call.owner.equals("zone/moddev/mc/basepower/registry/FuelRegistry")
                    || !call.name.equals("getActualBurntimeForItem")) continue;
            calls++;
            assertEquals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/entity/BlockEntity;)S", call.desc);
            assertTrue(call.getPrevious() instanceof VarInsnNode);
            var receiver = (VarInsnNode)call.getPrevious();
            assertEquals(Opcodes.ALOAD, receiver.getOpcode());
            assertEquals(0, receiver.var);
        }
        assertEquals(1, calls);
    }

    @Test public void configuredPerBucketValuesRemainExactAndChangesDoNotLeakThroughAStaticCache() throws Exception {
        var previous = new HashMap<>(SteamAdvantage.fluidBurnValues);
        try {
            var method = OilBoilerTileEntity.class.getDeclaredMethod("getBurnTimePerBucketFor", Fluid.class);
            method.setAccessible(true);
            var first = NativeMachineFixture.create(OilBoilerTileEntity.class);
            var second = NativeMachineFixture.create(OilBoilerTileEntity.class);
            SteamAdvantage.fluidBurnValues.put("minecraft:lava", 12345F);
            assertEquals(12345F, (Float)method.invoke(first, net.minecraft.world.level.material.Fluids.LAVA), 0F);
            SteamAdvantage.fluidBurnValues.put("minecraft:lava", 5432F);
            assertEquals(5432F, (Float)method.invoke(first, net.minecraft.world.level.material.Fluids.LAVA), 0F);
            assertEquals(5432F, (Float)method.invoke(second, net.minecraft.world.level.material.Fluids.LAVA), 0F);
        } finally {
            SteamAdvantage.fluidBurnValues.clear();
            SteamAdvantage.fluidBurnValues.putAll(previous);
        }
    }

    @Test public void sharedFuelApiRetainsTheLegacyForgeItemOverrideForThisMachine() throws Exception {
        var id = net.minecraft.resources.Identifier.fromNamespaceAndPath("basesteam_test", "oil_bucket_override");
        var item = new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().setId(
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, id))) {
            @Override public int getBurnTime(net.minecraft.world.item.ItemStack stack,
                    net.minecraft.world.item.crafting.RecipeType<?> recipe) {
                assertSame(net.minecraft.world.item.crafting.RecipeType.SMELTING, recipe);
                return 237;
            }
        };
        net.minecraftforge.registries.ForgeRegistries.ITEMS.register(id, item);
        net.minecraft.core.registries.BuiltInRegistries.ITEM.freeze();
        zone.moddev.mc.basesteam.testing.NativeTestBootstrap.bindRegisteredItemComponents();
        var boiler = NativeMachineFixture.create(OilBoilerTileEntity.class);
        boiler.setLevel(zone.moddev.mc.basesteam.testing.NativeFuelWorld.create());
        assertEquals(237, zone.moddev.mc.basepower.registry.FuelRegistry.getActualBurntimeForItem(
                new net.minecraft.world.item.ItemStack(item), boiler));
    }
}
