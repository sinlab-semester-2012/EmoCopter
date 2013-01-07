package mapping;

import com.shigeodayo.ardrone.processing.ARDroneForP5;

import constants.EmoConst;

public class ConsciousControl implements processing.core.PConstants{

	public static void map(ARDroneForP5 drone, Command command) {
		int cmd = 0;
		for(int i=0 ; i<EmoConst.COMMANDS.length ; i++){
			if(command.command().equals(EmoConst.COMMANDS[i])) cmd = i;
		}
		switch(cmd){
		case EmoConst.STOP:		drone.stop(); break;
		case EmoConst.UP:		drone.up(20); break;
		case EmoConst.DOWN:		drone.down(20); break;
		case EmoConst.FORWARD:	drone.forward(25); break;
		case EmoConst.SPINLEFT:	drone.spinLeft(25); break;
		default: drone.stop();
		}
	}
	
}
