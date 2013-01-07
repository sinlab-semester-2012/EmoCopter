package emokit;

import constants.EmoConst;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

public class EEGCap extends Sensor{
	private int contactQuality = 0;
	private double[] freqs;
	
	/**
	 * An EEGCap sensor with a name.
	 * @param name
	 */
	public EEGCap(String name){
		this(name, 0);
	}
	
	/**
	 * An EEGCap sensor with a name and a value.
	 * @param name
	 * @param value
	 */
	public EEGCap(String name, int value) {
		super(name, value);
		init_freqs(EmoConst.getBinCount(false));
		isCap = true;
	}
	
	/**
	 * An EEGCap sensor with a name, value and array of frequencies.
	 * @param name
	 * @param value
	 * @param freqBins
	 */
	public EEGCap(String name, int value, double[] freqBins) {
		this(name, value);
		this.freqs = freqBins;
	}
	
	/**
	 * An EEGCap sensor with a name and both a sensor value and contact quality.
	 * @param name
	 * @param value
	 * @param contactQuality
	 */
	public EEGCap(String name, int value, int contactQuality){
		this(name, value);
		this.contactQuality = contactQuality;
	}
	
	/**
	 * An EEGCap sensor with a name, value and array of frequencies.
	 * @param name
	 * @param value
	 * @param freqBins
	 */
	public EEGCap(int name, int value, double[] freqBins) {
		this(EmoConst.SENSOR_NAMES[name], value);
		this.freqs = freqBins;
	}
	
	/**
	 * Initializer for the frequency array.
	 */
	private void init_freqs(int size){
		freqs = new double[size];
		for(int i=0 ; i<size ; i++){
			freqs[i] = 0.0;
		}
	}
	
	/**
	 * Setter for contact quality.
	 * @param quality
	 */
	public void updateQuality(int quality){
		this.contactQuality = quality;
	}
	
	/**
	 * Getter for contact quality.
	 * @return contact quality for this sensor
	 */
	public int quality(){
		return contactQuality;
	}
	
	/**
	 * Setter for the frequency array.
	 * @param freqs
	 */
	public void setFreqs(double[] freqs){
		for(int i=0 ; i<freqs.length ; i++){
			this.freqs[i] = freqs[i];
		}
	}
	
	/**
	 * Getter for the frequency array.
	 * @return
	 */
	public double[] getFreqs(){
		double[] bins = new double[freqs.length];
		for(int i=0 ; i<bins.length ; i++){
			bins[i] = freqs[i];
		}
		return bins;
	}
	
	/**
	 * Returns an Instance of all the bins present for this sensor.
	 * @return
	 */
	public Instance getBinsInstance(){
		int n = freqs.length;
		Instance bins = new DenseInstance(n);
		for(int i=0 ; i<n ; i++){
			bins.put(i, freqs[i]);
		}
		return bins;
	}
	
}
