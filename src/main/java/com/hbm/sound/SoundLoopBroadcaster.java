package com.hbm.sound;

import com.hbm.tileentity.machine.TileEntityBroadcaster;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class SoundLoopBroadcaster extends SoundLoopMachine {
	
	public float intendedVolume = 25.0F;

	public SoundLoopBroadcaster(SoundEvent path, TileEntity te) {
		super(path, te, 1);
		this.attenuationType = ISound.AttenuationType.NONE;
	}

	@Override
	public void update() {
		super.update();
		if(this.donePlaying) return;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		
		if(player != null) {
			double dx = xPosF - player.posX;
			double dy = yPosF - player.posY;
			double dz = zPosF - player.posZ;
			double distanceSquared = dx * dx + dy * dy + dz * dz;
			double rangeSquared = intendedVolume * intendedVolume;
			volume = distanceSquared >= rangeSquared ? 0 : func((float)Math.sqrt(distanceSquared), intendedVolume);
			
			if(!(player.world.getTileEntity(new BlockPos((int)xPosF, (int)yPosF, (int)zPosF)) instanceof TileEntityBroadcaster)) {
				this.stop();
				volume = 0;
			}
		} else {
			volume = intendedVolume;
		}
	}
	
	public float func(float f, float v) {
		return (f / v) * -2 + 2;
	}

}
