package zone.moddev.mc.basesteam.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

/** Forge-aware access to the vanilla furnace fuel calculation. */
public final class FurnaceFuelHelper {
    private FurnaceFuelHelper() {}

    public static int getBurnTime(ItemStack stack) {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        return server == null || server.overworld() == null
                ? resolve(stack, null) : getBurnTime(stack, server.overworld());
    }

    public static int getBurnTime(ItemStack stack, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return getBurnTime(stack);
        var furnace = new FurnaceBlockEntity(BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        furnace.setLevel(serverLevel);
        return getBurnTime(stack, serverLevel, furnace);
    }

    public static int getBurnTime(ItemStack stack, BlockEntity machine) {
        if (!(machine.getLevel() instanceof ServerLevel level)) return getBurnTime(stack);
        return getBurnTime(stack, level, machine);
    }

    public static boolean isFuel(ItemStack stack, BlockEntity machine) {
        if (stack.isEmpty()) return false;
        if (machine.getLevel() instanceof ServerLevel) return getBurnTime(stack, machine) > 0;
        // Client slots cannot resolve server loot contexts; mirror the native furnace slot policy.
        return stack.has(DataComponents.COOKING_FUEL) || stack.getBurnTime(null) > 0
                || ForgeEventFactory.getItemBurnTime(stack, 0, null) > 0;
    }

    private static int getBurnTime(ItemStack stack, ServerLevel level, BlockEntity machine) {
        if (stack.isEmpty()) return 0;
        if (!(machine instanceof Container container)) {
            throw new IllegalArgumentException("Cooking fuel requires the actual machine container");
        }
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.BLOCK_STATE, machine.getBlockState())
                .withParameter(LootContextParams.BLOCK_ENTITY, machine)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(machine.getBlockPos()))
                .withParameter(LootContextParams.CONTAINER, container)
                .create(LootContextParamSets.CONTAINER_PROCESS);
        return resolve(stack, new LootContext.Builder(params).create(Optional.empty()));
    }

    private static int resolve(ItemStack stack, LootContext context) {
        if (stack.isEmpty()) return 0;
        int burnTime = stack.getBurnTime(null);
        if (burnTime < 0) {
            CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
            if (fuel == null) burnTime = 0;
            else if (fuel.burnTime() instanceof ResolvableInt.Constant constant) burnTime = constant.value();
            else {
                if (context == null) {
                    throw new IllegalStateException("Contextual cooking fuel requires a server world");
                }
                burnTime = fuel.burnTime().get(context, 0);
            }
        }
        // Retain the Forge item override before event overrides, as FuelValues did.
        return ForgeEventFactory.getItemBurnTime(stack, burnTime, null);
    }
}
