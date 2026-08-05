package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityDemonLamp extends TileEntity implements ITickable {

	@Override
	public void update(){
		if(!world.isRemote) {
			radiate(world, pos.getX(), pos.getY(), pos.getZ());
		}
	}

	@SuppressWarnings("deprecation")
	private void radiate(World world, int x, int y, int z){

		float rads = 100000F;
		double range = 25D;
		double sourceX = x + 0.5D;
		double sourceY = y + 0.5D;
		double sourceZ = z + 0.5D;
		MutableBlockPos blockPos = new MutableBlockPos();

		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(sourceX, sourceY, sourceZ, sourceX, sourceY, sourceZ).grow(range, range, range));
		for(EntityLivingBase e : entities) {

			double dx = e.posX - sourceX;
			double dy = e.posY + e.getEyeHeight() - sourceY;
			double dz = e.posZ - sourceZ;
			double distanceSquared = dx * dx + dy * dy + dz * dz;
			double len = Math.sqrt(distanceSquared);
			double inverseLength = len >= 1.0E-4D ? 1.0D / len : 0.0D;
			double nx = dx * inverseLength;
			double ny = dy * inverseLength;
			double nz = dz * inverseLength;

			float res = 0;

			for(int i = 1; i < len; i++) {

				int ix = (int)Math.floor(sourceX + nx * i);
				int iy = (int)Math.floor(sourceY + ny * i);
				int iz = (int)Math.floor(sourceZ + nz * i);

				IBlockState state = world.getBlockState(blockPos.setPos(ix, iy, iz));
				res += state.getBlock().getExplosionResistance(null);
			}

			if(res < 1)
				res = 1;

			float eRads = rads;
			eRads /= (float)res;
			eRads /= (float)distanceSquared;

			ContaminationUtil.contaminate(e, HazardType.RADIATION, ContaminationType.CREATIVE, eRads);
			if(len < 2) {
				e.attackEntityFrom(DamageSource.IN_FIRE, 100);
			}
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return 65536.0D;
	}
}
