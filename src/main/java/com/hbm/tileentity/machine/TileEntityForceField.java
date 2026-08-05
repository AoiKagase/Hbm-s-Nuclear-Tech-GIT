package com.hbm.tileentity.machine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.TEFFPacket;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.energy.IEnergyUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityForceField extends TileEntityLoadedBase implements ITickable, IEnergyUser {

	public ItemStackHandler inventory;
	
	public int health = 100;
	public int maxHealth = 100;
	public long power;
	public int powerCons;
	public int cooldown = 0;
	public int blink = 0;
	public float radius = 16;
	public boolean isOn = false;
	public int color = 0x00C6FF;
	public final int baseCon = 1000;
	public final int radCon = 500;
	public final int shCon = 250;
	public static final long maxPower = 1000000;
	
	//private static final int[] slots_top = new int[] {0};
	//private static final int[] slots_bottom = new int[] {0};
	//private static final int[] slots_side = new int[] {0};
	
	private String customName;
	
	public TileEntityForceField() {
		inventory = new ItemStackHandler(3){
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
		};
	}
	
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.forceField";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && !this.customName.isEmpty();
	}
	
	public void setCustomName(String name) {
		this.customName = name;
	}
	
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=64;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		this.power = nbt.getLong("powerTime");
		this.health = nbt.getInteger("health");
		this.maxHealth = nbt.getInteger("maxHealth");
		this.cooldown = nbt.getInteger("cooldown");
		this.blink = nbt.getInteger("blink");
		this.radius = nbt.getFloat("radius");
		this.isOn = nbt.getBoolean("isOn");
		if(nbt.hasKey("inventory"))
			inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
		super.readFromNBT(nbt);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("powerTime", power);
		nbt.setInteger("health", health);
		nbt.setInteger("maxHealth", maxHealth);
		nbt.setInteger("cooldown", cooldown);
		nbt.setInteger("blink", blink);
		nbt.setFloat("radius", radius);
		nbt.setBoolean("isOn", isOn);
		nbt.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(nbt);
	}
	
	public int getHealthScaled(int i) {
		return (health * i) / Math.max(1, maxHealth);
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / Math.max(1, maxPower);
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {
			this.updateStandardConnections(world, pos);
			int rStack = 0;
			int hStack = 0;
			radius = 16;
			maxHealth = 100;
			
			if(inventory.getStackInSlot(1).getItem() == ModItems.upgrade_radius) {
				rStack = inventory.getStackInSlot(1).getCount();
				radius += rStack * 16;
			}
			
			if(inventory.getStackInSlot(2).getItem() == ModItems.upgrade_health) {
				hStack = inventory.getStackInSlot(2).getCount();
				maxHealth += hStack * 50;
			}
			
			this.powerCons = this.baseCon + rStack * this.radCon + hStack * this.shCon;
			
			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
			
			if(blink > 0) {
				blink--;
				color = 0xFF0000;
			} else {
				color = 0x00C6FF;
			}
		}
		
		if(cooldown > 0) {
			cooldown--;
		} else {
			if(health < maxHealth)
				health += maxHealth / 50;
			
			if(health > maxHealth)
				health = maxHealth;
		}
		
		if(isOn && cooldown == 0 && health > 0 && power >= powerCons) {
			doField(radius);
			
			if(!world.isRemote) {
				power -= powerCons;
				markDirty();
			}
		} else {
			this.outside.clear();
			this.inside.clear();
			this.nextOutside.clear();
			this.nextInside.clear();
		}

		if(!world.isRemote) {
			if(power < powerCons)
				power = 0;
		}
		
		if(!world.isRemote) {
			PacketDispatcher.wrapper.sendToAllTracking(new TEFFPacket(pos, radius, health, maxHealth, (int) power, isOn, color, cooldown), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 100));
		}
	}
	
	private int impact(Entity e) {
		
		double mass = e.height * e.width * e.width;
		double speed = getMotionWithFallback(e);
		return (int)(mass * speed * 50);
	}
	
	private void damage(int ouch) {
		health -= ouch;
		
		if(ouch >= (this.maxHealth / 250))
			blink = 5;
		
		if(health <= 0) {
			health = 0;
			cooldown = (int) (100 + radius);
		}
	}

	Set<Entity> outside = new HashSet<Entity>();
	Set<Entity> inside = new HashSet<Entity>();
	Set<Entity> nextOutside = new HashSet<Entity>();
	Set<Entity> nextInside = new HashSet<Entity>();
	
	private void doField(float rad) {
		Set<Entity> previousOutside = outside;
		Set<Entity> previousInside = inside;
		Set<Entity> currentOutside = nextOutside;
		Set<Entity> currentInside = nextInside;

		currentOutside.clear();
		currentInside.clear();

		double centerX = pos.getX() + 0.5D;
		double centerY = pos.getY() + 0.5D;
		double centerZ = pos.getZ() + 0.5D;
		double radiusSquared = rad * rad;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(centerX - (rad + 25), centerY - (rad + 25), centerZ - (rad + 25), centerX + (rad + 25), centerY + (rad + 25), centerZ + (rad + 25)));
		
		for(Entity entity : list) {
			
			if(!(entity instanceof EntityPlayer) && !(entity instanceof EntityItem)) {
				double dx = centerX - entity.posX;
				double dy = centerY - entity.posY;
				double dz = centerZ - entity.posZ;
				boolean out = dx * dx + dy * dy + dz * dz > radiusSquared;
				boolean wasOutside = previousOutside.contains(entity);
				boolean wasInside = previousInside.contains(entity);
				
				//if the entity has not been registered yet
				if(!wasOutside && !wasInside) {
					if(out) {
						currentOutside.add(entity);
					} else {
						currentInside.add(entity);
					}
					
				//if the entity has been detected before
				} else {
					
					//if the entity has crossed inwards
					if(wasOutside && !out) {
						double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
						double nx = 0.0D;
						double ny = 0.0D;
						double nz = 0.0D;
						if(distance >= 1.0E-4D) {
							double inverseDistance = 1.0D / distance;
							nx = dx * inverseDistance;
							ny = dy * inverseDistance;
							nz = dz * inverseDistance;
						}
						
						double mx = -nx * (rad + 1);
						double my = -ny * (rad + 1);
						double mz = -nz * (rad + 1);
						
						entity.setLocationAndAngles(centerX + mx, centerY + my, centerZ + mz, 0, 0);
						
						double mo = Math.sqrt(entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ);

						entity.motionX = nx * -mo;
						entity.motionY = ny * -mo;
						entity.motionZ = nz * -mo;

						entity.posX -= entity.motionX;
						entity.posY -= entity.motionY;
						entity.posZ -= entity.motionZ;

			    		world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.sparkShoot, SoundCategory.BLOCKS, 2.5F, 1.0F);
						currentOutside.add(entity);
						
						if(!world.isRemote) {
							this.damage(this.impact(entity));
						}
						
					} else
					
					//if the entity has crossed outwards
					if(wasInside && out) {
						double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
						double nx = 0.0D;
						double ny = 0.0D;
						double nz = 0.0D;
						if(distance >= 1.0E-4D) {
							double inverseDistance = 1.0D / distance;
							nx = dx * inverseDistance;
							ny = dy * inverseDistance;
							nz = dz * inverseDistance;
						}
						
						double mx = -nx * (rad - 1);
						double my = -ny * (rad - 1);
						double mz = -nz * (rad - 1);

						entity.setLocationAndAngles(centerX + mx, centerY + my, centerZ + mz, 0, 0);
						
						double mo = Math.sqrt(entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ);

						entity.motionX = nx * mo;
						entity.motionY = ny * mo;
						entity.motionZ = nz * mo;

						entity.posX -= entity.motionX;
						entity.posY -= entity.motionY;
						entity.posZ -= entity.motionZ;

			    		world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.sparkShoot, SoundCategory.BLOCKS, 2.5F, 1.0F);
						currentInside.add(entity);
						
						if(!world.isRemote) {
							this.damage(this.impact(entity));
						}
						
					} else {
						
						if(out) {
							currentOutside.add(entity);
						} else {
							currentInside.add(entity);
						}
					}
				}
			}
		}

		outside = currentOutside;
		inside = currentInside;
		nextOutside = previousOutside;
		nextInside = previousInside;
	}
	
	private double getMotionWithFallback(Entity e) {

		double s1 = Math.sqrt(e.motionX * e.motionX + e.motionY * e.motionY + e.motionZ * e.motionZ);
		double dx = e.posX - e.prevPosY;
		double dy = e.posY - e.prevPosY;
		double dz = e.posZ - e.prevPosZ;
		double s2 = Math.sqrt(dx * dx + dy * dy + dz * dz);
		
		if(s1 == 0)
			return s2;
		
		if(s2 == 0)
			return s1;
		
		return Math.min(s1, s2);
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

}
