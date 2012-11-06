package frame;

import constants.EmoConst;
import sensors.Sensor;
import exceptions.*;

public class EmoFrame {
	// currentFrame has a fixed size and should not either shrink or grow.
	private Sensor[] currentFrame = new Sensor[EmoConst.NUMBER_OF_SENSORS];
	
	public EmoFrame(){}
	
	public EmoFrame(Sensor[] sensors){
		fill(sensors);
	}
	
	public EmoFrame(int[] values){
		if(values.length > EmoConst.NUMBER_OF_SENSORS) throw new TooManySensorsException(values.length);
		else {
			currentFrame = new Sensor[values.length];
			for(int i=0 ; i<values.length ; i++){
				currentFrame[i] = new Sensor(EmoConst.SENSOR_NAMES[i], values[i]);
			}
		}
	}
	
	/**
	 * Adds a new sensor to the currentFrame sensor array.
	 * @param sensor
	 */
	public void put(Sensor sensor) {
		if(sensor.isValid()) {
			int i = 0;
			while(currentFrame[i] != null || currentFrame[i].name() != sensor.name()){
				i++;
			}
			currentFrame[i] = sensor;
		} else throw new UnknownSensorNameException(sensor.name());
	}
	
	/**
	 * Returns data inside the sensor at a given position in the currentFrame array.
	 * @param sensor
	 * @return the value of the sensor at the given index
	 */
	public int getData(int sensor){
		return currentFrame[sensor].value();
	}
	
	/**
	 * Returns the size of the currentFrame array.
	 * @return the size of this frame
	 */
	public int size(){
		return currentFrame.length;
	}
	
	/**
	 * Fills the current Frame with given sensors.
	 * @param sensors
	 */
	private void fill(Sensor[] sensors){
		if(sensors.length > EmoConst.NUMBER_OF_SENSORS) throw new TooManySensorsException(sensors.length);
		else {
			for(int sensor=0 ; sensor<sensors.length ; sensor++){
				currentFrame[sensor] = sensors[sensor];
			}
		}
	}
}
