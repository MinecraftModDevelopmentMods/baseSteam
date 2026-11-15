package zone.moddev.mc.basesteam.testing;

import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Detached fixtures run through ModDevGradle's native, transformed JUnit launch. */
public final class NativeTestBootstrap {
    private NativeTestBootstrap() {}

    public static void start() {
        NativeRegistries.ensureStarted();
        BuiltInRegistries.REGISTRY.forEach(registry -> ((MappedRegistry<?>) registry).unfreeze());
    }

    public static RegistryAccess.Frozen registries() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    public static synchronized void registerVanillaFluidTypes() throws ReflectiveOperationException {
        start();
        if (NeoForgeRegistries.FLUID_TYPES.containsKey(ResourceLocation.parse("minecraft:lava"))) return;
        ((MappedRegistry<?>) NeoForgeRegistries.FLUID_TYPES).unfreeze();
        // Plain fixture tests do not dispatch the NeoForge mod's fluid-type registry event.
        var field = NeoForgeMod.class.getDeclaredField("VANILLA_FLUID_TYPES");
        field.setAccessible(true);
        var constructor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, Registry.class);
        constructor.setAccessible(true);
        var register = DeferredRegister.class.getDeclaredMethod("addEntries", RegisterEvent.class);
        register.setAccessible(true);
        register.invoke(field.get(null), constructor.newInstance(
                NeoForgeRegistries.Keys.FLUID_TYPES, NeoForgeRegistries.FLUID_TYPES));
    }

    private static final class NativeRegistries {
        static {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }
        private static void ensureStarted() {}
    }
}
