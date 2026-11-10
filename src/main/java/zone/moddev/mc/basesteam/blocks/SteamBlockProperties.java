package zone.moddev.mc.basesteam.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** Native block properties matching the old piston-like construction material. */
final class SteamBlockProperties {
    private SteamBlockProperties() {}

    static Block.Properties pistonLike() {
        return Block.Properties.of().mapColor(MapColor.STONE).pushReaction(PushReaction.BLOCK)
                .forceSolidOn().strength(0.75F);
    }

    static Block.Properties metalHelper(float resistance) {
        return Block.Properties.of().mapColor(MapColor.METAL).forceSolidOn().strength(5.0F, resistance)
                .noOcclusion().noLootTable();
    }
}
