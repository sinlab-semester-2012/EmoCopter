package potentials;

/**
 * An ARDRoneSpeller is a derived implementation of the P300-speller.
 * The speller will choose a row or column and then wait for a delay.
 */
public class ARDRoneSpeller extends Thread {
	public static Object spellerLock = new Object();
	public final static int delay = 300;
	private boolean axis = false;
	private int position = -1;
	private final static String[][] MOVES = {
		{"RotateLeft", "Forward", "RotateRight"},
		{"Left", "TakeOff/Land", "Right"},
		{"Up", "Backward", "Down"}
		};
	private ARDroneSpellerGrid gui;
	
	public ARDRoneSpeller(ARDroneSpellerGrid gui){
		this.gui = gui;
	}
	
	/**
	 * Flash a row or column.
	 */
	public void run(){
		while(true){
			synchronized(spellerLock) {
				if(Math.random() < 0.5) {	// choose a row
					axis = false;
					position = randRow();
				} else {		// choose a column
					axis = true;
					position = randCol();
				}
				gui.flash(axis, position);
			}
			try {
				sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Chooses a row randomly.
	 * @return
	 */
	private int randRow(){
		return (int)(Math.random()*MOVES.length);
	}
	
	/**
	 * Chooses a column randomly.
	 * @return
	 */
	private int randCol(){
		return (int)(Math.random()*MOVES[0].length);
	}
	
	/**
	 * Horizontal axis is defined as true, vertical as false.
	 * @return true if a column, false in the case of a row.
	 */
	public boolean axis(){
		synchronized(spellerLock){
			return axis;
		}
	}
	
	/**
	 * Getter for the randomly chosen position.
	 * @return
	 */
	public int getPosition(){
		synchronized (spellerLock) {
			return position;
		}
	}
	
	/**
	 * Provides the name of a given position.
	 * @param row
	 * @param col
	 * @return
	 */
	public static String getMove(int row, int col){
		return new String(MOVES[row][col]);
	}
	
	public static String[] getLine(boolean axis, int position) {
		if(axis) {
			String[] s = new String[MOVES.length];
			for(int i=0 ; i<MOVES.length ; i++){
				s[i] = MOVES[i][position];
			}
			return s;
		} else {
			return MOVES[position];
		}
	}
}
