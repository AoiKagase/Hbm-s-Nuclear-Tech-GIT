package com.hbm.sound;

import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;
import com.hbm.tileentity.machine.TileEntityMachineChemplant;
import com.hbm.tileentity.machine.TileEntityMachineChemfac;

import com.hbm.tileentity.machine.TileEntityMachineGasCent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;

public class SoundLoopChemplant extends SoundLoopMachine {
	
	public SoundLoopChemplant(SoundEvent path, TileEntity te) {
		super(path, te, 3);
	}

    public static boolean isProcessing(TileEntity te){
        boolean shouldPlay = false;
        if(te instanceof TileEntityMachineChemplant plant) {
            shouldPlay = plant.isProgressing;
        } else if(te instanceof TileEntityMachineChemfac plant) {
            shouldPlay = plant.isProgressing;
        }
        return shouldPlay;
    }

    @Override
    public boolean isThisProcessing(){
        return isProcessing(te);
    }
}
