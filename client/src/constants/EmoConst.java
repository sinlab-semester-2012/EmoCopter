package constants;

public class EmoConst {
	public final static String[] SENSOR_NAMES = {"F3", "FC6", "P7", "T8", "F7", "F8", "T7", "P8", "AF4", "F4", "AF3", "O2", "O1", "FC5", "GyroX", "GyroY", "Battery"};
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
	//Precision level will determine how many samples are required for the base level mean.
	//public final static int MEAN_PRECISION = 40;
	//Precision rate defines how many times a sample is taken per second.
	//public final static int PRECISION_RATE = 4;
	public final static int FFT_BUFFER_SIZE = 128;
	public final static int SAMPLE_RATE = 128;
	public final static int USUALMIN = 7000;
	public final static int USUALMAX = 10000;
}
