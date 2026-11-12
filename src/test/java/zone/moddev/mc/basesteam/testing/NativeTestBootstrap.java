package zone.moddev.mc.basesteam.testing;

import java.util.List;
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

    public static void bindRegisteredItemComponents() {
        var world = net.minecraft.data.registries.VanillaRegistries.createWorldLookup();
        var registries = net.minecraft.data.registries.VanillaRegistries.createReloadableLookup(world);
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries)
                .forEach(net.minecraft.core.component.DataComponentInitializers.PendingComponents::apply);
    }

    public static synchronized void registerVanillaFluidTypes() throws ReflectiveOperationException {
        start();
        if (ForgeMod.LAVA_TYPE.isPresent()) return;

        // Plain JUnit has no mod loader to dispatch these two Forge registry phases.
        if (ForgeRegistries.FLUID_TYPES.get() == null) {
            createForgeRegistry(ForgeRegistries.Keys.FLUID_TYPES);
        }

        var vanillaTypes = ForgeMod.class.getDeclaredField("VANILLA_FLUID_TYPES");
        vanillaTypes.setAccessible(true);
        var event = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        event.setAccessible(true);
        invokeRegistryPhase((DeferredRegister<?>) vanillaTypes.get(null), "handleEvent",
                event.newInstance(ForgeRegistries.Keys.FLUID_TYPES, ForgeRegistries.FLUID_TYPES.get(), null));
    }

    public static synchronized void registerCraftingSerializers() throws ReflectiveOperationException {
        start();
        registerHolderSetTypes();
        if (ForgeRegistries.INGREDIENT_SERIALIZERS.get() == null)
            createForgeRegistry(ForgeRegistries.Keys.INGREDIENT_SERIALIZERS);
        var field = ForgeMod.class.getDeclaredField("INGREDIENT_SERIALIZERS");
        field.setAccessible(true);
        var register = (DeferredRegister<?>) field.get(null);
        if (register.getEntries().stream().allMatch(net.minecraftforge.registries.RegistryObject::isPresent)) return;
        var event = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        event.setAccessible(true);
        invokeRegistryPhase(register, "handleEvent", event.newInstance(
                ForgeRegistries.Keys.INGREDIENT_SERIALIZERS, ForgeRegistries.INGREDIENT_SERIALIZERS.get(), null));
    }

    public static synchronized void registerHolderSetTypes() throws ReflectiveOperationException {
        start();
        if (ForgeRegistries.HOLDER_SET_TYPES.get() == null)
            createForgeRegistry(ForgeRegistries.Keys.HOLDER_SET_TYPES);
        var field = ForgeMod.class.getDeclaredField("HOLDER_SET_TYPES");
        field.setAccessible(true);
        var register = (DeferredRegister<?>)field.get(null);
        if (register.getEntries().stream().allMatch(net.minecraftforge.registries.RegistryObject::isPresent)) return;
        var event = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        event.setAccessible(true);
        invokeRegistryPhase(register, "handleEvent", event.newInstance(
                ForgeRegistries.Keys.HOLDER_SET_TYPES, ForgeRegistries.HOLDER_SET_TYPES.get(), null));
        if (register.getEntries().stream().anyMatch(entry -> !entry.isPresent()))
            throw new IllegalStateException("Native Forge HolderSet types did not bind");
    }

    private static void createForgeRegistry(ResourceKey<?> key) throws ReflectiveOperationException {
        var registries = ForgeRegistries.class.getDeclaredField("registries");
        registries.setAccessible(true);
        DeferredRegister<?> register = ((List<?>) registries.get(null)).stream()
                .map(DeferredRegister.class::cast).filter(value -> key.equals(value.getRegistryKey()))
                .findFirst().orElseThrow();
        var event = new NewRegistryEvent();
        invokeRegistryPhase(register, "createRegistry", event);
        var fill = NewRegistryEvent.class.getDeclaredMethod("fill");
        fill.setAccessible(true);
        fill.invoke(event);
    }

    private static void invokeRegistryPhase(DeferredRegister<?> register, String phase, Object event)
            throws ReflectiveOperationException {
        // The launch-only EventBus transform is not present in plain JUnit.
        Class<?> dispatcher = Class.forName(DeferredRegister.class.getName() + "$EventDispatcher");
        var constructor = dispatcher.getDeclaredConstructor(DeferredRegister.class);
        constructor.setAccessible(true);
        var method = dispatcher.getDeclaredMethod(phase, event.getClass());
        method.setAccessible(true);
        method.invoke(constructor.newInstance(register), event);
    }

    public static void bindItemTags(java.util.Map<net.minecraft.tags.TagKey<net.minecraft.world.item.Item>,
            List<net.minecraft.core.Holder<net.minecraft.world.item.Item>>> tags) {
        var registry = (net.minecraft.core.MappedRegistry<net.minecraft.world.item.Item>)BuiltInRegistries.ITEM;
        registry.unfreeze();
        registry.bindAllTagsToEmpty();
        BuiltInRegistries.ITEM.freeze();
        BuiltInRegistries.ITEM.prepareTagReload(new net.minecraft.tags.TagLoader.LoadResult<>(
                net.minecraft.core.registries.Registries.ITEM, tags)).apply();
    }

    private static final class NativeRegistries {
        static {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }

        private static void ensureStarted() {}
    }
}
