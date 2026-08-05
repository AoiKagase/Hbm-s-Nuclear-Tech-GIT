package com.hbm.sound;

import com.hbm.tileentity.TileEntityLoadedBase;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class SoundLoopMachine extends PositionedSound implements ITickableSound {
	private static final Map<SoundKey, SoundLoopMachine> activeSounds = new HashMap<SoundKey, SoundLoopMachine>();
	boolean donePlaying = false;
	public TileEntity te;
    int targetVolume;
	private final SoundKey soundKey;

	public SoundLoopMachine(SoundEvent path, TileEntity te, int volume) {
		super(path, SoundCategory.BLOCKS);
		this.repeat = true;
        this.targetVolume = volume;
		this.volume = 1;
		this.pitch = 1;
		this.xPosF = te.getPos().getX();
		this.yPosF = te.getPos().getY();
		this.zPosF = te.getPos().getZ();
		this.repeatDelay = 0;
		this.te = te;
		this.soundKey = new SoundKey(te, getClass());
		activeSounds.put(soundKey, this);
	}

	public static boolean isActive(TileEntity te, Class<? extends SoundLoopMachine> soundType) {
		return getActive(te, soundType) != null;
	}

	public static <T extends SoundLoopMachine> T getActive(TileEntity te, Class<T> soundType) {
		if(te == null || te.getWorld() == null || te.getWorld().provider == null)
			return null;

		SoundKey key = new SoundKey(te, soundType);
		SoundLoopMachine sound = activeSounds.get(key);
		if(sound != null && sound.isDonePlaying()) {
			activeSounds.remove(key, sound);
			sound = null;
		}
		return sound == null ? null : soundType.cast(sound);
	}

    public static boolean isLoaded(TileEntity te){
        if(te instanceof TileEntityLoadedBase base) return base.isLoaded();
        return true;
    }

    public static boolean canPlay(TileEntity te){
        return !(te == null || te.isInvalid() || !te.getWorld().isBlockLoaded(te.getPos()) || !isLoaded(te));
    }

    public boolean isThisProcessing(){
        return true;
    }

	@Override
	public void update() {
		if(!canPlay(te) || !isThisProcessing() || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this)) {
            stop();
        } else if(this.volume != this.targetVolume) {
            this.volume = this.targetVolume;
        }
    }

	@Override
	public boolean isDonePlaying() {
		return this.donePlaying;
	}
	
	public void setVolume(float f) {
		volume = f;
	}
	
	public void setPitch(float f) {
		pitch = f;
	}
	
	public void stop() {
		if(!donePlaying) {
			donePlaying = true;
			activeSounds.remove(soundKey, this);
		}
	}

	private static class SoundKey {
		private final int dimension;
		private final BlockPos pos;
		private final Class<?> soundType;

		private SoundKey(TileEntity te, Class<?> soundType) {
			this.dimension = te.getWorld().provider.getDimension();
			this.pos = te.getPos();
			this.soundType = soundType;
		}

		@Override
		public int hashCode() {
			int result = dimension;
			result = 31 * result + pos.hashCode();
			result = 31 * result + soundType.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(!(obj instanceof SoundKey))
				return false;
			SoundKey other = (SoundKey) obj;
			return dimension == other.dimension && pos.equals(other.pos) && soundType == other.soundType;
		}
	}
}
