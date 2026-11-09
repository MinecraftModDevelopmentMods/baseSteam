package zone.moddev.mc.basesteam.enchantments;

import zone.moddev.mc.basesteam.init.Items;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class RapidReloadEnchantment extends net.minecraft.world.item.enchantment.Enchantment{

	public RapidReloadEnchantment() {
		super(Rarity.RARE,EnchantmentCategory.BREAKABLE,new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
		return 15;
	}

	@Override
	public int getMaxCost(final int lvl) {
		return 50;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}
}
