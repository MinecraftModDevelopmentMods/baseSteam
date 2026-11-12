package zone.moddev.mc.basesteam.testing;

import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

/** A detached world with the real vanilla cooking providers, not a fuel-value stub. */
public final class NativeFuelWorld extends ServerLevel {
    private FuelServer server;
    private RandomSource fuelRandom;
    public int dirtyNotifications;
    public int comparatorNotifications;

    private NativeFuelWorld() {
        super(null, null, null, null, null, null, false, 0L, List.of(), false);
    }

    public static NativeFuelWorld create() throws ReflectiveOperationException {
        var world = allocate(NativeFuelWorld.class);
        world.server = allocate(FuelServer.class);
        world.server.registries = new ReloadableServerRegistries.Holder(
                VanillaRegistries.createReloadableLookup(VanillaRegistries.createWorldLookup()));
        world.fuelRandom = RandomSource.create(0L);
        return world;
    }

    @Override public MinecraftServer getServer() { return server; }
    @Override public RandomSource getRandom() { return fuelRandom; }
    @Override public void blockEntityChanged(net.minecraft.core.BlockPos pos) { dirtyNotifications++; }
    @Override public void updateNeighbourForOutputSignal(net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.block.Block block) { comparatorNotifications++; }

    private static <T> T allocate(Class<T> type) throws ReflectiveOperationException {
        Class<?> allocator = Class.forName("sun.misc.Unsafe");
        Field field = allocator.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(allocator.getMethod("allocateInstance", Class.class).invoke(field.get(null), type));
    }

    private static final class FuelServer extends DedicatedServer {
        private ReloadableServerRegistries.Holder registries;

        private FuelServer() { super(null, null, null, null, null, null, null, null, null, null); }
        @Override public ReloadableServerRegistries.Holder reloadableRegistries() { return registries; }
    }
}
