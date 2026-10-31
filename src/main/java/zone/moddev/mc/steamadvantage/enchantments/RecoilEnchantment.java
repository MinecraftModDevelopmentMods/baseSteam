package zone.moddev.mc.steamadvantage.enchantments;

import zone.moddev.mc.steamadvantage.init.Items;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class RecoilEnchantment extends net.minecraft.world.item.enchantment.Enchantment{

	public RecoilEnchantment() {
		super(Rarity.COMMON,EnchantmentCategory.BREAKABLE,new EquipmentSlot[]{EquipmentSlot.MAINHAND});
	}

	
	@Override
	public boolean canEnchant(ItemStack item){
		return item.getItem() == Items.blackpowder_musket;
	}
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack item){
		return canEnchant(item);
	}
	


	@Override
	public int getMinCost(final int lvl) {
		return Math.max(1,lvl * 5 - 3);
	}

	@Override
	public int getMaxCost(final int lvl) {
		return this.getMinCost(lvl) + 10;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}
}
