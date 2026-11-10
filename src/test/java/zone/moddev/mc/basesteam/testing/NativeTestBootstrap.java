package zone.moddev.mc.basesteam.testing;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;

public final class NativeTestBootstrap {
    private NativeTestBootstrap() {}

    public static void start() {
        NativeRegistries.ensureStarted();
        // Standalone fixtures construct blocks and items outside Forge's mod registration phase.
        GameData.unfreezeData();
    }

    public static RegistryAccess.Frozen registries() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    public static synchronized void registerVanillaFluidTypes() throws ReflectiveOperationException {
        start();
        if (ForgeMod.LAVA_TYPE.isPresent()) return;

        // Plain JUnit has no mod loader to dispatch these two Forge registry phases.
        if (ForgeRegistries.FLUID_TYPES.get() == null) {
            var event = new NewRegistryEvent();
            var fluidTypes = ForgeRegistries.class.getDeclaredField("DEFERRED_FLUID_TYPES");
            fluidTypes.setAccessible(true);
            var create = DeferredRegister.class.getDeclaredMethod("createRegistry", NewRegistryEvent.class);
            create.setAccessible(true);
            create.invoke(fluidTypes.get(null), event);
            var fill = NewRegistryEvent.class.getDeclaredMethod("fill");
            fill.setAccessible(true);
            fill.invoke(event);
        }

        var vanillaTypes = ForgeMod.class.getDeclaredField("VANILLA_FLUID_TYPES");
        vanillaTypes.setAccessible(true);
        var addEntries = DeferredRegister.class.getDeclaredMethod("addEntries", RegisterEvent.class);
        addEntries.setAccessible(true);
        var event = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        event.setAccessible(true);
        addEntries.invoke(vanillaTypes.get(null), event.newInstance(
                ForgeRegistries.Keys.FLUID_TYPES, ForgeRegistries.FLUID_TYPES.get(), null));
    }

    private static final class NativeRegistries {
        static {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }

        private static void ensureStarted() {}
    }
}
