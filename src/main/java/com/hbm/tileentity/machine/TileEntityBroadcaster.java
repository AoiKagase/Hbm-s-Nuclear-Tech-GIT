package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.lib.ModDamageSource;
import com.hbm.packet.LoopedSoundPacket;
import com.hbm.packet.PacketDispatcher;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityBroadcaster extends TileEntity implements ITickable {

	private static final double RANGE = 25D;
	private static final double RANGE_SQUARED = RANGE * RANGE;

	@Override
	public void update() {
		if(!world.isRemote) {
			double centerX = pos.getX() + 0.5D;
			double centerY = pos.getY() + 0.5D;
			double centerZ = pos.getZ() + 0.5D;
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(centerX - RANGE, centerY - RANGE, centerZ - RANGE, centerX + RANGE, centerY + RANGE, centerZ + RANGE));

			for(Entity entity : list) {
				if(entity instanceof EntityLivingBase) {
					EntityLivingBase e = (EntityLivingBase)entity;
					double dx = e.posX - centerX;
					double dy = e.posY - centerY;
					double dz = e.posZ - centerZ;
					double distanceSquared = dx * dx + dy * dy + dz * dz;

					if(distanceSquared <= RANGE_SQUARED) {
						double distance = Math.sqrt(distanceSquared);
						double t = (RANGE - distance) / RANGE * 10;
						e.attackEntityFrom(ModDamageSource.broadcast, (float)t);
						if(!(e instanceof EntityPlayer p && (p.capabilities.isCreativeMode || p.isSpectator()))) {
							PotionEffect nausea = e.getActivePotionEffect(MobEffects.NAUSEA);
							if(nausea == null || nausea.getDuration() < 100)
								e.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300, 0));
						}
					}
				}
			}

			if(world.getTotalWorldTime() % 20 == 0) {
				PacketDispatcher.wrapper.sendToAllAround(new LoopedSoundPacket(pos.getX(), pos.getY(), pos.getZ()), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), LoopedSoundPacket.AUDIO_RANGE));
			}
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos, pos.add(1, 2, 1));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
}
