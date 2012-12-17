package potentials;

import java.util.Date;

public class P300 extends Thread{
	private Position position = new Position();
	// Minimum number of trials before having an acceptable move
	private int minimumNumberOfTrials = 10;
	// Number of already ran trials
	private int trialCounter = 0; 
	// Number of trials per second
	private long trialFrequency = 64;
	private Trial[] trials = new Trial[minimumNumberOfTrials]; // TODO has to be a buffer of trials
	
	public P300(){
	}
	
	public void run(){
		// TODO trials all the time and store current move
	}
	
	private void doTrial(){	// TODO has to submit a row or column every 1/trialFrequency seconds.
		try {
			sleep(1000/trialFrequency);
			trialCounter += 1;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public int randRow(){
		return (int)Math.random()*Move.height();
	}
	
	public int randCol(){
		return (int)Math.random()*Move.width();
	}
	
	// TODO find peak
	private void findPeak(double[][] freqBins){
		
	}
	
	public String getMove(int row, int col){
		return Move.getPosition(new Position(row, col));
	}
	
	private class Trial{
		private final int delay = 300;
		private long startTime;
		public Trial(){
			startTime = (new Date()).getTime();
		}
		public boolean isTimeOut(){
			return (new Date()).getTime() - startTime >= delay;
		}
	}
	
	private static class Move{
		private final static String[][] MOVES = {
			{"RotateLeft", "Forward", "RotateRight"},
			{"Left", "TakeOff/Land", "Right"},
			{"Up", "Backward", "Down"}
			};
		public static String getPosition(Position p){
			return new String(MOVES[p.c][p.r]);
		}
		public static int height(){
			return MOVES.length;
		}
		public static int width(){
			return MOVES[0].length;
		}
	}
	private class Position{
		public int r = -1, c = -1;
		public Position(){}
		public Position(int row, int col){
			this.r = row;
			this.c = col;
		}
	}
}

