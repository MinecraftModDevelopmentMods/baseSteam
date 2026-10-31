package zone.moddev.mc.steamadvantage.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class SoundHelper {

	public static void playSoundAtTileEntity(SoundEvent sound, SoundSource soundType, float volume, float pitch, BlockEntity te){
		final BlockPos pos = te.getBlockPos();
		final Vec3 center = new Vec3(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5);
		playSoundAtPosition(center.x, center.y, center.z, sound, soundType, volume, pitch, te.getLevel());
	}

	public static void playSoundAtPosition(double x, double y, double z, SoundEvent sound, SoundSource soundType, float volume, float pitch, Level serverWorld){
		if(serverWorld.isClientSide) return;
		final double range = 16;
		final AABB area = new AABB(
				x - range, y - range, z - range,
				x + range, y + range, z + range);
		for(Player candidate : serverWorld.players()){
			if(!(candidate instanceof ServerPlayer) || !EntitySelector.NO_SPECTATORS.test(candidate)
					|| !area.intersects(candidate.getBoundingBox())) continue;
			ServerPlayer player = (ServerPlayer) candidate;
			if(player.connection != null) player.connection.send(new ClientboundCustomSoundPacket(sound.getRegistryName(), soundType,
					new Vec3(x, y, z), volume, pitch));
		}
	}

	public static void playBigSoundAtPosition(double x, double y, double z, SoundEvent sound, SoundSource soundType, float volume, float pitch, Level serverWorld){
		if(serverWorld.isClientSide) return;
		final double range = 50;
		final AABB area = new AABB(
				x - range, y - range, z - range,
				x + range, y + range, z + range);
		for(Player candidate : serverWorld.players()){
			if(!(candidate instanceof ServerPlayer) || !EntitySelector.NO_SPECTATORS.test(candidate)
					|| !area.intersects(candidate.getBoundingBox())) continue;
			ServerPlayer player = (ServerPlayer) candidate;
			if(player.connection != null) player.connection.send(new ClientboundCustomSoundPacket(sound.getRegistryName(), soundType,
					new Vec3(x, y, z), volume, pitch));
		}
	}
}
