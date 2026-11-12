package zone.moddev.mc.basesteam.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** Native block properties matching the old piston-like construction material. */
public final class SteamBlockProperties {
    private SteamBlockProperties() {}

    public static net.minecraft.resources.ResourceKey<Block> key(String path) {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK,
                net.minecraft.resources.Identifier.fromNamespaceAndPath("basesteam", path));
    }

    public static Block.Properties pistonLike(String path) {
        return Block.Properties.of().setId(key(path)).mapColor(MapColor.STONE).pushReaction(PushReaction.IMMOVEABLE)
                .forceSolidOn().strength(0.75F);
    }

    static Block.Properties metalHelper(String path, float resistance) {
        return Block.Properties.of().setId(key(path)).mapColor(MapColor.METAL).forceSolidOn().strength(5.0F, resistance)
                .noOcclusion().noLootTable();
    }
}
