package constants;

public class EmoConst {
	public final static String[] SENSOR_NAMES = {"F3", "FC6", "P7", "T8", "F7", "F8", "T7", "P8", "AF4", "F4", "AF3", "O2", "O1", "FC5", "GyroX", "GyroY", "Battery"};
	public final static int F3 = 0, FC6 = 1, P7 = 2, T8 = 3, F7 = 4, F8 = 5, T7 = 6, P8 = 7, AF4 = 8, F4 = 9, AF3 = 10, O2 = 11, O1 = 12, FC5 = 13;
	public final static String[] COMMANDS = {"STOP", "UP", "DOWN", "FORWARD", "SPINLEFT"};
	//Number of sensors; note that gyroscopes and battery are included.
	public final static int NUMBER_OF_EEG_CAPS = 14;
	public final static int NUMBER_OF_SENSORS = SENSOR_NAMES.length;
	public final static int sensorDiff = Math.abs(NUMBER_OF_SENSORS - NUMBER_OF_EEG_CAPS);
	public final static String CHANNEL_ADDR_PATTERN = "/emokit/channels";
	public final static String GYRO_ADDR_PATTERN = "/emokit/gyro";
	public final static String INFO_ADDR_PATTERN = "/emokit/info";
	public final static int gyroX_index = NUMBER_OF_EEG_CAPS;
	public final static int gyroY_index = gyroX_index+1;
	public final static int battery_index = gyroY_index+1;
	public final static int SETTLE_TIME = 5;	// time to wait before learning
	public final static int FFT_SIZE_RATIO = 3;	// this ratio expresses a number of seconds
	public final static int TRIAL_FFT_SIZE = 128;
	public final static int LEARNING_FFT_SIZE = FFT_SIZE_RATIO*TRIAL_FFT_SIZE;
	public final static int SAMPLE_RATE = 128;
	public final static int TAKE_N_BANDS = 4;
	public final static int TRIAL_INTERVAL = 1;	// the interval of time between trials, in seconds
	/**
	 * Computes the number of bins the fft buffer is going to have.
	 * @return
	 */
	public static int getBinCount(boolean frameType){
		int rate = SAMPLE_RATE;
		int size;
		if(frameType) size = LEARNING_FFT_SIZE;
		else size = TRIAL_FFT_SIZE;
		double resolution = ((double) rate) / ((double) size);
		return (int) (rate/2/resolution + 1);
	}
}
