package emokit;

import net.sf.javaml.core.*;
import constants.EmoConst;
import exceptions.*;
import feature_extraction.Features;

public class EmoFrame {
	// frame has a fixed size and should neither shrink nor grow.
	private boolean frameType = false;
	private Sensor[] frame = new Sensor[EmoConst.NUMBER_OF_SENSORS];
	private int numberOfCaps = 0;
	private int numberOfInfo = 0;
	private boolean isFull = false;
	private boolean infoSet = false;
	
	/**
	 * Constructs an empty EmoFrame.
	 * @param frameType - true indicates a learning frame, false a normal one.
	 */
	public EmoFrame(boolean frameType){
		this.frameType = frameType;
		for(int sensor=0 ; sensor<frame.length ; sensor++){
			if(sensor < EmoConst.NUMBER_OF_EEG_CAPS){
				if(frameType){
					frame[sensor] = new EEGCap(sensor, 0, new double[EmoConst.getBinCount(frameType)]);
				} else {
					frame[sensor] = new EEGCap(EmoConst.SENSOR_NAMES[sensor]);
				}
			} else {
				frame[sensor] = new InfoSensor(EmoConst.SENSOR_NAMES[sensor]);
			}
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
			frame[index].updateValue(sensor.value());
			if(sensor.isCap()) {
				try {
					frame[index].setFreqs(((EEGCap)sensor).getFreqs());
				} catch (WrongSensorException e) {
					e.printStackTrace();
				}
				if(isFull) throw new FullFrameException("Sensors are aleady full.");
				numberOfCaps++;
				if(numberOfCaps >= frame.length - EmoConst.sensorDiff) isFull = true;
			} else {
				if(infoSet) throw new FullFrameException("Info is already set.");
				numberOfInfo++;
				if(numberOfInfo >= EmoConst.sensorDiff) infoSet = true;
			}
		} else throw new UnknownSensorNameException(sensor.name());
	}
	
	/**
	 * Value setter for a specific sensor in the frame.
	 * @param sensor
	 * @param value
	 */
	public void set(int sensor, int value){
		frame[sensor].updateValue(value);
	}
	
	/**
	 * Frequency bins setter for a specific sensor in the frame.
	 * @param sensor
	 * @param bins
	 * @throws WrongSensorException
	 */
	public void setFreqs(int sensor, double[] bins) throws WrongSensorException{
		frame[sensor].setFreqs(bins);
	}
	
	/**
	 * Contact quality setter.
	 * @param sensor
	 * @param quality
	 */
	public void setQuality(int sensor, int quality){
		((EEGCap)frame[sensor]).updateQuality(quality);
	}
	
	/**
	 * Getter for contact quality.
	 * @param sensor
	 * @return
	 */
	public int getQuality(int sensor){
		return ((EEGCap)frame[sensor]).quality();
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
	 * Provides an instance built from the frequency bins in a given sensor.
	 * @param sensor
	 * @return
	 * @throws WrongSensorException
	 */
	public Instance getBinsInstance(int sensor) throws WrongSensorException{
		return frame[sensor].getBinsInstance();
	}
	
	/**
	 * Returns an instance of the data inside this frame.
	 * This is useful for studying signal before any processing has been done.
	 * @param withInfo says whether to include gyro and battery info.
	 * @return the data instance.
	 */
	public Instance getInstance(){
		int n = EmoConst.NUMBER_OF_EEG_CAPS;
		Instance instance = new DenseInstance(n);
		for(int sensor = 0 ; sensor<n ; sensor++){
			instance.put(sensor, (double)frame[sensor].value());
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
	 * Extract powers from theta, alpha, beta and gamma bands.
	 * @param sensor
	 * @return
	 */
	public double[] extractPowers(int sensor){
		int seconds = 1;
		if(frameType) seconds = EmoConst.FFT_SIZE_RATIO;
		double[] bins = null;
		try {
			bins = frame[sensor].getFreqs();
		} catch (WrongSensorException e) {
			e.printStackTrace();
		}
		double[] theta = Features.theta(bins, seconds);
		double[] alpha = Features.alpha(bins, seconds);
		double[] beta = Features.beta(bins, seconds);
		double[] gamma = Features.gamma(bins, seconds);
		double thetaPower = Features.bandPower(theta, seconds);
		double alphaPower = Features.bandPower(alpha, seconds);
		double betaPower = Features.bandPower(beta, seconds);
		double gammaPower = Features.bandPower(gamma, seconds);
		
		return new double[]{thetaPower, alphaPower, betaPower, gammaPower};
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
