package com.hbm.tileentity.machine;

import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.TEStructurePacket;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityStructureMarker extends TileEntity implements ITickable {
	private static final int CLIENT_SYNC_INTERVAL = 5;
	private static final int FULL_SYNC_INTERVAL = 20;

	// 0: Factory
	// 1: Nuclear Reactor
	// 2: Reactor with Coat
	// 3: Watz Power Plant
	// 4: Singularity-Anti-Fusion-Experiment
	public int type = 0;
	private long lastClientSyncTick = -1;
	private int lastSyncedType = Integer.MIN_VALUE;

	@Override
	public void update() {
		if(this.type > 3)
			type = 0;

		if(!world.isRemote)
			syncClientState();
	}

	private void syncClientState() {
		long time = world.getTotalWorldTime();
		boolean changed = type != lastSyncedType;
		if(lastClientSyncTick >= 0 && time - lastClientSyncTick < CLIENT_SYNC_INTERVAL)
			return;
		if(!changed && lastClientSyncTick >= 0 && time - lastClientSyncTick < FULL_SYNC_INTERVAL)
			return;

		PacketDispatcher.wrapper.sendToAllAround(new TEStructurePacket(pos.getX(), pos.getY(), pos.getZ(), type), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 80));
		lastClientSyncTick = time;
		lastSyncedType = type;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		type = compound.getInteger("type");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("type", type);
		return super.writeToNBT(compound);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

}
