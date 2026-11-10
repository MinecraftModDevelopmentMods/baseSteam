package zone.moddev.mc.basesteam.migration;

import cyano.poweradvantage.api.migration.LegacyMigrationApi;
import cyano.poweradvantage.api.simple.BlockSimplePowerMachine;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import zone.moddev.mc.basepower.blocks.BlockPowerSwitch;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.blocks.PlatformBlock;
import zone.moddev.mc.basesteam.machines.SteamDrillBlock;

/** Registers old Steam identities and state codecs with basePower's shared migration hook. */
public final class SteamLegacyMigrations {
    public static final String LEGACY_MODID = "steamadvantage";
    private static boolean registered;

    private SteamLegacyMigrations() {}

    public static synchronized void register() {
        if (registered) return;

        LegacyMigrationApi.registerNamespaceAlias(LEGACY_MODID, SteamAdvantage.MODID);
        for (String path : new String[] {
                "steam_pipe", "steam_track", "steam_boiler_coal", "steam_boiler_electric",
                "steam_boiler_geothermal", "steam_tank", "steam_furnace", "steam_crusher",
                "steam_drill", "drillbit", "platform", "steam_elevator", "steam_switch",
                "pump_pipe_steam", "steam_still", "steam_pump", "steam_boiler_oil" }) {
            LegacyMigrationApi.registerBlockStateCodec(id(path), SteamLegacyMigrations::decodeState);
        }

        registerTile("steam_boiler_coal");
        registerTile("steam_boiler_electric");
        registerTile("steam_boiler_geothermal");
        registerTile("steam_tank");
        registerTile("steam_furnace");
        registerTile("steam_crusher");
        registerTile("steam_drill");
        registerTile("steam_elevator");
        registerTile("drillbit");
        registerTile("steam_still");
        registerTile("steam_pump");
        registerTile("steam_boiler_oil");

        LegacyMigrationApi.registerDurableItem(id("musket"));
        LegacyMigrationApi.registerNbtMigration("basesteam:persistence_keys",
                SteamLegacyMigrations::repairPersistenceKeys);
        registered = true;
    }

    private static void registerTile(String path) {
        LegacyMigrationApi.registerTileEntityAliases(id(path));
    }

    private static BlockState decodeState(Block block, int metadata) {
        if (block instanceof SteamDrillBlock) {
            return block.defaultBlockState().setValue(SteamDrillBlock.FACING, legacyDrillFacing(metadata));
        }
        if (block instanceof PlatformBlock) {
            return block.defaultBlockState().setValue(PlatformBlock.HEIGHT,
                    Integer.valueOf(legacyPlatformHeight(metadata)));
        }
        if (block instanceof BlockPowerSwitch) {
            return ((BlockPowerSwitch) block).getStateFromMeta(metadata);
        }
        if (block instanceof BlockSimplePowerMachine) {
            return ((BlockSimplePowerMachine) block).getStateFromMeta(metadata);
        }
        return block.defaultBlockState();
    }

    static Direction legacyDrillFacing(int metadata) {
        return Direction.from3DDataValue(metadata % Direction.values().length);
    }

    static int legacyPlatformHeight(int metadata) {
        return Math.min(metadata & 15, 4);
    }

    static boolean repairPersistenceKeys(CompoundTag compound) {
        String id = compound.getString("id");
        boolean changed = false;
        if (isTile(id, "steam_pump") && !compound.contains("Tank", 10) && compound.contains("TankOut", 10)) {
            compound.put("Tank", compound.getCompound("TankOut").copy());
            changed = true;
        }
        if (isTile(id, "steam_boiler_geothermal")
                && !compound.contains("Temperature", 99) && compound.contains("Tempoerature", 99)) {
            compound.putFloat("Temperature", compound.getFloat("Tempoerature"));
            changed = true;
        }
        return changed;
    }

    private static boolean isTile(String value, String path) {
        return matchesTileId(value, SteamAdvantage.MODID, path) || matchesTileId(value, LEGACY_MODID, path);
    }

    private static boolean matchesTileId(String value, String namespace, String path) {
        String dotted = namespace + "." + path;
        return (namespace + ":" + path).equalsIgnoreCase(value)
                || dotted.equalsIgnoreCase(value) || ("minecraft:" + dotted).equalsIgnoreCase(value);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, path);
    }
}
