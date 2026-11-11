package zone.moddev.mc.basesteam.items;

import static zone.moddev.mc.basesteam.util.SoundHelper.playBigSoundAtPosition;
import static zone.moddev.mc.basesteam.util.SoundHelper.playSoundAtPosition;

import java.util.List;
import java.util.Optional;

import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.init.Enchantments;
import zone.moddev.mc.basesteam.init.Power;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;

@SuppressWarnings("deprecation")
public class MusketItem extends net.minecraft.world.item.Item{
	private static final TagKey<Block> FRAGILE_TARGETS = TagKey.create(Registries.BLOCK,
			Identifier.fromNamespaceAndPath(SteamAdvantage.MODID, "musket_fragile"));
	
	
	public static final String NBT_DATA_KEY_LOADED = "loaded";
	public static final double MAX_RANGE = 64;
	private static final int maxUseTime = 7200;

	private static final TagKey<Item> BLACKPOWDER_AMMO_TAG = ItemTags.create(Identifier.fromNamespaceAndPath("forge", "ammo/blackpowder"));
	private static final TagKey<Item> STEEL_INGOT_TAG = ItemTags.create(Identifier.fromNamespaceAndPath("forge", "ingots/steel"));

	public MusketItem(Item.Properties properties){
		super(properties.durability(250).enchantable(1).repairable(STEEL_INGOT_TAG).attributes(ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID,
						getMeleeDamage(), AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.build()));
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
	public ItemUseAnimation getUseAnimation(ItemStack stack){
		if(isLoaded(stack)){
			return ItemUseAnimation.BOW;
		} else {
			return ItemUseAnimation.BLOCK;
		}
	}
	
	@Override
	public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
		ItemStack srcItemStack = playerEntity.getItemInHand(hand);
		if(hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
		if(isNotLoaded(srcItemStack) && !hasAmmo(playerEntity, srcItemStack)) return InteractionResult.FAIL;
		if(isNotLoaded(srcItemStack) && Enchantments.level(Enchantments.rapid_reload, srcItemStack, world.registryAccess()) > 0){
			// instant reload
			decrementAmmo(playerEntity,srcItemStack);
			startLoad(srcItemStack);
			playSound(SoundEvents.LEVER_CLICK,world,playerEntity);
			finishLoad(srcItemStack);
			return InteractionResult.SUCCESS;
		}
		playerEntity.startUsingItem(hand);
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count){
		if(level.isClientSide() && count % 7 == 3 && isNotLoaded(stack)){
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
	public boolean releaseUsing(ItemStack stack, Level worldIn, LivingEntity playerIn, int timeLeft) {
		if(isAlmostLoaded(stack)){
			finishLoad(stack);
		} else if(isLoaded(stack)){
			fire(stack,playerIn,worldIn);
		}
		return true;
	}
	
	protected void fire(ItemStack srcItemStack, LivingEntity playerEntity,
			Level world) {
		final boolean isPlayer = playerEntity instanceof Player;
		int explosive = Enchantments.level(Enchantments.high_explosive, srcItemStack, world.registryAccess());
		int recoil = Enchantments.level(Enchantments.recoil, srcItemStack, world.registryAccess());
		unload(srcItemStack);
		if(isPlayer && !((Player)playerEntity).isCreative()){
			srcItemStack.hurtAndBreak(1, playerEntity, EquipmentSlot.MAINHAND);
		}
		Vec3 lookVector = playerEntity.getLookAngle();
		spawnParticle(ParticleTypes.EXPLOSION_EMITTER, world,
				playerEntity.getX()+lookVector.x, playerEntity.getY()+playerEntity.getEyeHeight()+lookVector.y, playerEntity.getZ() + lookVector.z);
		if(world.isClientSide()){
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
		// TNT keeps the old BREAK behavior by default; BLOCK now decays drops by default.
		final Level.ExplosionInteraction explosionInteraction = Level.ExplosionInteraction.TNT;
		if(rayTrace instanceof EntityHitResult){
			Entity e = ((EntityHitResult) rayTrace).getEntity();
			e.hurtServer((ServerLevel)world, Power.musketDamage(world), getShotDamage());
			if(explosive > 0){
				int lvl = explosive;
				world.explode(playerEntity, e.getX(), e.getY()+0.5, e.getZ(),
						explodeFactor * lvl, 
						explosionInteraction);
				AABB fireArea = new AABB(e.getX()-lvl, e.getY()-lvl, e.getZ()-lvl,
						e.getX()+lvl, e.getY()+lvl, e.getZ()+lvl);
				List<LivingEntity> collateralDamage = world.getEntitiesOfClass(LivingEntity.class, fireArea);
				for(LivingEntity victim : collateralDamage){
					victim.igniteForSeconds(lvl);
				}
			}
		} else if(rayTrace.getType() == HitResult.Type.BLOCK){
			playSoundAtPosition(rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z,
					SoundEvents.GRAVEL_HIT, SoundSource.BLOCKS, 1f, 1f, world);
			BlockPos coord = ((BlockHitResult) rayTrace).getBlockPos();
			if(!world.isOutsideBuildHeight(coord) && world.hasChunkAt(coord) && !world.isEmptyBlock(coord)){
				BlockState bs = world.getBlockState(coord);
				if (bs.is(FRAGILE_TARGETS)) {
					world.destroyBlock(coord, true);
				} else if (bs.getBlock() instanceof TntBlock) {
					if (bs.getBlock().onCaughtFire(bs, world, coord, null, null)) {
						world.removeBlock(coord, false);
					}
				}
			}
			if(explosive > 0){
				int lvl = explosive;
				world.explode(playerEntity, rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z,
						explodeFactor * lvl, 
						explosionInteraction);
				AABB fireArea = new AABB(rayTrace.getLocation().x-lvl, rayTrace.getLocation().y-lvl, rayTrace.getLocation().z-lvl,
						rayTrace.getLocation().x+lvl, rayTrace.getLocation().y+lvl, rayTrace.getLocation().z+lvl);
				List<LivingEntity> collateralDamage = world.getEntitiesOfClass(LivingEntity.class, fireArea);
				for(LivingEntity victim : collateralDamage){
					victim.igniteForSeconds(lvl);
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
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
	{
		stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
	}
	
	/** plays a sound at the player location */
	protected void playSound(SoundEvent sound, Level world, Entity e){
		if (!world.isClientSide())
		{
			playSoundAtPosition(e.getX(), e.getY(), e.getZ(), sound,SoundSource.PLAYERS,1F,1F,world);
		}
	}
	
	public static void startLoad(ItemStack musket){
		setLoadedState(musket, (byte)2);
	}
	
	public static void finishLoad(ItemStack musket){
		setLoadedState(musket, (byte)1);
	}
	
	public static void unload(ItemStack musket){
		setLoadedState(musket, (byte)0);
	}

	private static void setLoadedState(ItemStack musket, byte state) {
		CustomData.update(DataComponents.CUSTOM_DATA, musket, tag -> tag.putByte(NBT_DATA_KEY_LOADED, state));
	}

	private static byte loadedState(ItemStack musket) {
		CustomData data = musket.get(DataComponents.CUSTOM_DATA);
		return data == null ? 0 : data.copyTag().getByteOr(NBT_DATA_KEY_LOADED, (byte)0);
	}
	
	public static boolean isLoaded(ItemStack musket){
		return loadedState(musket) == 1;
	}
	
	public static boolean isNotLoaded(ItemStack musket){
		byte state = loadedState(musket);
		return state != 1 && state != 2;
	}
	
	public static boolean isAlmostLoaded(ItemStack musket){
		return loadedState(musket) == 2;
	}


	
	public static boolean hasAmmo(LivingEntity e, ItemStack musket) {
		if(e instanceof Player) {
			Player playerEntity = (Player) e;
			if (playerEntity.isCreative()
					|| Enchantments.level(net.minecraft.world.item.enchantment.Enchantments.INFINITY, musket, e.registryAccess()) > 0
					|| Enchantments.level(Enchantments.powderless, musket, e.registryAccess()) > 0) return true;
			for (int i = 0; i < playerEntity.getInventory().getNonEquipmentItems().size(); i++) {
				ItemStack inventoryStack = playerEntity.getInventory().getItem(i);
				if (inventoryStack.isEmpty()) continue;
				if (inventoryStack.is(BLACKPOWDER_AMMO_TAG)) return true;
			}
		}
		return false;
	}
	

	public static void decrementAmmo(LivingEntity e, ItemStack musket) {
		if(e instanceof Player) {
			Player playerEntity = (Player) e;
			if (playerEntity.isCreative()
					|| Enchantments.level(net.minecraft.world.item.enchantment.Enchantments.INFINITY, musket, e.registryAccess()) > 0
					|| Enchantments.level(Enchantments.powderless, musket, e.registryAccess()) > 0) return;
			for (int i = 0; i < playerEntity.getInventory().getNonEquipmentItems().size(); i++) {
				ItemStack inventoryStack = playerEntity.getInventory().getItem(i);
				if (inventoryStack.isEmpty()) continue;
				if (inventoryStack.is(BLACKPOWDER_AMMO_TAG)) {
					inventoryStack.shrink(1);
					if (inventoryStack.isEmpty()) playerEntity.getInventory().setItem(i, ItemStack.EMPTY);
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
				net.minecraft.core.Direction.getApproximateNearest(rayDirection.x, rayDirection.y, rayDirection.z), BlockPos.containing(rayOrigin));
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
		if(!loaded.test(BlockPos.containing(origin))) return origin;
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
	
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> list,
            net.minecraft.world.item.TooltipFlag flag){
		super.appendHoverText(stack,context,display,list,flag);
		list.accept(Component.translatable("tooltip.musket.damage", (int)MusketItem.getShotDamage()));
		if(isLoaded(stack)){
			list.accept(Component.translatable("tooltip.musket.loaded"));
		} else {
			list.accept(Component.translatable("tooltip.musket.unloaded"));
			list.accept(Component.translatable("tooltip.musket.ammo", Component.translatable("item.basesteam.blackpowder_cartridge")));
		}
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user)
	{
		if(isLoaded(stack)){
			return maxUseTime;
		} else {
			return getReloadTime();
		}
	}
}
