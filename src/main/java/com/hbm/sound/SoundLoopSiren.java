package com.hbm.sound;

import com.hbm.items.machine.ItemCassette.SoundType;
import com.hbm.tileentity.machine.TileEntityMachineSiren;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;

public class SoundLoopSiren extends SoundLoopMachine {

	public float intendedVolume;
	public SoundType type;

	public SoundLoopSiren(SoundEvent path, TileEntity te, SoundType type) {
		super(path, te, 1);
		intendedVolume = 10.0F;
		this.attenuationType = ISound.AttenuationType.NONE;
		this.type = type;
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
		} else {
			volume = intendedVolume;
		}
		
		if(te instanceof TileEntityMachineSiren) {
			this.setRepeat(type.name().equals(SoundType.LOOP.name()));
		} else {
			this.stop();
		}
	}
	
	public TileEntity getTE() {
		return te;
	}

	public String getPath() {
		return this.positionedSoundLocation.getNamespace() + ":" + this.positionedSoundLocation.getPath();
	}
	
	public void setRepeat(boolean b) {
		this.repeat = b;
	}

	public void setRepeatDelay(int i) {
		this.repeatDelay = i;
	}
	
	public float func(float f, float v) {
		return (f / v) * -2 + 2;
	}
}
