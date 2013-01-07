package feature_extraction;

import constants.EmoConst;

/**
 * This class provides static methods parsing pre-processed waves.
 * This is useful only with an fft'ed signal.
 * It is assumed that the number of bins is at least 64 and that the 
 * standard time scale for the fft is 1 second, so that given a 3 seconds 
 * sample, the power will be divided by 3 to match the 1s one.
 * For more information on brain wave patterns, visit:
 * http://en.wikipedia.org/wiki/Electroencephalography#Wave_patterns
 */
public class Features {
	
	/**
	 * Delta waves are usually associated with deep sleep or lack
	 * of consciousness. They range from 0 to 4 Hz.
	 * @param bins
	 * @param seconds
	 * @return
	 */
	public static double[] delta(double[]  bins, int seconds){
		int lower = 1;
		int upper = 4;
		double[] delta = createBand(bins, lower, upper, seconds);
		return delta;
	}
	
	/**
	 * Theta waves can be seen in drowsiness or arousal, or also
	 * during meditation.
	 * They range from 4 to 7 Hz.
	 * @param bins
	 * @param seconds
	 * @return
	 */
	public static double[] theta(double[]  bins, int seconds){
		int lower = 4;
		int upper = 7;
		double[] theta = createBand(bins, lower, upper, seconds);
		return theta;
	}
	
	/**
	 * Alpha waves are associated with relaxation.
	 * They range from 8 to 12 Hz.
	 * @param bins
	 * @param seconds
	 * @return
	 */
	public static double[] alpha(double[]  bins, int seconds){
		int lower = 8;
		int upper = 12;
		double[] alpha = createBand(bins, lower, upper, seconds);
		return alpha;
	}
	
	/**
	 * Beta waves range from 12 to 30 Hz.
	 * Active thinking and concentration can usually be addressed 
	 * from this range of waves.
	 * @param bins
	 * @param seconds
	 * @return
	 */
	public static double[] beta(double[]  bins, int seconds){
		int lower = 12;
		int upper = 30;
		double[] beta = createBand(bins, lower, upper, seconds);
		return beta;
	}
	
	/**
	 * Gamma waves are thought to represent binding of different 
	 * populations of neurons together into a network for the 
	 * purpose of carrying out a certain cognitive or motor function.
	 * They range from 30 to 100 Hz.
	 * @param bins
	 * @param seconds
	 * @return
	 */
	public static double[] gamma(double[]  bins, int seconds){
		int lower = 30;
		int upper = Math.min(bins.length/seconds - 1, 100);
		double[] gamma = createBand(bins, lower, upper, seconds);
		return gamma;
	}
	
	/**
	 * Computes the power of a specific band taken over a given number of seconds.
	 * @param bins
	 * @param seconds
	 * @return
	 */
	public static double bandPower(double[] bins, int seconds){
		double sum = 0.0;
		int n = EmoConst.TRIAL_FFT_SIZE;
		for(int i=0 ; i<bins.length ; i++){
			if(bins[i] != -1.0) sum += (bins[i] * bins[i]);
		}
		return sum/(seconds*n);
	}
	
	/**
	 * Produces the mean over a specific band.
	 * @param bins
	 * @return
	 */
	public static double mean(double[] bins){
		double sum = 0.0;
		for(int i=0 ; i<bins.length ; i++){
			sum += bins[i];
		}
		return sum/bins.length;
	}
	
	/**
	 * Fills the 'waves' with 'bins' from 'start' to 'stop' included.
	 * @param waves array
	 * @param bins array
	 * @param start index
	 * @param stop index
	 */
	private static void fill(double[] waves, double[] bins, int start, int stop, int seconds){
		for (int i=start*seconds ; i<=stop*seconds ; i++){
			waves[i-start*seconds] = bins[i];
		}
	}
	
	/**
	 * The array size of each band is calculated this way.
	 * @param lower
	 * @param upper
	 * @param seconds
	 * @return
	 */
	private static int size(int lower, int upper, int seconds){
		return seconds*(upper-lower)+1;
	}
	
	/**
	 * Creates a band.
	 * @param bins
	 * @param lower
	 * @param upper
	 * @param seconds
	 * @return
	 */
	private static double[] createBand(double[] bins, int lower, int upper, int seconds){
		double[] band = new double[size(lower, upper, seconds)];
		fill(band, bins, lower, upper, seconds);
		return band;
	}
}
