package com.hbm.tileentity;

import api.hbm.energy.ILoadedTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityLoadedBase extends TileEntity implements ILoadedTile {
	
	private static final int IDENTICAL_NETWORK_PACK_RESEND_INTERVAL = 20;

	public boolean isLoaded = false;

	private NBTTagCompound lastNetworkPack;
	private int lastNetworkPackRange;
	private long lastNetworkPackTick = Long.MIN_VALUE;
	
	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		this.isLoaded = false;
	}

    @Override
    public void onLoad() {
        super.onLoad();
        this.isLoaded = true;
    }

	protected boolean shouldSendNetworkPack(NBTTagCompound nbt, int range) {
		if(world == null || world.isRemote)
			return false;

		long time = world.getTotalWorldTime();
		if(lastNetworkPack != null && lastNetworkPackRange == range && time - lastNetworkPackTick < IDENTICAL_NETWORK_PACK_RESEND_INTERVAL && lastNetworkPack.equals(nbt))
			return false;

		lastNetworkPack = nbt.copy();
		lastNetworkPackRange = range;
		lastNetworkPackTick = time;
		return true;
	}
}
