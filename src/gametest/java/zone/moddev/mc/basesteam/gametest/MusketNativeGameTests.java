package zone.moddev.mc.basesteam.gametest;

import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import zone.moddev.mc.basesteam.init.Enchantments;
import zone.moddev.mc.basesteam.init.Items;
import zone.moddev.mc.basesteam.items.MusketItem;

@GameTestHolder("basesteam")
@PrefixGameTestTemplate(false)
public final class MusketNativeGameTests {
    private MusketNativeGameTests() {}

    @GameTest(batch = "basesteam_musket", template = "basesteam_empty")
    public static void rapidReloadConsumesTheLastCommonCartridgeAndLoadedMusketStillAims(GameTestHelper helper) {
        ServerPlayer player = player(helper, "rapid-last-cartridge");
        ItemStack weapon = weapon(player);
        weapon.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.rapid_reload), 1);
        ItemStack cartridge = new ItemStack(Items.blackpowder_cartridge);
        helper.assertTrue(cartridge.is(ItemTags.create(ResourceLocation.parse("c:ammo/blackpowder"))),
                "Native common cartridge tag was not bound");
        player.getInventory().setItem(1, cartridge);
        helper.assertTrue(Enchantments.level(Enchantments.rapid_reload, weapon, helper.getLevel().registryAccess()) == 1,
                "Rapid Reload holder did not survive the native enchantment component");
        helper.assertTrue(MusketItem.hasAmmo(player, weapon), "Survival musket cannot see its common-tag cartridge");
        helper.assertTrue(weapon.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.SUCCESS,
                "Rapid reload rejected the last finite common-tag cartridge");
        helper.assertTrue(MusketItem.isLoaded(weapon) && player.getInventory().getItem(1).isEmpty(),
                "Rapid reload must load the chamber and consume exactly one cartridge");
        helper.assertTrue(weapon.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.SUCCESS
                && player.isUsingItem(), "Loaded musket cannot aim without a spare cartridge");
        player.stopUsingItem();
        helper.assertTrue(weapon.getDamageValue() == 0 && MusketItem.isLoaded(weapon), "Reloading or aiming damaged or discharged the musket");
        helper.succeed();
    }

    @GameTest(batch = "basesteam_musket", template = "basesteam_empty")
    public static void ordinaryReloadConsumesOnlyAtCompletionAndReleaseFinishesTheChamber(GameTestHelper helper) {
        ServerPlayer player = player(helper, "ordinary-reload");
        ItemStack weapon = weapon(player);
        player.getInventory().setItem(1, new ItemStack(Items.blackpowder_cartridge, 2));
        helper.assertTrue(weapon.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.SUCCESS
                && player.isUsingItem(), "Ordinary common-tag reload did not enter the native use lifecycle");
        helper.assertTrue(MusketItem.isNotLoaded(weapon) && player.getInventory().getItem(1).getCount() == 2,
                "Beginning an ordinary reload consumed ammunition early");
        weapon.getItem().finishUsingItem(weapon, helper.getLevel(), player);
        helper.assertTrue(MusketItem.isAlmostLoaded(weapon) && player.getInventory().getItem(1).getCount() == 1,
                "Reload completion did not consume exactly one cartridge");
        weapon.getItem().releaseUsing(weapon, helper.getLevel(), player, 0);
        player.stopUsingItem();
        helper.assertTrue(MusketItem.isLoaded(weapon) && weapon.getDamageValue() == 0
                && player.getInventory().getItem(1).getCount() == 1, "Finishing the chamber consumed ammunition twice or fired");
        helper.succeed();
    }

    @GameTest(batch = "basesteam_musket", template = "basesteam_empty")
    public static void emptyInventoryRejectsOrdinaryAndRapidReloadWithoutStateLoss(GameTestHelper helper) {
        ServerPlayer player = player(helper, "no-ammo");
        for (int rapid : new int[] {0, 1}) {
            ItemStack weapon = weapon(player);
            if (rapid > 0) weapon.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.rapid_reload), rapid);
            player.getInventory().setItem(1, new ItemStack(net.minecraft.world.item.Items.STONE, 17));
            helper.assertTrue(weapon.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.FAIL,
                    "Unloaded survival musket accepted unrelated inventory as ammunition");
            helper.assertTrue(MusketItem.isNotLoaded(weapon) && weapon.getDamageValue() == 0
                    && player.getInventory().getItem(1).getCount() == 17, "Rejected reload changed the chamber, wear or unrelated inventory");
        }
        helper.succeed();
    }

    @GameTest(batch = "basesteam_musket", template = "basesteam_empty")
    public static void powderlessAndInfinityStillPermitRapidReloadWithoutCartridges(GameTestHelper helper) {
        ServerPlayer player = player(helper, "ammo-saving-enchantments");
        var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (var saved : java.util.List.of(Enchantments.powderless, net.minecraft.world.item.enchantment.Enchantments.INFINITY)) {
            ItemStack weapon = weapon(player);
            weapon.enchant(registry.getHolderOrThrow(Enchantments.rapid_reload), 1);
            weapon.enchant(registry.getHolderOrThrow(saved), 1);
            helper.assertTrue(weapon.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.SUCCESS
                    && MusketItem.isLoaded(weapon), "Ammunition-saving enchantment no longer permits the intended free reload");
            helper.assertTrue(player.getInventory().getItem(1).isEmpty() && weapon.getDamageValue() == 0,
                    "Ammunition-saving reload invented cartridges or damaged the weapon");
        }
        helper.succeed();
    }

    @GameTest(batch = "basesteam_musket", template = "basesteam_empty")
    public static void steelRepairUsesTheBoundCommonMaterialTag(GameTestHelper helper) {
        ItemStack weapon = new ItemStack(Items.blackpowder_musket);
        var steel = helper.getLevel().registryAccess().registryOrThrow(Registries.ITEM)
                .getTag(ItemTags.create(ResourceLocation.parse("c:ingots/steel"))).orElseThrow();
        helper.assertTrue(steel.size() > 0, "The common steel-ingot integration tag is empty");
        for (var holder : steel) helper.assertTrue(weapon.getItem().isValidRepairItem(weapon, new ItemStack(holder.value())),
                "Musket rejected a bound common steel ingot for repair");
        helper.assertTrue(!weapon.getItem().isValidRepairItem(weapon, new ItemStack(net.minecraft.world.item.Items.STONE)),
                "Musket accepted an unrelated repair material");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(
                UUID.nameUUIDFromBytes(("SteamMusketGameTest/" + name).getBytes(StandardCharsets.UTF_8)), "SteamMusketTest"));
        player.stopUsingItem();
        player.getInventory().clearContent();
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return player;
    }

    private static ItemStack weapon(ServerPlayer player) {
        player.stopUsingItem();
        player.getInventory().clearContent();
        ItemStack weapon = new ItemStack(Items.blackpowder_musket);
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        return weapon;
    }
}
