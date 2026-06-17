package com.hbm.tileentity.network.energy;

import com.hbm.lib.ForgeDirection;

import api.hbm.energy.IEnergyConductor;
import api.hbm.energy.IPowerNet;
import api.hbm.energy.PowerNet;
import net.minecraft.util.ITickable;
import net.minecraft.tileentity.TileEntity;

public class TileEntityCableBaseNT extends TileEntity implements ITickable, IEnergyConductor {
	
	private static final int NETWORK_BUILD_SPREAD_TICKS = 4;

	protected IPowerNet network;
	private boolean networkBuildScheduled;
	private long nextNetworkBuildTick = Long.MIN_VALUE;

	@Override
	public void update() {
		
		if(!world.isRemote && canUpdate()) {

			if(!canBuildNetworkThisTick())
				return;
			
			//we got here either because the net doesn't exist or because it's not valid, so that's safe to assume
			this.setPowerNet(null);
			
			this.connect();
			
			if(this.getPowerNet() == null) {
				this.setPowerNet(new PowerNet().joinLink(this));
			}
		}
	}

	private boolean canBuildNetworkThisTick() {

		long currentTick = world.getTotalWorldTime();

		if(!networkBuildScheduled) {
			int delay = (getIdentity() & Integer.MAX_VALUE) % NETWORK_BUILD_SPREAD_TICKS;
			nextNetworkBuildTick = currentTick + delay;
			networkBuildScheduled = true;
		}

		return currentTick >= nextNetworkBuildTick;
	}
	
	protected void connect() {
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			
			TileEntity te = world.getTileEntity(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ));
			
			if(te instanceof IEnergyConductor) {
				
				IEnergyConductor conductor = (IEnergyConductor) te;
				
				if(!conductor.canConnect(dir.getOpposite()))
					continue;
				
				if(this.getPowerNet() == null && conductor.getPowerNet() != null) {
					conductor.getPowerNet().joinLink(this);
				}
				
				if(this.getPowerNet() != null && conductor.getPowerNet() != null && this.getPowerNet() != conductor.getPowerNet()) {
					conductor.getPowerNet().joinNetworks(this.getPowerNet());
				}
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		
		if(!world.isRemote) {
			if(this.network != null) {
				this.network.reevaluate();
				this.network = null;
			}
		}
	}

	/**
	 * Only update until a power net is formed, in >99% of the cases it should be the first tick. Everything else is handled by neighbors and the net itself.
	 */
	public boolean canUpdate() {
		return (this.network == null || !this.network.isValid()) && !this.isInvalid();
	}

    @Override
	public long getPower() {
		return 0;
	}

	@Override
	public long getMaxPower() {
		return 0;
	}

	@Override
	public void setPowerNet(IPowerNet network) {
		this.network = network;
		this.networkBuildScheduled = false;
	}

	@Override
	public long transferPower(long power) {
		
		if(this.network == null)
			return power;
		
		return this.network.transferPower(power);
	}

	@Override
	public IPowerNet getPowerNet() {
		return this.network;
	}
}
