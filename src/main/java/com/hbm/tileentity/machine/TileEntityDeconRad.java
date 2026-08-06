package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.capability.HbmLivingCapability.EntityHbmPropsProvider;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ContaminationUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityDeconRad extends TileEntity implements ITickable {

	private static final int ENTITY_SCAN_INTERVAL = 5;
	private static float radRemove;
	private static final float decayRate = 0.9998074776F; //30m halflife
	private static final float intervalDecayRate = (float)Math.pow(decayRate, ENTITY_SCAN_INTERVAL);
	private static final float intervalActivationRemoval = -0.005F * (1F - intervalDecayRate) / (1F - decayRate);
	private AxisAlignedBB decontaminationBounds;

	public TileEntityDeconRad() {
		this(0.5F);
	}

	public TileEntityDeconRad(float rad) {
		super();
		radRemove = rad;
	}

	@Override
	public void update() {
		if(!this.world.isRemote && (this.world.getTotalWorldTime() + pos.toLong()) % ENTITY_SCAN_INTERVAL == 0) {
			if(decontaminationBounds == null)
				decontaminationBounds = new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5);
			List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, decontaminationBounds);

			if(!entities.isEmpty()) {
				for(Entity e : entities) {
					if(e instanceof EntityLivingBase){
						if(((EntityLivingBase)e).isPotionActive(HbmPotion.radiation)){
							((EntityLivingBase)e).removePotionEffect(HbmPotion.radiation);
						}
					}
					if(e.hasCapability(EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)){
						if(radRemove > 0.0F){
							e.getCapability(EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null).decreaseRads(radRemove * ENTITY_SCAN_INTERVAL);
						}
					}
					if(e instanceof EntityPlayer){
						ContaminationUtil.neutronActivateInventory((EntityPlayer)e, intervalActivationRemoval, intervalDecayRate);
						((EntityPlayer)e).inventoryContainer.detectAndSendChanges();
					}
				}
			}
		}
	}
}
