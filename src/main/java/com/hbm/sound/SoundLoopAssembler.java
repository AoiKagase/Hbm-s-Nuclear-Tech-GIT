package com.hbm.sound;

import com.hbm.tileentity.machine.TileEntityMachineAssembler;

import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;
import com.hbm.tileentity.machine.TileEntityMachineGasCent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;

public class SoundLoopAssembler extends SoundLoopMachine {
	
	public SoundLoopAssembler(SoundEvent path, TileEntity te) {
		super(path, te, 3);
	}

    public static boolean isProcessing(TileEntity te){
        boolean shouldPlay = false;
        if(te instanceof TileEntityMachineAssembler ass) {
            shouldPlay = ass.isProgressing;
        }
        return shouldPlay;
    }

    @Override
    public boolean isThisProcessing(){
        return isProcessing(te);
    }
}
