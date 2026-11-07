package zone.moddev.mc.basesteam.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cyano.poweradvantage.api.migration.LegacyMigrationApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.junit.Test;

public class SteamLegacyMigrationsTest {
    @Test public void preservesAllSixDrillFacings() {
        for (Direction facing : Direction.values()) {
            assertEquals(facing, SteamLegacyMigrations.legacyDrillFacing(facing.get3DDataValue()));
        }
    }

    @Test public void clampsLegacyPlatformHeight() {
        assertEquals(0, SteamLegacyMigrations.legacyPlatformHeight(0));
        assertEquals(3, SteamLegacyMigrations.legacyPlatformHeight(3));
        assertEquals(4, SteamLegacyMigrations.legacyPlatformHeight(15));
    }

    @Test public void repairsLegacyPumpAndGeothermalKeysWithoutOverwritingCanonicalState() {
        CompoundTag pump = new CompoundTag();
        pump.putString("id", "steamadvantage.steam_pump");
        CompoundTag oldTank = new CompoundTag();
        oldTank.putInt("Amount", 1250);
        pump.put("TankOut", oldTank);
        assertTrue(SteamLegacyMigrations.repairPersistenceKeys(pump));
        assertEquals(1250, pump.getCompound("Tank").getInt("Amount"));

        CompoundTag boiler = new CompoundTag();
        boiler.putString("id", "steamadvantage:steam_boiler_geothermal");
        boiler.putFloat("Tempoerature", 412.5F);
        assertTrue(SteamLegacyMigrations.repairPersistenceKeys(boiler));
        assertEquals(412.5F, boiler.getFloat("Temperature"), 0.0F);

        CompoundTag canonical = new CompoundTag();
        canonical.putString("id", "basesteam:steam_boiler_geothermal");
        canonical.putFloat("Temperature", 500.0F);
        canonical.putFloat("Tempoerature", 100.0F);
        assertFalse(SteamLegacyMigrations.repairPersistenceKeys(canonical));
        assertEquals(500.0F, canonical.getFloat("Temperature"), 0.0F);
    }

    @Test public void aliasesLegacyTileIds() {
        SteamLegacyMigrations.register();
        for (String path : new String[] {"steam_boiler_coal", "steam_boiler_electric", "steam_boiler_geothermal",
                "steam_tank", "steam_furnace", "steam_crusher", "steam_drill", "steam_elevator",
                "drillbit", "steam_still", "steam_pump", "steam_boiler_oil"}) {
            for (String source : legacyTileIds(path)) {
                assertEquals(source, "basesteam:" + path, LegacyMigrationApi.tileTarget(source));
            }
        }
    }

    @Test public void namespaceAliasRetainsNativePathsAndLegacyCodecs() {
        SteamLegacyMigrations.register();
        assertTrue(LegacyMigrationApi.handlesNamespace("steamadvantage"));
        assertTrue(LegacyMigrationApi.handlesNamespace("basesteam"));
        for (String path : new String[] {"steam_pipe", "steam_track", "steam_boiler_coal", "steam_boiler_electric",
                "steam_boiler_geothermal", "steam_tank", "steam_furnace", "steam_crusher", "steam_drill",
                "drillbit", "platform", "steam_elevator", "steam_switch", "pump_pipe_steam", "steam_still",
                "steam_pump", "steam_boiler_oil"}) {
            ResourceLocation current = new ResourceLocation("basesteam", path);
            assertEquals(current, LegacyMigrationApi.blockTarget(new ResourceLocation("steamadvantage", path)));
            assertTrue(path, LegacyMigrationApi.blockStateCodecs().containsKey(current));
        }
        ResourceLocation oldMusket = new ResourceLocation("steamadvantage:musket");
        assertEquals(new ResourceLocation("basesteam:musket"), LegacyMigrationApi.itemTarget(oldMusket));
        assertTrue(LegacyMigrationApi.isDurableItem(oldMusket));
        assertEquals(new ResourceLocation("basesteam:rapid_reload"),
                LegacyMigrationApi.registryTarget(new ResourceLocation("steamadvantage:rapid_reload")));
    }

    @Test public void repairsOldKeysForBothNamespacesWithoutDependingOnRemapOrder() {
        for (String source : legacyTileIds("steam_pump")) {
            CompoundTag pump = new CompoundTag();
            pump.putString("id", source);
            CompoundTag tank = new CompoundTag();
            tank.putInt("Amount", 1250);
            pump.put("TankOut", tank);
            assertTrue(source, SteamLegacyMigrations.repairPersistenceKeys(pump));
            assertEquals(source, pump.getString("id"));
            assertEquals(tank, pump.getCompound("Tank"));
            assertFalse(SteamLegacyMigrations.repairPersistenceKeys(pump));
        }
    }

    private static String[] legacyTileIds(String path) {
        return new String[] {"steamadvantage:" + path, "steamadvantage." + path,
                "minecraft:steamadvantage." + path, "basesteam:" + path, "basesteam." + path,
                "minecraft:basesteam." + path};
    }
}
