package zone.moddev.mc.basesteam;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import cyano.poweradvantage.api.migration.LegacyConfigMigration;
import cyano.poweradvantage.api.migration.LegacyConfigMigration.Mapping;
import cyano.poweradvantage.api.migration.LegacyConfigMigration.ValueType;
import zone.moddev.mc.basesteam.config.SteamAdvantageConfig;
import zone.moddev.mc.basesteam.init.GUI;
import zone.moddev.mc.basesteam.init.TreasureChests;
import zone.moddev.mc.basesteam.init.Villages;
import zone.moddev.mc.basesteam.migration.SteamLegacyMigrations;
import zone.moddev.mc.basesteam.migration.SteamConfigMigration;
import zone.moddev.mc.basesteam.recipe.RecipeConditions;

/** NeoForge 1.21.1 entry point for baseSteam. */
@Mod(SteamAdvantage.MODID)
public final class SteamAdvantage {
    public static final String MODID = "basesteam";
    public static final String NAME = "baseSteam";
    public static final String VERSION = "2.2.20.121012";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static float MUSKET_DAMAGE = 20.0F;
    public static int MUSKET_RELOAD = 20 * 5;
    public static boolean MUSKET_ENABLE = true;
    public static final Map<String, Float> fluidBurnValues = new HashMap<>();

    public SteamAdvantage(IEventBus modBus, ModContainer context) {
        SteamLegacyMigrations.register();

        migrateLegacyConfiguration();
        context.registerConfig(ModConfig.Type.COMMON, SteamAdvantageConfig.SPEC);
        modBus.addListener(SteamAdvantageConfig::onConfigLoading);
        modBus.addListener(this::commonSetup);
        RecipeConditions.register(modBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            zone.moddev.mc.basesteam.graphics.ClientRegistration.register(modBus);
        }
    }

    private static void migrateLegacyConfiguration() {
        try {
            Path configDirectory = FMLPaths.CONFIGDIR.get();
            if (SteamConfigMigration.copyLegacyCommonConfig(configDirectory)) {
                LOGGER.info("Copied Steam Advantage settings into basesteam-common.toml");
            }
            LegacyConfigMigration.Result result = LegacyConfigMigration.migrate(configDirectory, MODID,
                    SteamLegacyMigrations.LEGACY_MODID,
                    Mapping.of("options", "musket_damage", "options", "musketDamage", ValueType.DOUBLE),
                    Mapping.of("options", "musket_reload_time", "options", "musketReloadTime", ValueType.INTEGER),
                    Mapping.of("options", "musket_allowed", "options", "musketAllowed", ValueType.BOOLEAN),
                    Mapping.of("options", "fluid_fuel_values", "options", "fluidFuelValues", ValueType.STRING));
            if (result.wasMigrated()) {
                LOGGER.info("Imported {} legacy Steam Advantage settings; report: {}",
                        result.getImportedValues(), result.getReportFile());
            }
        } catch (IOException exception) {
            LOGGER.error("Could not migrate legacy Steam Advantage configuration", exception);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(GUI::init);
        SteamAdvantageConfig.apply();
        TreasureChests.init(FMLPaths.CONFIGDIR.get());
        Villages.init();
        LOGGER.info("baseSteam {} common setup complete", VERSION);
    }
}
