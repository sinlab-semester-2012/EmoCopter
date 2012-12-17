package sensors;

import net.sf.javaml.core.Instance;
import constants.EmoConst;
import exceptions.WrongSensorException;

public class Sensor {
	protected boolean isCap = false;
	private String name;
	private int value;
	private boolean isSet;
	
	/**
	 * Constructs a sensor with a given name and a value set to 0.
	 * @param name
	 */
	public Sensor(String name){
		this(name, 0);
		isSet = false;
	}
	
	/**
	 * Constructs a sensor with a given name and given value.
	 * @param name
	 * @param value
	 */
	public Sensor(String name, int value){
		this.name = new String(name);
		this.value = value;
		isSet = true;
	}
	
	/**
	 * Constructs a sensor with a given name index and a value set to 0.
	 * @param name index
	 */
	public Sensor(int name){
		this(name, 0);
		isSet = false;
	}
	
	/**
	 * Constructs a sensor with a given name index and given value.
	 * @param name index
	 * @param value
	 */
	public Sensor(int name, int value){
		this.name = EmoConst.SENSOR_NAMES[name];
		this.value = value;
		isSet = true;
	}
	
	/**
	 * Returns true only if the argument name is a valid sensor.
	 * @param sensor
	 * @return true if the argument is a valid sensor, false otherwise
	 */
	public static boolean isValid(String sensor){
		boolean contains = false;
		for(int i=0 ; i<EmoConst.NUMBER_OF_SENSORS ; i++)
			contains |= (sensor.equals(EmoConst.SENSOR_NAMES[i]));
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
	 * Getter for isSet.
	 * @return true if a value has already been assigned to this sensor.
	 */
	public boolean isSet(){
		return isSet;
	}
	
	/**
	 * Setter for value.
	 * @param value
	 */
	public void updateValue(int value){
		isSet = true;
		this.value = value;
	}
	
	/**
	 * Getter for isCap.
	 * @return true if this sensor is an EEG cap.
	 */
	public boolean isCap(){
		return isCap;
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
	
	/**
	 * 
	 * @param bins
	 * @throws WrongSensorException
	 */
	public void setFreqs(double[] bins) throws WrongSensorException {
		throw new WrongSensorException();
	}

	/**
	 * 
	 * @return
	 * @throws WrongSensorException
	 */
	public Instance getBinsInstance() throws WrongSensorException {
		throw new WrongSensorException();
	}
}
