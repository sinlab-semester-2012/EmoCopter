package sensors;

public class EEGCap extends Sensor{
	private int contactQuality = 0;
	
	/**
	 * An EEGCap sensor with a name.
	 * @param name
	 */
	public EEGCap(String name){
		super(name, 0);
	}
	
	/**
	 * An EEGCap sensor with a name and a value.
	 * @param name
	 * @param value
	 */
	public EEGCap(String name, int value) {
		super(name, value);
	}
	
	/**
	 * An EEGCap sensor with a name and both a sensor value and contact quality.
	 * @param name
	 * @param value
	 * @param contactQuality
	 */
	public EEGCap(String name, int value, int contactQuality){
		super(name, value);
		this.contactQuality = contactQuality;
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

}
