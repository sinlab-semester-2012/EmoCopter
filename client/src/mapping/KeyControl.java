package mapping;

import com.shigeodayo.ardrone.processing.ARDroneForP5;

/**
 * This class provides a mapping for keyboard-sent commands.
 */
public class KeyControl implements processing.core.PConstants{
	public static void map(ARDroneForP5 drone, int key, int keyCode){
		switch(key) {
		case CODED:
			switch(keyCode) {
			case UP:			drone.forward(50); break;
			case DOWN:		drone.backward(50); break;
			case LEFT:		drone.goLeft(50); break;
			case RIGHT:		drone.goRight(50); break;
			case SHIFT:		drone.takeOff(); break;
			case CONTROL:	drone.landing(); break;
			}
			break;
		case WAIT:	drone.stop(); break;
		case 'd':	drone.spinRight(10); break;
		case 'a':	drone.spinLeft(10); break;
		case 'w':	drone.up(10); break;
		case 's':	drone.down(10); break;
		case '1':	drone.setHorizontalCamera(); break;
		case '2':	drone.setHorizontalCameraWithVertical(); break;
		case '3':	drone.setVerticalCamera(); break;
		case '4':	drone.setVerticalCameraWithHorizontal(); break;
		case '5':	drone.toggleCamera(); break;
		}
	}
}
