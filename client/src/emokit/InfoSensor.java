package emokit;

import net.sf.javaml.core.Instance;
import exceptions.WrongSensorException;

public class InfoSensor extends Sensor{
	public InfoSensor(String name){
		super(name);
		isCap = false;
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
