package exceptions;

public class WrongSensorException extends Exception {
	private static final long serialVersionUID = 1L;
	public WrongSensorException(){
		super("Sensor is incorrectly used.");
	}
}
