package exceptions;

import javax.lang.model.element.UnknownElementException;

public class UnknownSensorNameException extends UnknownElementException{
	private static final long serialVersionUID = 1L;
	public UnknownSensorNameException(String name){
		super(null, "This type of sensor doesn't exist : " + name);
	}
}
