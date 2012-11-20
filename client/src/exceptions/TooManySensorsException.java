package exceptions;

public class TooManySensorsException extends IllegalArgumentException{
	private static final long serialVersionUID = 1L;
	public TooManySensorsException(int n){
		super("This frame cannot take as many sensors : " + n);
	}
}
