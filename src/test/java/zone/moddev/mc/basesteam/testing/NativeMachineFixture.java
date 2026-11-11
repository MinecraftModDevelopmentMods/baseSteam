package zone.moddev.mc.basesteam.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import zone.moddev.mc.basesteam.blocks.DrillBitBlock;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;
import zone.moddev.mc.basesteam.init.Entities;
import zone.moddev.mc.basesteam.machines.*;

/** Actual type/block pairs for detached tests, with every Entities field restored even on failure. */
public final class NativeMachineFixture implements TestRule {
    @Override public Statement apply(Statement test, Description description) {
        return new Statement() {
            @Override public void evaluate() throws Throwable {
                NativeTestBootstrap.start();
                List<Runnable> restore = new ArrayList<>();
                try {
                    for (Fixture<?> fixture : Fixtures.ALL) restore.add(fixture.install());
                    test.evaluate();
                } finally {
                    for (int index = restore.size() - 1; index >= 0; index--) restore.get(index).run();
                }
            }
        };
    }

    public static BlockState state(String id) {
        return fixture(id).block.defaultBlockState();
    }

    public static List<Block> blocks() {
        return Fixtures.ALL.stream().map(fixture -> fixture.block).toList();
    }

    public static <T extends BlockEntity> T create(Class<T> tileClass) {
        for (Fixture<?> fixture : Fixtures.ALL) {
            BlockEntity tile = fixture.type.create(BlockPos.ZERO, fixture.block.defaultBlockState());
            if (tileClass.isInstance(tile)) return tileClass.cast(tile);
        }
        throw new IllegalArgumentException("No native Steam fixture for " + tileClass);
    }

    private static Fixture<?> fixture(String id) {
        return Fixtures.ALL.stream().filter(fixture -> fixture.id.equals(id)).findFirst().orElseThrow();
    }

    private static final class Fixtures {
        private static final List<Fixture<?>> ALL = List.of(
                new Fixture<>("steam_boiler_coal", new CoalBoilerBlock(), CoalBoilerTileEntity::new,
                        () -> Entities.STEAM_BOILER_COAL, type -> Entities.STEAM_BOILER_COAL = type),
                new Fixture<>("steam_boiler_electric", new ElectricBoilerBlock(), ElectricBoilerTileEntity::new,
                        () -> Entities.STEAM_BOILER_ELECTRIC, type -> Entities.STEAM_BOILER_ELECTRIC = type),
                new Fixture<>("steam_boiler_geothermal", new GeothermalBoilerBlock(), GeothermalBoilerTileEntity::new,
                        () -> Entities.STEAM_BOILER_GEOTHERMAL, type -> Entities.STEAM_BOILER_GEOTHERMAL = type),
                new Fixture<>("steam_boiler_oil", new OilBoilerBlock(), OilBoilerTileEntity::new,
                        () -> Entities.STEAM_BOILER_OIL, type -> Entities.STEAM_BOILER_OIL = type),
                new Fixture<>("steam_tank", new SteamTankBlock(), SteamTankTileEntity::new,
                        () -> Entities.STEAM_TANK, type -> Entities.STEAM_TANK = type),
                new Fixture<>("steam_furnace", new BlastFurnaceBlock(), BlastFurnaceTileEntity::new,
                        () -> Entities.STEAM_FURNACE, type -> Entities.STEAM_FURNACE = type),
                new Fixture<>("steam_crusher", new RockCrusherBlock(), RockCrusherTileEntity::new,
                        () -> Entities.STEAM_CRUSHER, type -> Entities.STEAM_CRUSHER = type),
                new Fixture<>("steam_drill", new SteamDrillBlock(), SteamDrillTileEntity::new,
                        () -> Entities.STEAM_DRILL, type -> Entities.STEAM_DRILL = type),
                new Fixture<>("steam_elevator", new SteamElevatorBlock(), SteamElevatorTileEntity::new,
                        () -> Entities.STEAM_ELEVATOR, type -> Entities.STEAM_ELEVATOR = type),
                new Fixture<>("drillbit", new DrillBitBlock(), DrillBitTileEntity::new,
                        () -> Entities.DRILLBIT, type -> Entities.DRILLBIT = type),
                new Fixture<>("steam_still", new SteamStillBlock(), SteamStillTileEntity::new,
                        () -> Entities.STEAM_STILL, type -> Entities.STEAM_STILL = type),
                new Fixture<>("steam_pump", new SteamPumpBlock(), SteamPumpTileEntity::new,
                        () -> Entities.STEAM_PUMP, type -> Entities.STEAM_PUMP = type));
    }

    private static final class Fixture<T extends BlockEntity> {
        private final String id;
        private final Block block;
        private final BlockEntityType<T> type;
        private final Supplier<BlockEntityType<T>> getter;
        private final Consumer<BlockEntityType<T>> setter;

        private Fixture(String id, Block block, BlockEntityType.BlockEntitySupplier<T> factory,
                Supplier<BlockEntityType<T>> getter, Consumer<BlockEntityType<T>> setter) {
            this.id = id;
            this.block = block;
            this.getter = getter;
            this.setter = setter;
            type = BlockEntityType.Builder.of(factory, block).build(null);
            ForgeRegistry<BlockEntityType<?>> registry = (ForgeRegistry<BlockEntityType<?>>) ForgeRegistries.BLOCK_ENTITY_TYPES;
            boolean locked = registry.isLocked();
            registry.unfreeze();
            try {
                registry.register(ResourceLocation.fromNamespaceAndPath("basesteam", id), type);
            } finally {
                if (locked) registry.freeze();
            }
        }

        private Runnable install() {
            BlockEntityType<T> previous = getter.get();
            setter.accept(type);
            return () -> setter.accept(previous);
        }
    }
}
