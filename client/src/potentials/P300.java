package potentials;

import java.util.Date;

/**
 * The idea here is to cluster all information regarding a trial at P300.
 * This information should be provided to the speller, which will in turn
 * know which selection to make.
 */
public class P300 extends Thread{
	// Minimum number of trials before having an acceptable move
	public static final int minimumNumberOfTrials = 10;	// TODO TBD on experiment basis
	// Number of already ran trials
	private int trialCounter = 0; 
	// Number of trials per second
	public static final long trialFrequency = 64;		// [Hz] TODO has to be defined also
	public static final int minLatency = 300;			// [ms]
	public static final int maxLatency = 600;			// [ms]
	public static final int minVoltDeflection = 2;	// [uV]
	public static final int maxVoltDeflection = 5;	// [uV]
	public static final String[] sensors = {"P7", "P8"};	// Parietal lobe
	private Trial[] trials = new Trial[minimumNumberOfTrials]; // TODO buffer of trials
	
	public P300(ARDRoneSpeller speller){
		
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
	
	// TODO find peak, in other words, build a classifier
	private void findPeak(double[][] freqBins){
		
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
}

