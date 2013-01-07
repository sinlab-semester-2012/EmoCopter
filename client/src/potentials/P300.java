package potentials;

/**
 * The idea here is to cluster all information regarding a trial at P300.
 * This information should be provided to the speller, which will in turn
 * know which selection to make.
 */
public class P300{
	// Minimum number of trials before having an acceptable move
	public static final int minimumNumberOfTrials = 10;	// TODO TBD on experiment basis
	// Number of trials per second
	public static final long trialFrequency = 64;		// [Hz] TODO has to be defined also
	public static final int minLatency = 300;			// [ms]
	public static final int maxLatency = 600;			// [ms]
	public static final int minVoltDeflection = 2;	// [uV]
	public static final int maxVoltDeflection = 5;	// [uV]
	public static final String[] sensors = {"P7", "P8"};	// Parietal lobe
}

