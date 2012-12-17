package frame;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import constants.EmoConst;
import sensors.*;
import exceptions.*;

public class EmoFrame {
	// frame has a fixed size and should neither shrink nor grow.
	private Sensor[] frame = new Sensor[EmoConst.NUMBER_OF_SENSORS];
	private int numberOfCaps = 0;
	private int numberOfInfo = 0;
	private boolean isFull = false;
	private boolean infoSet = false;
	
	/**
	 * Constructs an empty EmoFrame.
	 */
	public EmoFrame(){
		for(int sensor=0 ; sensor<frame.length ; sensor++){
			frame[sensor] = new Sensor(EmoConst.SENSOR_NAMES[sensor]);
		}
	}
	
	/**
	 * Constructs an EmoFrame with the given sensor array and sorts them.
	 * @param sensor array
	 */
	public EmoFrame(Sensor[] sensors){
		try {
			fill(sensors);
		} catch (FullFrameException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructs an EmoFrame with the given integer values.
	 * @param values
	 */
	public EmoFrame(int[] values){
		if(values.length > EmoConst.NUMBER_OF_SENSORS) throw new TooManySensorsException(values.length);
		else {
			frame = new Sensor[values.length];
			for(int i=0 ; i<values.length ; i++){
				frame[i] = new Sensor(EmoConst.SENSOR_NAMES[i], values[i]);
			}
		}
	}
	
	/**
	 * Adds a new sensor to the sensor array at the right position.
	 * Also checks whether all caps have been recorded and all info is present.
	 * These informations can be retrieved from isFull and infoSet respectively.
	 * @param sensor
	 * @throws FullFrameException 
	 */
	public void put(Sensor sensor) throws FullFrameException {
		if(sensor.isValid()) {
			int index = getSensorIndex(sensor.name());
			frame[index] = sensor;
			if(sensor.isCap()) {
				if(isFull) throw new FullFrameException("Sensors are aleady full.");
				numberOfCaps++;
				if(numberOfCaps >= frame.length - EmoConst.sensorDiff) isFull = true;
			}
			else {
				if(infoSet) throw new FullFrameException("Info is already set.");
				numberOfInfo++;
				if(numberOfInfo >= EmoConst.sensorDiff) infoSet = true;
			}
		} else throw new UnknownSensorNameException(sensor.name());
	}
	
	/**
	 * Returns data inside the sensor at a given position in the sensor array.
	 * @param sensor index
	 * @return the value of the sensor at the given index
	 */
	public int getData(int sensor){
		return frame[sensor].value();
	}
	
	/**
	 * Returns data inside the sensor at a given position in the sensor array.
	 * @param sensor name
	 * @return the value of the sensor at the given index
	 */
	public int getData(String sensor){
		return getData(getSensorIndex(sensor));
	}
	
	/**
	 * Returns the bins computed by fft for a given sensor.
	 * @param sensor index
	 * @return
	 * @throws WrongSensorException 
	 */
	public double[] getBins(int sensor) throws WrongSensorException{
		if(!frame[sensor].isCap()) throw new WrongSensorException();
		return ((EEGCap)frame[sensor]).getFreqs();
	}
	
	/**
	 * Returns the bins computed by fft for a given sensor.
	 * @param sensor name
	 * @return
	 * @throws WrongSensorException 
	 */
	public double[] getBins(String sensor) throws WrongSensorException{
		return getBins(getSensorIndex(sensor));
	}
	
	/**
	 * Returns an instance of the data inside this frame.
	 * This is useful for studying signal before any processing has been done.
	 * @param withInfo says whether to include gyro and battery info.
	 * @return the data instance.
	 */
	public Instance getInstance(boolean withInfo){
		int n = EmoConst.NUMBER_OF_EEG_CAPS;
		if(withInfo) n = EmoConst.NUMBER_OF_SENSORS;
		Instance instance = new DenseInstance(n);
		for(int i = 0 ; i<n ; i++){
			instance.add(frame[i].value());
		}
		return instance;
	}
	
	/**
	 * This method provides a Dataset of all Instances of the sensors' bins.
	 * @return
	 * @throws WrongSensorException 
	 */
	public Dataset getFrequencyDataset() throws WrongSensorException{
		int n = EmoConst.NUMBER_OF_EEG_CAPS;
		Dataset frequencies = new DefaultDataset();
		for(int i = 0 ; i<n ; i++){
			if(!frame[i].isCap()) throw new WrongSensorException();
			frequencies.add(((EEGCap) frame[i]).getBinsInstance());
		}
		return frequencies;
	}
	
	/**
	 * Returns the size of the sensor array.
	 * @return the size of this frame
	 */
	public int size(){
		return frame.length;
	}
	
	/**
	 * Getter for isFull.
	 * @return true if the frame is already full, false otherwise
	 */
	public boolean isFull(){
		return isFull;
	}
	
	/**
	 * Getter for infoSet
	 * @return true if all information has been gathered
	 */
	public boolean infoSet(){
		return infoSet;
	}
	
	/**
	 * Gets the index of the requested sensor in this frame.
	 * @param the sensor's name
	 * @return the sensor's index if it exists, -1 otherwise
	 */
	public int getSensorIndex(String sensor){
		int index = -1;
		if(Sensor.isValid(sensor)){
			int i = 0;
			while(!frame[i].name().equals(sensor) || i > EmoConst.NUMBER_OF_SENSORS) i++;
			index = i;
		}
		return index;
	}
	
	/**
	 * Fills the current Frame with given sensors at their correct positions.
	 * @param sensors
	 * @throws FullFrameException 
	 */
	private void fill(Sensor[] sensors) throws FullFrameException{
		if(sensors.length > EmoConst.NUMBER_OF_SENSORS) throw new TooManySensorsException(sensors.length);
		else {
			for(int sensor=0 ; sensor<sensors.length ; sensor++){
				put(sensors[sensor]);
			}
		}
	}
}
