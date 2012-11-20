package eegUtils.util;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import eegUtils.util.WindowedDataBuffer.Window;

public class FFTDataBuffer {
	
	// use this window
	private Window window = Window.HANN;
		
	// the size of the buffer and fft
	private int size;
	
	// the sample rate of the incomming samples
	private int rate;
	
	// the resolution of the incomming samples
	private double resolution;
	
	// min and max frequency
	protected int min;
	protected int max;
	
	// WindowedDataBuffer contains incomming samples and handles windowing
	private WindowedDataBuffer.Float buffer;

	// the FFT
	private FloatFFT_1D fft;
	
	// target array for performing fft
	private float[] target;
	
	// bin array
	private double[] bins;
	
	// average magnitude of frequencies in bins
	private double magnitude = 0d;
	
	public FFTDataBuffer(int size, int rate) {
		this.size = size;
		this.rate = rate;
		
		this.resolution = ((double) rate) / ((double) size);
		
		this.min = 0;
		this.max = rate/2;
		
		buffer = new WindowedDataBuffer.Float(size);
		fft = new FloatFFT_1D(size);

		target = new float[size];
		bins = new double[getBinCount()];
	}
	
	public void setScale(float scale) {
		buffer.setScale(scale);
	}
	
	public float getScale() {
		return buffer.getScale();
	}
	
	public void setWindow(Window window) {
		this.window = window;
	}
	
	public Window getWindow() {
		return this.window;
	}
	
	public void setFrequencyRange(int min, int max) {
		this.setMinFrequency(min);
		this.setMaxFrequency(max);
	}
	
	public void setMinFrequency(int min) {
		this.min = Math.max(min, 0);
	}
	
	public void setMaxFrequency(int max) {
		this.max = Math.min(max, this.rate / 2);
	}
	
	public int getMinFrequency() {
		return this.min;
	}
	
	public int getMaxFrequency() {
		return this.max;
	}
	
	public double getFrequencyResolution() {
		return this.resolution;
	}
	
	public int getBinCount() {
		return (int) ((max - min) / resolution + 1);
	}
	
	public double[] getBins() {
		return this.bins;
	}
	
	public double getAverageMagnitude() {
		return this.magnitude;
	}
	
	public double getMagnitude(double frequency) {
		if (frequency < min || frequency > max) {
			return 0d;
		}

		if(frequency % resolution != 0) {
			double offset = frequency % resolution;

			double value1 = bins[(int) (((frequency - offset) - min) / resolution)];
			double value2 = bins[(int) (((frequency - offset + resolution) - min) / resolution)];

			double scale1 = 1 - offset / resolution;
			double scale2 = offset / resolution;

			return (scale1 * value1) + (scale2 * value2);
		}

		return bins[(int) ((frequency - min) / resolution)];
	}
	
	public void add(float value) {
		buffer.add(value);
	}
	
	public void applyFFT() {
		int binCount = getBinCount();
		double averageMagnitude = 0;
		
		bins = new double[binCount];
		
		// get data from buffer
		buffer.getData(target, window);
		
		// perform fft
		fft.realForward(target);
		
		// get the values between the min and max frequencies
		for(double f = min, i = 0; f < max; f += resolution, i++) {
			int index = (int) ((f / resolution)) * 2;
			
			// magnitude of frequency
			bins[(int) i] = Math.sqrt(target[index]*target[index] + target[index+1]*target[index+1]) / (size / 2);

			averageMagnitude += bins[(int) i];
		}
		
		// update magnitude
		magnitude = averageMagnitude / ((double) binCount);
	}
}
