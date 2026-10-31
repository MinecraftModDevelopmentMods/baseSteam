package zone.moddev.mc.steamadvantage.init;


import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zone.moddev.mc.steamadvantage.SteamAdvantage;
import zone.moddev.mc.steamadvantage.blocks.DrillBitTileEntity;
import zone.moddev.mc.steamadvantage.machines.BlastFurnaceTileEntity;
import zone.moddev.mc.steamadvantage.machines.CoalBoilerTileEntity;
import zone.moddev.mc.steamadvantage.machines.ElectricBoilerTileEntity;
import zone.moddev.mc.steamadvantage.machines.GeothermalBoilerTileEntity;
import zone.moddev.mc.steamadvantage.machines.OilBoilerTileEntity;
import zone.moddev.mc.steamadvantage.machines.RockCrusherTileEntity;
import zone.moddev.mc.steamadvantage.machines.SteamDrillTileEntity;
import zone.moddev.mc.steamadvantage.machines.SteamElevatorTileEntity;
import zone.moddev.mc.steamadvantage.machines.SteamPumpTileEntity;
import zone.moddev.mc.steamadvantage.machines.SteamStillTileEntity;
import zone.moddev.mc.steamadvantage.machines.SteamTankTileEntity;

/** Stable tile entity registrations; registry names are unchanged from 1.12. */
@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Entities {
    public static BlockEntityType<CoalBoilerTileEntity> STEAM_BOILER_COAL;
    public static BlockEntityType<ElectricBoilerTileEntity> STEAM_BOILER_ELECTRIC;
    public static BlockEntityType<GeothermalBoilerTileEntity> STEAM_BOILER_GEOTHERMAL;
    public static BlockEntityType<SteamTankTileEntity> STEAM_TANK;
    public static BlockEntityType<BlastFurnaceTileEntity> STEAM_FURNACE;
    public static BlockEntityType<RockCrusherTileEntity> STEAM_CRUSHER;
    public static BlockEntityType<SteamDrillTileEntity> STEAM_DRILL;
    public static BlockEntityType<SteamElevatorTileEntity> STEAM_ELEVATOR;
    public static BlockEntityType<DrillBitTileEntity> DRILLBIT;
    public static BlockEntityType<SteamStillTileEntity> STEAM_STILL;
    public static BlockEntityType<SteamPumpTileEntity> STEAM_PUMP;
    public static BlockEntityType<OilBoilerTileEntity> STEAM_BOILER_OIL;

    private Entities() {}
    public static void init() {}

    @SubscribeEvent
    public static void register(RegistryEvent.Register<BlockEntityType<?>> event) {
        STEAM_BOILER_COAL = register(event, "steam_boiler_coal", CoalBoilerTileEntity::new, Blocks.steam_boiler_coal);
        STEAM_BOILER_ELECTRIC = register(event, "steam_boiler_electric", ElectricBoilerTileEntity::new, Blocks.steam_boiler_electric);
        STEAM_BOILER_GEOTHERMAL = register(event, "steam_boiler_geothermal", GeothermalBoilerTileEntity::new, Blocks.steam_boiler_geothermal);
        STEAM_TANK = register(event, "steam_tank", SteamTankTileEntity::new, Blocks.steam_tank);
        STEAM_FURNACE = register(event, "steam_furnace", BlastFurnaceTileEntity::new, Blocks.steam_furnace);
        STEAM_CRUSHER = register(event, "steam_crusher", RockCrusherTileEntity::new, Blocks.steam_crusher);
        STEAM_DRILL = register(event, "steam_drill", SteamDrillTileEntity::new, Blocks.steam_drill);
        STEAM_ELEVATOR = register(event, "steam_elevator", SteamElevatorTileEntity::new, Blocks.steam_elevator);
        DRILLBIT = register(event, "drillbit", DrillBitTileEntity::new, Blocks.drillbit);
        STEAM_STILL = register(event, "steam_still", SteamStillTileEntity::new, Blocks.steam_still);
        STEAM_PUMP = register(event, "steam_pump", SteamPumpTileEntity::new, Blocks.steam_pump);
        STEAM_BOILER_OIL = register(event, "steam_boiler_oil", OilBoilerTileEntity::new, Blocks.steam_boiler_oil);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            RegistryEvent.Register<BlockEntityType<?>> event, String id, BlockEntityType.BlockEntitySupplier<T> factory, Block block) {
        BlockEntityType<T> type = BlockEntityType.Builder.<T>of(factory, block).build(null);
        type.setRegistryName(SteamAdvantage.MODID, id);
        event.getRegistry().register(type);
        return type;
    }
}
