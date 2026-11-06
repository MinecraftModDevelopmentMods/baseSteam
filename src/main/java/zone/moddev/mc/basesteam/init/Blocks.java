package zone.moddev.mc.basesteam.init;

import java.util.LinkedHashMap;
import java.util.Map;

import cyano.poweradvantage.api.GUIBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zone.moddev.mc.basepower.blocks.BlockPowerSwitch;
import zone.moddev.mc.basepower.init.ItemGroups;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.blocks.DrillBitBlock;
import zone.moddev.mc.basesteam.blocks.PlatformBlock;
import zone.moddev.mc.basesteam.blocks.PumpPipeBlock;
import zone.moddev.mc.basesteam.blocks.SteamPipeBlock;
import zone.moddev.mc.basesteam.blocks.SteamTrackBlock;
import zone.moddev.mc.basesteam.machines.BlastFurnaceBlock;
import zone.moddev.mc.basesteam.machines.CoalBoilerBlock;
import zone.moddev.mc.basesteam.machines.ElectricBoilerBlock;
import zone.moddev.mc.basesteam.machines.GeothermalBoilerBlock;
import zone.moddev.mc.basesteam.machines.OilBoilerBlock;
import zone.moddev.mc.basesteam.machines.RockCrusherBlock;
import zone.moddev.mc.basesteam.machines.SteamDrillBlock;
import zone.moddev.mc.basesteam.machines.SteamElevatorBlock;
import zone.moddev.mc.basesteam.machines.SteamPumpBlock;
import zone.moddev.mc.basesteam.machines.SteamStillBlock;
import zone.moddev.mc.basesteam.machines.SteamTankBlock;

@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Blocks {
    private static final Map<String, Block> ALL_BLOCKS = new LinkedHashMap<>();

    public static GUIBlock steam_boiler_coal;
    public static GUIBlock steam_boiler_electric;
    public static GUIBlock steam_boiler_geothermal;
    public static GUIBlock steam_crusher;
    public static GUIBlock steam_furnace;
    public static GUIBlock steam_tank;
    public static GUIBlock steam_drill;
    public static GUIBlock steam_elevator;
    public static Block steam_elevator_platform;
    public static BlockState[] steam_elevator_platforms;
    public static Block steam_pipe;
    public static Block drillbit;
    public static Block steam_track;
    public static Block pump_pipe_steam;
    public static Block steam_switch;
    public static GUIBlock steam_still;
    public static GUIBlock steam_pump;
    public static GUIBlock steam_boiler_oil;

    private static boolean initialized;
    private Blocks() {}

    public static synchronized void init() {
        if (initialized) return;
        ItemGroups.init();
        steam_pipe = add(new SteamPipeBlock(), "steam_pipe");
        steam_track = add(new SteamTrackBlock(), "steam_track");
        steam_boiler_coal = (GUIBlock) add(new CoalBoilerBlock(), "steam_boiler_coal");
        steam_boiler_electric = (GUIBlock) add(new ElectricBoilerBlock(), "steam_boiler_electric");
        steam_boiler_geothermal = (GUIBlock) add(new GeothermalBoilerBlock(), "steam_boiler_geothermal");
        steam_tank = (GUIBlock) add(new SteamTankBlock(), "steam_tank");
        steam_furnace = (GUIBlock) add(new BlastFurnaceBlock(), "steam_furnace");
        steam_crusher = (GUIBlock) add(new RockCrusherBlock(), "steam_crusher");
        steam_drill = (GUIBlock) add(new SteamDrillBlock(), "steam_drill");
        drillbit = add(new DrillBitBlock(), "drillbit");
        steam_elevator_platform = add(new PlatformBlock(), "platform");
        steam_elevator_platforms = new BlockState[5];
        for (int i = 0; i < steam_elevator_platforms.length; i++) {
            steam_elevator_platforms[i] = steam_elevator_platform.getDefaultState().with(PlatformBlock.HEIGHT, i);
        }
        steam_elevator = (GUIBlock) add(new SteamElevatorBlock(), "steam_elevator");
        steam_switch = add(new BlockPowerSwitch(Power.steam_power), "steam_switch");
        pump_pipe_steam = add(new PumpPipeBlock(), "pump_pipe_steam");
        steam_still = (GUIBlock) add(new SteamStillBlock(), "steam_still");
        steam_pump = (GUIBlock) add(new SteamPumpBlock(), "steam_pump");
        steam_boiler_oil = (GUIBlock) add(new OilBoilerBlock(), "steam_boiler_oil");
        initialized = true;
    }

    private static Block add(Block block, String id) {
        block.setRegistryName(SteamAdvantage.MODID, id);
        ALL_BLOCKS.put(id, block);
        return block;
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        init();
        event.getRegistry().registerAll(ALL_BLOCKS.values().toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void registerBlockItems(RegistryEvent.Register<Item> event) {
        init();
        for (Map.Entry<String, Block> entry : ALL_BLOCKS.entrySet()) {
            Item.Properties properties = new Item.Properties();
            if (!"pump_pipe_steam".equals(entry.getKey()) && !"drillbit".equals(entry.getKey())) {
                properties.group(ItemGroups.tab_powerAdvantage);
            }
            BlockItem item = new BlockItem(entry.getValue(), properties);
            item.setRegistryName(SteamAdvantage.MODID, entry.getKey());
            event.getRegistry().register(item);
        }
    }
}
