package zone.moddev.mc.basesteam;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import cyano.poweradvantage.api.migration.LegacyConfigMigration;
import cyano.poweradvantage.api.migration.LegacyConfigMigration.Mapping;
import cyano.poweradvantage.api.migration.LegacyConfigMigration.ValueType;
import zone.moddev.mc.basesteam.config.SteamAdvantageConfig;
import zone.moddev.mc.basesteam.init.GUI;
import zone.moddev.mc.basesteam.init.Recipes;
import zone.moddev.mc.basesteam.init.TreasureChests;
import zone.moddev.mc.basesteam.init.Villages;
import zone.moddev.mc.basesteam.migration.SteamLegacyMigrations;
import zone.moddev.mc.basesteam.migration.SteamConfigMigration;
import zone.moddev.mc.basesteam.recipe.RecipeConditions;

/** Forge 1.20.6 entry point for baseSteam. */
@Mod(SteamAdvantage.MODID)
public final class SteamAdvantage {
    public static final String MODID = "basesteam";
    public static final String NAME = "baseSteam";
    public static final String VERSION = "2.2.14.120061";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static float MUSKET_DAMAGE = 20.0F;
    public static int MUSKET_RELOAD = 20 * 5;
    public static boolean MUSKET_ENABLE = true;
    public static final Map<String, Float> fluidBurnValues = new HashMap<>();

    public SteamAdvantage(FMLJavaModLoadingContext context) {
        SteamLegacyMigrations.register();

        migrateLegacyConfiguration();
        context.registerConfig(ModConfig.Type.COMMON, SteamAdvantageConfig.SPEC);
        var modBus = context.getModEventBus();
        modBus.addListener(SteamAdvantageConfig::onConfigLoading);
        modBus.addListener(this::commonSetup);
        RecipeConditions.register(modBus);

        DistExecutor.runWhenOn(Dist.CLIENT,
                () -> () -> zone.moddev.mc.basesteam.graphics.ClientRegistration.register(modBus));
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
        Recipes.init();
        TreasureChests.init(FMLPaths.CONFIGDIR.get());
        Villages.init();
        LOGGER.info("baseSteam {} common setup complete", VERSION);
    }
}
