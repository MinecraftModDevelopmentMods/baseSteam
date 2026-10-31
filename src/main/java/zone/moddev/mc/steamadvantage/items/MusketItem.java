package zone.moddev.mc.steamadvantage.items;

import static zone.moddev.mc.steamadvantage.util.SoundHelper.playBigSoundAtPosition;
import static zone.moddev.mc.steamadvantage.util.SoundHelper.playSoundAtPosition;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import zone.moddev.mc.steamadvantage.SteamAdvantage;
import zone.moddev.mc.steamadvantage.init.Enchantments;
import zone.moddev.mc.steamadvantage.init.Power;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.tags.ItemTags;

@SuppressWarnings("deprecation")
public class MusketItem extends net.minecraft.world.item.Item{
	
	
	public static final String NBT_DATA_KEY_LOADED = "loaded";
	public static final double MAX_RANGE = 64;
	private static final int maxUseTime = 7200;

	private static final ResourceLocation BLACKPOWDER_AMMO_TAG = new ResourceLocation("forge", "ammo/blackpowder");
	private static final ResourceLocation STEEL_INGOT_TAG = new ResourceLocation("forge", "ingots/steel");

	public MusketItem(Item.Properties properties){
		super(properties.defaultDurability(250));
	}
	

	public static int getReloadTime(){
		return SteamAdvantage.MUSKET_RELOAD;
	}
	public static float getShotDamage(){
		return SteamAdvantage.MUSKET_DAMAGE;
	}
	public static float getMeleeDamage(){
		return 3;
	}
	
	@Override
	public int getEnchantmentValue(){
		return 1;
	}
	
	@Override
	public UseAnim getUseAnimation(ItemStack stack){
		if(isLoaded(stack)){
			return UseAnim.BOW;
		} else {
			return UseAnim.BLOCK;
		}
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player playerEntity, InteractionHand hand){
		ItemStack srcItemStack = playerEntity.getItemInHand(hand);
		if(hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(srcItemStack);
		if(isNotLoaded(srcItemStack) && !hasAmmo(playerEntity, srcItemStack)) return InteractionResultHolder.fail(srcItemStack);
		if(isNotLoaded(srcItemStack) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.rapid_reload, srcItemStack) > 0){
			// instant reload
			decrementAmmo(playerEntity,srcItemStack);
			startLoad(srcItemStack);
			playSound(SoundEvents.LEVER_CLICK,world,playerEntity);
			finishLoad(srcItemStack);
			return InteractionResultHolder.success(srcItemStack);
		}
		playerEntity.startUsingItem(hand);
		return InteractionResultHolder.success(srcItemStack);
	}
	
	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count){
		if(player.level.isClientSide && count % 7 == 3 && isNotLoaded(stack)){
			// indicator to player that the gun is loading
			player.playSound(SoundType.STONE.getStepSound(), 0.5f, 1.0f);
		}
	}
	/**
	 * This method is invoked after the item has been used for an amount of time equal to the duration 
	 * provided to the PlayerEntity.setItemInUse(stack, duration).
	 */
	@Override
	public ItemStack finishUsingItem (ItemStack srcItemStack, Level world, LivingEntity playerEntity)
	{ // 
		if(isNotLoaded(srcItemStack) && hasAmmo(playerEntity,srcItemStack)){
			decrementAmmo(playerEntity,srcItemStack);
			startLoad(srcItemStack);
			playSound(SoundEvents.LEVER_CLICK,world,playerEntity);
		}
		return srcItemStack;
	}
	
	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity playerIn, int timeLeft) {
		if(isAlmostLoaded(stack)){
			finishLoad(stack);
		} else if(isLoaded(stack)){
			fire(stack,playerIn,worldIn);
		}
	}
	
	protected void fire(ItemStack srcItemStack, LivingEntity playerEntity,
			Level world) {
		final boolean isPlayer = playerEntity instanceof Player;
		unload(srcItemStack);
		if(isPlayer && !((Player)playerEntity).isCreative()){
			srcItemStack.hurtAndBreak(1, playerEntity, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		}
		Vec3 lookVector = playerEntity.getLookAngle();
		spawnParticle(ParticleTypes.EXPLOSION_EMITTER, world,
				playerEntity.getX()+lookVector.x, playerEntity.getY()+playerEntity.getEyeHeight()+lookVector.y, playerEntity.getZ() + lookVector.z);
		if(world.isClientSide){
			playerEntity.setXRot(playerEntity.getXRot() - 15);
			return;
		}
		playSound(SoundEvents.FLINTANDSTEEL_USE,world,playerEntity);
		playBigSoundAtPosition(playerEntity.getX(),playerEntity.getY(),playerEntity.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST,SoundSource.PLAYERS,2F,0.5F,world);
		
		Vec3 start = new Vec3(playerEntity.getX(), playerEntity.getY()+playerEntity.getEyeHeight(),playerEntity.getZ());
		start.add(MAX_RANGE * lookVector.x, MAX_RANGE * lookVector.y, MAX_RANGE * lookVector.z);
		HitResult rayTrace = rayTraceBlocksAndEntities(world,MAX_RANGE,playerEntity);
		if(rayTrace == null){
			// no collisions
			return;
		}
		final float explodeFactor = 0.75f;
		if(rayTrace instanceof EntityHitResult){
			Entity e = ((EntityHitResult) rayTrace).getEntity();
			e.hurt(Power.musket_damage, getShotDamage());
			if(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.high_explosive, srcItemStack) > 0){
				int lvl = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.high_explosive, srcItemStack);
				world.explode(playerEntity, e.getX(), e.getY()+0.5, e.getZ(),
						explodeFactor * lvl, 
						Explosion.BlockInteraction.BREAK);
				AABB fireArea = new AABB(e.getX()-lvl, e.getY()-lvl, e.getZ()-lvl,
						e.getX()+lvl, e.getY()+lvl, e.getZ()+lvl);
				List<LivingEntity> collateralDamage = world.getEntitiesOfClass(LivingEntity.class, fireArea);
				for(LivingEntity victim : collateralDamage){
					victim.setSecondsOnFire(lvl);
				}
			}
		} else if(rayTrace.getType() == HitResult.Type.BLOCK){
			playSoundAtPosition(rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z,
					SoundEvents.GRAVEL_HIT, SoundSource.BLOCKS, 1f, 1f, world);
			BlockPos coord = ((BlockHitResult) rayTrace).getBlockPos();
			if(!world.isOutsideBuildHeight(coord) && world.hasChunkAt(coord) && !world.isEmptyBlock(coord)){
				BlockState bs = world.getBlockState(coord);
				Material blockMat = bs.getMaterial();
				if(blockMat == net.minecraft.world.level.material.Material.GLASS
						|| blockMat == net.minecraft.world.level.material.Material.LEAVES
						|| blockMat == net.minecraft.world.level.material.Material.DECORATION
						|| blockMat == net.minecraft.world.level.material.Material.PLANT
						|| blockMat == net.minecraft.world.level.material.Material.REPLACEABLE_PLANT
						|| blockMat == net.minecraft.world.level.material.Material.WEB){
					world.destroyBlock(coord, true);
				} else if (blockMat == net.minecraft.world.level.material.Material.EXPLOSIVE){
					if(world.getBlockState(coord).getBlock() instanceof TntBlock){
						TntBlock.explode(world, coord);
						world.removeBlock(coord, false);
					}
				}
			}
			if(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.high_explosive, srcItemStack) > 0){
				int lvl = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.high_explosive, srcItemStack);
				world.explode(playerEntity, rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z,
						explodeFactor * lvl, 
						Explosion.BlockInteraction.BREAK);
				AABB fireArea = new AABB(rayTrace.getLocation().x-lvl, rayTrace.getLocation().y-lvl, rayTrace.getLocation().z-lvl,
						rayTrace.getLocation().x+lvl, rayTrace.getLocation().y+lvl, rayTrace.getLocation().z+lvl);
				List<LivingEntity> collateralDamage = world.getEntitiesOfClass(LivingEntity.class, fireArea);
				for(LivingEntity victim : collateralDamage){
					victim.setSecondsOnFire(lvl);
				}
			}
		}
		if(rayTrace.getType() != HitResult.Type.MISS){
			spawnParticle(ParticleTypes.LARGE_SMOKE, world,
					rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z);
			spawnParticle(ParticleTypes.EXPLOSION, world,
					rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z);
		}
		
		// recoil enchantment kicks you back
		int recoil = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.recoil, srcItemStack);
		if(recoil > 0){
			double c = -0.3;
			double lateral = 2.0;
			playerEntity.push(c * recoil * lookVector.x * lateral,
					c * recoil * lookVector.y, 
					c * recoil * lookVector.z * lateral);
			if(playerEntity instanceof ServerPlayer){
				// send update packet from server
				((ServerPlayer) playerEntity).connection.send(new ClientboundSetEntityMotionPacket(playerEntity));
			}
		}
	}


	private void spawnParticle(ParticleOptions p, Level w, double x, double y, double z) {
		if(w instanceof ServerLevel) {
			((ServerLevel) w).sendParticles(p, x, y, z, 1, 0, 0, 0, 0);
		} else {
			w.addParticle(p, x, y, z, 0, 0, 0);
		}
	}
	
	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
	{
		stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		return true;
	}
	
	/** plays a sound at the player location */
	protected void playSound(SoundEvent sound, Level world, Entity e){
		if (!world.isClientSide)
		{
			playSoundAtPosition(e.getX(), e.getY(), e.getZ(), sound,SoundSource.PLAYERS,1F,1F,world);
		}
	}
	
	public static void startLoad(ItemStack musket){
		CompoundTag data;
		if(musket.hasTag()){
			data = musket.getTag();
		} else {
			data = new CompoundTag();
		}
		data.putByte(NBT_DATA_KEY_LOADED, (byte)2);
		musket.setTag(data);
	}
	
	public static void finishLoad(ItemStack musket){
		CompoundTag data;
		if(musket.hasTag()){
			data = musket.getTag();
		} else {
			data = new CompoundTag();
		}
		data.putByte(NBT_DATA_KEY_LOADED, (byte)1);
		musket.setTag(data);
	}
	
	public static void unload(ItemStack musket){
		CompoundTag data;
		if(musket.hasTag()){
			data = musket.getTag();
		} else {
			data = new CompoundTag();
		}
		data.putByte(NBT_DATA_KEY_LOADED, (byte)0);
		musket.setTag(data);
	}
	
	public static boolean isLoaded(ItemStack musket){
		CompoundTag data = musket.getTag();
		if(data == null) return false;
		return data.contains(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 1;
	}
	
	public static boolean isNotLoaded(ItemStack musket){
		CompoundTag data = musket.getTag();
		if(data == null || !data.contains(NBT_DATA_KEY_LOADED)) return true;
		return data.contains(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 0;
	}
	
	public static boolean isAlmostLoaded(ItemStack musket){
		CompoundTag data = musket.getTag();
		if(data == null) return false;
		return data.contains(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 2;
	}


	
	public static boolean hasAmmo(LivingEntity e, ItemStack musket) {
		if(e instanceof Player) {
			Player playerEntity = (Player) e;
			if (playerEntity.isCreative()
					|| EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.INFINITY_ARROWS, musket) > 0
					|| EnchantmentHelper.getItemEnchantmentLevel(Enchantments.powderless, musket) > 0) return true;
			for (int i = 0; i < playerEntity.getInventory().items.size(); i++) {
				ItemStack inventoryStack = playerEntity.getInventory().items.get(i);
				if (inventoryStack.isEmpty()) continue;
				if (ItemTags.getAllTags().getTagOrEmpty(BLACKPOWDER_AMMO_TAG).contains(inventoryStack.getItem())) return true;
			}
		}
		return false;
	}
	

	public static void decrementAmmo(LivingEntity e, ItemStack musket) {
		if(e instanceof Player) {
			Player playerEntity = (Player) e;
			if (playerEntity.isCreative()
					|| EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.INFINITY_ARROWS, musket) > 0
					|| EnchantmentHelper.getItemEnchantmentLevel(Enchantments.powderless, musket) > 0) return;
			for (int i = 0; i < playerEntity.getInventory().items.size(); i++) {
				ItemStack inventoryStack = playerEntity.getInventory().items.get(i);
				if (inventoryStack.isEmpty()) continue;
				if (ItemTags.getAllTags().getTagOrEmpty(BLACKPOWDER_AMMO_TAG).contains(inventoryStack.getItem())) {
					inventoryStack.shrink(1);
					if (inventoryStack.isEmpty()) playerEntity.getInventory().items.set(i, ItemStack.EMPTY);
					return;
				}
			}
		}
	}
	
	
	public static HitResult rayTraceBlocksAndEntities(Level w, double maxRange, LivingEntity source){
		Vec3 rayOrigin = new Vec3(source.getX(), source.getY() + source.getEyeHeight(), source.getZ());
		Vec3 rayDirection = source.getLookAngle();
		Vec3 loadedEnd = loadedRayEnd(rayOrigin, rayOrigin.add(rayDirection.scale(maxRange)), w::hasChunkAt);
		if(loadedEnd.equals(rayOrigin)) return BlockHitResult.miss(rayOrigin,
				net.minecraft.core.Direction.getNearest(rayDirection.x, rayDirection.y, rayDirection.z), new BlockPos(rayOrigin));
		BlockHitResult terrainHit = w.clip(new ClipContext(rayOrigin, loadedEnd,
				ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, source));
		Vec3 rayEnd = terrainHit.getLocation();
		AABB aoi = new AABB(rayOrigin, rayEnd).inflate(1.0D);
		// Server spatial entity queries can synchronously generate chunks around the shooter.
		Iterable<Entity> entities = w instanceof ServerLevel ? ((ServerLevel)w).getAllEntities() : w.getEntities(source, aoi);
		double closestDistSqr = rayOrigin.distanceToSqr(rayEnd);
		Entity closestEntity = null;
		Vec3 closestHit = null;
		for(Entity e : entities){
			if(e == source || e.isRemoved() || e.isSpectator() || !aoi.intersects(e.getBoundingBox())) continue;
			Optional<Vec3> hit = rayHitBeforeTerrain(rayOrigin, rayEnd, e.getBoundingBox());
			if(hit.isPresent()){
				double distSqr = rayOrigin.distanceToSqr(hit.get());
				if(distSqr < closestDistSqr){
					closestDistSqr = distSqr;
					closestEntity = e;
					closestHit = hit.get();
				}
			}
		}
		return closestEntity == null ? terrainHit : new EntityHitResult(closestEntity, closestHit);
	}

	static Vec3 loadedRayEnd(Vec3 origin, Vec3 end, java.util.function.Predicate<BlockPos> loaded) {
		if(!loaded.test(new BlockPos(origin))) return origin;
		// Reuse native voxel traversal so even a grazing diagonal cannot enter an unloaded chunk.
		Vec3 boundary = net.minecraft.world.level.BlockGetter.traverseBlocks(origin, end, loaded,
				(test, pos) -> test.test(pos) ? null : new AABB(pos).clip(origin, end).orElse(origin),
				test -> end);
		if(boundary.equals(end)) return end;
		return origin.distanceToSqr(boundary) <= 0.00000001D ? origin
				: boundary.subtract(end.subtract(origin).normalize().scale(0.0001D));
	}

	static Optional<Vec3> rayHitBeforeTerrain(Vec3 origin, Vec3 terrainEnd, AABB target){
		Optional<Vec3> hit = target.contains(origin) ? Optional.of(origin) : target.clip(origin, terrainEnd);
		return hit.filter(point -> origin.distanceToSqr(point) < origin.distanceToSqr(terrainEnd));
	}

	public static boolean rayIntersectsBoundingBox(Vec3 rayOrigin, Vec3 rayDirection, AABB box){
		if(box == null) return false;
		// algorithm from http://gamedev.stackexchange.com/questions/18436/most-efficient-aabb-vs-ray-collision-algorithms
		Vec3 inverse = new Vec3(1.0 / rayDirection.x, 1.0 / rayDirection.y, 1.0 / rayDirection.z);
		double t1 = (box.minX - rayOrigin.x)*inverse.x;
		double t2 = (box.maxX- rayOrigin.x)*inverse.x;
		double t3 = (box.minY - rayOrigin.y)*inverse.y;
		double t4 = (box.maxY - rayOrigin.y)*inverse.y;
		double t5 = (box.minZ - rayOrigin.z)*inverse.z;
		double t6 = (box.maxZ - rayOrigin.z)*inverse.z;

		double tmin = max(max(min(t1, t2), min(t3, t4)), min(t5, t6));
		double tmax = min(min(max(t1, t2), max(t3, t4)), max(t5, t6));

		// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
		if (tmax < 0)
		{
			return false;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tmin > tmax)
		{
			return false;
		}

		return true;
	}
	
	
	private static double max(double a, double b){
		return Math.max(a, b);
	}
	private static double min(double a, double b){
		return Math.min(a, b);
	}
	
	/**
	 * Return whether this item is repairable in an anvil.
	 */
	@Override public boolean isValidRepairItem(ItemStack srcItemStack, ItemStack rawMaterial)
	{
		// repair with steel ingots
		return ItemTags.getAllTags().getTagOrEmpty(STEEL_INGOT_TAG).contains(rawMaterial.getItem());
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> list, net.minecraft.world.item.TooltipFlag flag){
		super.appendHoverText(stack,world,list,flag);
		list.add(new TranslatableComponent("tooltip.musket.damage", (int)MusketItem.getShotDamage()));
		if(isLoaded(stack)){
			list.add(new TranslatableComponent("tooltip.musket.loaded"));
		} else {
			list.add(new TranslatableComponent("tooltip.musket.unloaded"));
			list.add(new TranslatableComponent("tooltip.musket.ammo", new TranslatableComponent("item.steamadvantage.blackpowder_cartridge")));
		}
	}

	/** Sets melee attack damage */
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
	{
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
		if(slot != EquipmentSlot.MAINHAND) return multimap;
		multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)getMeleeDamage(), AttributeModifier.Operation.ADDITION));
		return multimap;
	}
	
	@Override
	public int getUseDuration(ItemStack stack)
	{
		if(isLoaded(stack)){
			return maxUseTime;
		} else {
			return getReloadTime();
		}
	}
}
