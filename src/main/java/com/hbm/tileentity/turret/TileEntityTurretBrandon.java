package com.hbm.tileentity.turret;

import java.util.Arrays;
import java.util.List;

import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.lib.HBMSoundHandler;

import net.minecraft.util.SoundCategory;

public class TileEntityTurretBrandon extends TileEntityTurretBaseNT {

	private static final List<Integer> CONFIGS = Arrays.asList(
			BulletConfigSyncingUtil.GRENADE_NORMAL,
			BulletConfigSyncingUtil.GRENADE_HE,
			BulletConfigSyncingUtil.GRENADE_INCENDIARY,
			BulletConfigSyncingUtil.GRENADE_CHEMICAL,
			BulletConfigSyncingUtil.GRENADE_SLEEK,
			BulletConfigSyncingUtil.GRENADE_CONCUSSION,
			BulletConfigSyncingUtil.GRENADE_FINNED,
			BulletConfigSyncingUtil.GRENADE_NUCLEAR,
			BulletConfigSyncingUtil.GRENADE_PHOSPHORUS,
			BulletConfigSyncingUtil.GRENADE_TRACER,
			BulletConfigSyncingUtil.GRENADE_KAMPF);

	private int firingTimer;
	
	@Override
	public long getMaxPower() {
		return 10000;
	}

	@Override
	public void updateFiringTick() {
		firingTimer++;
		if(firingTimer % 20 != 0) return;

		BulletConfiguration config = getFirstConfigLoaded();
		if(config == null) return;

		spawnBullet(config);
		conusmeAmmo(config.ammo);
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.glauncher, SoundCategory.BLOCKS, 2.0F, 1.0F);
	}

	@Override
	protected List<Integer> getAmmoList() {
		return CONFIGS;
	}

	@Override
	public String getName() {
		return "container.turretBrandon";
	}
}
