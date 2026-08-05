package com.hbm.sound;

import com.hbm.tileentity.machine.TileEntityFEL;
import com.hbm.tileentity.machine.TileEntityMachineMiningLaser;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;

public class SoundLoopTurbofan extends SoundLoopMachine {
	
	public SoundLoopTurbofan(SoundEvent path, TileEntity te) {
		super(path, te, 10);
	}

    public static boolean isProcessing(TileEntity te){
        boolean shouldPlay = false;
        if(te instanceof TileEntityMachineTurbofan fan) {
            shouldPlay = fan.isRunning;
        }
        return shouldPlay;
    }

    @Override
    public boolean isThisProcessing(){
        return isProcessing(te);
    }
}
