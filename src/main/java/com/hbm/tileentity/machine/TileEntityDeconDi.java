package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.capability.HbmLivingCapability.EntityHbmPropsProvider;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityDeconDi extends TileEntity implements ITickable {

	private static final int ENTITY_SCAN_INTERVAL = 5;
	private static float digammaRemove;
	private AxisAlignedBB decontaminationBounds;
    public TileEntityDeconDi() {
        this(0.001F);
    }

    public TileEntityDeconDi(float dig) {
        super();
		digammaRemove = dig;
	}

	@Override
	public void update() {
		if(!this.world.isRemote && (this.world.getTotalWorldTime() + pos.toLong()) % ENTITY_SCAN_INTERVAL == 0) {
			if(decontaminationBounds == null)
				decontaminationBounds = new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5);
			List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, decontaminationBounds);

			if(!entities.isEmpty()) {
				for(Entity e : entities) {
					if(e.hasCapability(EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)){
						if(digammaRemove > 0.0F){
							e.getCapability(EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null).decreaseDigamma(digammaRemove * ENTITY_SCAN_INTERVAL);
						}
					}
				}
			}
		}
	}
}
