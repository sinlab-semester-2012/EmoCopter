package waves;

/**
 * This class provides static methods parsing pre-processed waves.
 * This is useful only with an fft'ed signal.
 * It is assumed that the number of bins is at least 65 (64 Hz).
 * For more information on brain wave patterns, visit:
 * http://en.wikipedia.org/wiki/Electroencephalography#Wave_patterns
 */
public class Patterns {
	public final static int maxBin = 64;
	
	/**
	 * Delta waves are usually associated with deep sleep or lack
	 * of consciousness. They range from 0 to 4 Hz.
	 * @param bins
	 * @return
	 */
	public static double[] delta(double[] bins){
		double[] delta = new double[5];
		fill(delta, bins, 0, 4);
		return delta;
	}
	
	/**
	 * Theta waves can be seen in drowsiness or arousal, or also
	 * during meditation.
	 * They range from 4 to 7 Hz.
	 * @param bins
	 * @return
	 */
	public static double[] theta(double[] bins){
		double[] theta = new double[4];
		fill(theta, bins, 4, 7);
		return theta;
	}
	
	/**
	 * Alpha waves are associated with relaxation.
	 * They range from 8 to 12 Hz.
	 * @param bins
	 * @return
	 */
	public static double[] alpha(double[] bins){
		double[] alpha = new double[5];
		fill(alpha, bins, 8, 12);
		return alpha;
	}
	
	/**
	 * Beta waves range from 12 to 30 Hz.
	 * Active thinking and concentration can usually be addressed 
	 * from this range of waves.
	 * @param bins
	 * @return
	 */
	public static double[] beta(double[] bins){
		double[] beta = new double[19];
		fill(beta, bins, 12, 30);
		return beta;
	}
	
	/**
	 * Gamma waves are thought to represent binding of different 
	 * populations of neurons together into a network for the 
	 * purpose of carrying out a certain cognitive or motor function.
	 * They range from 30 to 100 Hz.
	 * @param bins
	 * @return
	 */
	public static double[] gamma(double[] bins){
		double[] gamma = new double[71];
		fill(gamma, bins, 30, maxBin);
		int top = Math.min(101, bins.length);
		for(int i=maxBin+1 ; i<top ; i++){
			gamma[i] = bins[i];
		}
		if(top <= 100){
			for(int i=top ; i<101 ; i++){
				gamma[i] = -1;
			}
		}
		return gamma;
	}
	
	/**
	 * Computes the power of a specific band.
	 * @param bins
	 * @return
	 */
	public static double power(double[] bins){
		double sum = 0.0;
		for(int i=0 ; i<bins.length ; i++){
			if(bins[i] != -1.0) sum += (bins[i] * bins[i]);
		}
		return sum;
	}
	
	/**
	 * Produces the mean over a specific band.
	 * @param bins
	 * @return
	 */
	public static double mean(double[] bins){
		int validBinsN = 0;
		double sum = 0.0;
		for(int i=0 ; i<bins.length ; i++){
			if(bins[i] != -1.0) {
				validBinsN += 1;
				sum += bins[i];
			}
		}
		return sum/validBinsN;
	}
	
	/**
	 * Fills the 'waves' with 'bins' from 'start' to 'stop' included.
	 * @param waves array
	 * @param bins array
	 * @param start index
	 * @param stop index
	 */
	private static void fill(double[] waves, double[] bins, int start, int stop){
		for (int i=start ; i<=stop ; i++){
			waves[i-start] = bins[i];
		}
	}
}
