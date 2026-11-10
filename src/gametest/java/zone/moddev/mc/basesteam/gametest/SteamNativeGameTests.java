package zone.moddev.mc.basesteam.gametest;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestHolder;
import zone.moddev.mc.basepower.migration.LegacyWorldDataHook;
import zone.moddev.mc.basepower.util.ItemData;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;
import zone.moddev.mc.basesteam.init.Blocks;
import zone.moddev.mc.basesteam.init.Entities;
import zone.moddev.mc.basesteam.init.Items;
import zone.moddev.mc.basesteam.items.MusketItem;

@GameTestHolder(value = SteamAdvantage.MODID, namespace = SteamAdvantage.MODID)
@GameTestDontPrefix
public final class SteamNativeGameTests {
    private SteamNativeGameTests() {}

    @GameTest(template = "basesteam_empty")
    public static void drillBitPacketPreservesTypePositionAndDirection(GameTestHelper helper) {
        BlockPos local = new BlockPos(1, 1, 1);
        helper.setBlock(local, Blocks.drillbit);
        DrillBitTileEntity source = (DrillBitTileEntity) helper.getBlockEntity(local);
        var registries = helper.getLevel().registryAccess();
        for (Direction direction : Direction.values()) {
            source.setDirection(direction);
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
            try {
                ClientboundBlockEntityDataPacket.STREAM_CODEC.encode(buffer, source.getUpdatePacket());
                var received = ClientboundBlockEntityDataPacket.STREAM_CODEC.decode(buffer);
                helper.assertTrue(received.getType() == Entities.DRILLBIT, "Wrong drill-bit packet type");
                helper.assertTrue(received.getPos().equals(helper.absolutePos(local)), "Wrong drill-bit position");
                DrillBitTileEntity destination = new DrillBitTileEntity(received.getPos(), source.getBlockState());
                destination.onDataPacket(null, received, registries);
                helper.assertTrue(destination.getUpdateTag(registries).getByte("dir") == direction.get3DDataValue(),
                        "Lost drill-bit direction " + direction);
            } finally {
                buffer.release();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "basesteam_empty")
    public static void legacyMusketWearAndLoadedByteSurviveNativeComponentization(GameTestHelper helper) {
        CompoundTag legacy = new CompoundTag();
        legacy.putString("id", "basesteam:musket");
        legacy.putByte("Count", (byte) 1);
        CompoundTag tag = new CompoundTag();
        tag.putInt("Damage", 249);
        tag.putByte("loaded", (byte) 1);
        tag.putString("owner", "legacy fixture");
        legacy.put("tag", tag);
        var registries = helper.getLevel().registryAccess();
        var fixed = DataFixers.getDataFixer().update(References.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, legacy), 3465, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        ItemStack stack = ItemStack.parse(registries, fixed.getValue()).orElseThrow();
        helper.assertTrue(stack.is(Items.blackpowder_musket), "Converted the wrong item");
        helper.assertTrue(stack.getDamageValue() == 249 && stack.getMaxDamage() == 250, "Lost legacy wear");
        helper.assertTrue(MusketItem.isLoaded(stack), "Lost the legacy loaded byte");
        MusketItem.unload(stack);
        ItemStack reloaded = ItemStack.parse(registries, stack.save(registries)).orElseThrow();
        helper.assertTrue(MusketItem.isNotLoaded(reloaded) && reloaded.getDamageValue() == 249,
                "Changing loaded state must not reset wear");
        helper.assertTrue("legacy fixture".equals(reloaded.get(DataComponents.CUSTOM_DATA).copyTag().getString("owner")),
                "Changing loaded state must not discard unrelated legacy data");
        helper.succeed();
    }

    @GameTest(template = "basesteam_empty")
    public static void olderMusketAliasAndTopLevelWearSurvivePowerMigration(GameTestHelper helper) {
        CompoundTag legacy = new CompoundTag();
        legacy.putString("id", "steamadvantage:musket");
        legacy.putByte("Count", (byte) 1);
        legacy.putShort("Damage", (short) 131);
        CompoundTag tag = new CompoundTag();
        tag.putByte("loaded", (byte) 2);
        legacy.put("tag", tag);
        LegacyWorldDataHook.prepareLegacyChunk(legacy);
        ItemStack migrated = ItemData.readStack(helper.getLevel().registryAccess(), legacy);
        helper.assertTrue(migrated.is(Items.blackpowder_musket), "The old musket ID was not remapped");
        helper.assertTrue(migrated.getDamageValue() == 131, "The legacy Damage short was lost");
        helper.assertTrue(MusketItem.isAlmostLoaded(migrated), "The partial reload was lost");
        helper.succeed();
    }

    @GameTest(template = "basesteam_empty")
    public static void nativeResourcesLoadWithExpectedIds(GameTestHelper helper) {
        var level = helper.getLevel();
        var registries = level.registryAccess();
        var manager = level.getServer().getAdvancements();
        helper.assertTrue(manager.get(id("recipes/steam_pipe")) != null, "Missing Steam pipe recipe advancement");
        var unlock = manager.get(id("recipes/musket_right_normal")).value();
        var ingredient = (InventoryChangeTrigger.TriggerInstance)
                unlock.criteria().get("has_ingredient_1").triggerInstance();
        var predicate = ingredient.items().getFirst();
        helper.assertTrue(predicate.test(new ItemStack(net.minecraft.world.item.Items.FLINT_AND_STEEL)),
                "Musket discovery lost the matching ingredient");
        helper.assertTrue(!predicate.test(new ItemStack(net.minecraft.world.item.Items.STONE)),
                "An unrelated item unlocked the musket recipe");
        var tagIngredient = (InventoryChangeTrigger.TriggerInstance)
                unlock.criteria().get("has_ingredient_3").triggerInstance();
        helper.assertTrue(tagIngredient.items().getFirst().test(new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS)),
                "Musket discovery lost the plank tag predicate");
        var loot = level.getServer().reloadableRegistries();
        var chest = ResourceKey.create(Registries.LOOT_TABLE, id("chests/advantage_parts"));
        LootTable chestTable = loot.getLootTable(chest);
        helper.assertTrue(chestTable != LootTable.EMPTY && chestTable.getParamSet() == LootContextParamSets.CHEST,
                "Missing Steam chest loot table");
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);
        boolean foundSteamItem = false;
        for (int seed = 1; seed <= 32; seed++) {
            if (!chestTable.getRandomItems(params, seed).isEmpty()) foundSteamItem = true;
        }
        helper.assertTrue(foundSteamItem, "Steam chest rolls returned no items");
        helper.assertTrue(loot.getLootTable(Blocks.steam_pipe.getLootTable()) != LootTable.EMPTY,
                "Missing Steam pipe block loot");
        var pipe = level.getRecipeManager().byKey(id("steam_pipe"));
        var brass = net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ingots/brass"));
        if (registries.lookupOrThrow(Registries.ITEM).get(brass).filter(tag -> tag.size() > 0).isPresent()) {
            var result = pipe.orElseThrow().value().getResultItem(registries);
            helper.assertTrue(result.is(Blocks.steam_pipe.asItem()) && result.getCount() == 6,
                    "Wrong Steam pipe recipe output");
        } else {
            helper.assertTrue(pipe.isEmpty(), "Recipe must not load with an empty brass tag");
        }
        helper.succeed();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, path);
    }
}
