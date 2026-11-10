package zone.moddev.mc.basesteam.testing;

import java.util.List;
import java.util.Optional;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
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
            var registries = ForgeRegistries.class.getDeclaredField("registries");
            registries.setAccessible(true);
            DeferredRegister<?> fluidTypes = ((List<?>) registries.get(null)).stream()
                    .map(DeferredRegister.class::cast)
                    .filter(registry -> ForgeRegistries.Keys.FLUID_TYPES.equals(registry.getRegistryKey()))
                    .findFirst().orElseThrow();
            var event = new NewRegistryEvent();
            invokeRegistryPhase(fluidTypes, "createRegistry", event);
            var fill = NewRegistryEvent.class.getDeclaredMethod("fill");
            fill.setAccessible(true);
            fill.invoke(event);
        }

        var vanillaTypes = ForgeMod.class.getDeclaredField("VANILLA_FLUID_TYPES");
        vanillaTypes.setAccessible(true);
        var event = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        event.setAccessible(true);
        invokeRegistryPhase((DeferredRegister<?>) vanillaTypes.get(null), "handleEvent",
                event.newInstance(ForgeRegistries.Keys.FLUID_TYPES, ForgeRegistries.FLUID_TYPES.get(), null));
    }

    private static void invokeRegistryPhase(DeferredRegister<?> register, String phase, Event event)
            throws ReflectiveOperationException {
        // The launch-only EventBus transform is not present in plain JUnit.
        Class<?> dispatcher = Class.forName(DeferredRegister.class.getName() + "$EventDispatcher");
        var constructor = dispatcher.getDeclaredConstructor(DeferredRegister.class);
        constructor.setAccessible(true);
        var method = dispatcher.getDeclaredMethod(phase, event.getClass());
        method.setAccessible(true);
        method.invoke(constructor.newInstance(register), event);
    }

    private static final class NativeRegistries {
        static {
            initializeEmptyForgeDiscovery();
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }

        private static void ensureStarted() {}

        private static void initializeEmptyForgeDiscovery() {
            if (FMLLoader.getLoadingModList() != null) return;
            // Forge 50 lazily gathers item components through ForgeEventFactory.
            // Plain JUnit has no launch discovery, so give the native loader an empty list.
            var list = LoadingModList.of(List.of(), List.of(), null);
            list.setBrokenFiles(List.of());
            try {
                var field = FMLLoader.class.getDeclaredField("loadingModList");
                field.setAccessible(true);
                field.set(null, list);
                var layers = FMLLoader.class.getDeclaredField("moduleLayerManager");
                layers.setAccessible(true);
                layers.set(null, (IModuleLayerManager) layer -> layer == IModuleLayerManager.Layer.GAME
                        ? Optional.of(ModuleLayer.empty()) : Optional.empty());
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not initialize the Forge 50 unit fixture", exception);
            }
        }
    }
}
