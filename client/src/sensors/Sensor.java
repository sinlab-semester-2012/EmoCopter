package sensors;

import constants.EmoConst;

public class Sensor {
	private String name;
	private int value;
	
	/**
	 * A sensor with a name.
	 * @param name
	 */
	public Sensor(String name){
		this(name, 0);
	}
	
	/**
	 * A sensor with a name and a value.
	 * @param name
	 * @param value
	 */
	public Sensor(String name, int value){
		this.name = new String(name);
		this.value = value;
	}
	
	/**
	 * Returns true only if the argument name is a valid sensor.
	 * @param sensor
	 * @return true if the argument is a valid sensor, false otherwise
	 */
	public static boolean isValid(String sensor){
		boolean contains = false;
		for(int i=0 ; i<EmoConst.NUMBER_OF_SENSORS ; i++)
			contains |= (sensor == EmoConst.SENSOR_NAMES[i]);
		return contains;
	}
	
	/**
	 * Returns true only if this is a valid sensor.
	 * @return true if this is a valid sensor, false otherwise
	 */
	public boolean isValid(){
		return isValid(name);
	}
	
	/**
	 * Setter for value.
	 * @param value
	 */
	public void updateValue(int value){
		this.value = value;
	}
	
	/**
	 * Getter for name.
	 * @return the sensor's name
	 */
	public String name(){
		return new String(name);
	}
	
	/**
	 * Getter for value.
	 * @return the sensor's value
	 */
	public int value(){
		return value;
	}
}
