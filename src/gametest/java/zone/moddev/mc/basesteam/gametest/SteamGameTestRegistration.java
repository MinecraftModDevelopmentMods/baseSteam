package zone.moddev.mc.basesteam.gametest;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import zone.moddev.mc.basesteam.SteamAdvantage;

/** Native test functions are registered only in the isolated GameTest source scope. */
@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SteamGameTestRegistration {
    private SteamGameTestRegistration() {}

    @SubscribeEvent public static void register(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, registry -> {
            registry.register(id("drill_bit_packet_preserves_type_position_and_direction"),
                    SteamNativeGameTests::drillBitPacketPreservesTypePositionAndDirection);
            registry.register(id("legacy_musket_wear_and_loaded_byte_survive_native_componentization"),
                    SteamNativeGameTests::legacyMusketWearAndLoadedByteSurviveNativeComponentization);
            registry.register(id("older_musket_alias_and_top_level_wear_survive_power_migration"),
                    SteamNativeGameTests::olderMusketAliasAndTopLevelWearSurvivePowerMigration);
            registry.register(id("native_resources_load_with_expected_ids"), SteamNativeGameTests::nativeResourcesLoadWithExpectedIds);
        });
    }

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(SteamAdvantage.MODID, path); }
}
