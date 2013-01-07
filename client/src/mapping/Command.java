package mapping;

public class Command {
	private int key, keyCode;
	private String command;
	
	public Command(int key, int keyCode){
		this.key = key;
		this.keyCode = keyCode;
	}
	
	public Command(String command){
		this.command = new String(command);
	}
	
	public int key(){
		return key;
	}
	
	public int keyCode(){
		return keyCode;
	}
	
	public String command(){
		return command;
	}
}
