package eegUtils.util;

public abstract class WindowedDataBuffer extends DataBuffer {
	
	public enum Window {RECTANGLE, HANN, HAMMING, BLACKMAN, BLACKMAN_HARRIS};
	
	public static float BLACKMAN_ALPHA = 0.16f;
	
	public WindowedDataBuffer(int capacity) {
		super(capacity);
	}
	
	public static class Float extends DataBuffer.Float {
		
		public Float(int capacity) {
			super(capacity);			
		}
		
		public void getData(float[] target, Window window) {
			if(size <= 0) {
				return;
			}
			
			for(int i=0; i<capacity; i++) {
				target[i] = applyWindow(data[((index%size) + i) % capacity], i, window);
			}
		}
		
		private float applyWindow(float value, int n, Window window) {
			switch(window) {
				case HANN:
					return (float) (value * (0.5 * (1 - Math.cos((2 * Math.PI * n) / (capacity - 1)))));
				case HAMMING:
					return (float) (value * (0.54 - 0.46 * Math.cos((2 * Math.PI * n) / (capacity - 1))));
				case BLACKMAN:
					return (float) (value * ((1 - BLACKMAN_ALPHA) / 2 - 0.5 * Math.cos((2 * Math.PI * n) / (capacity - 1)) + (BLACKMAN_ALPHA / 2) * Math.cos((4 * Math.PI * n) / (capacity - 1))));
				case BLACKMAN_HARRIS:
					return (float) (value * (0.35875f - 0.48829f * Math.cos((2 * Math.PI * n) / (capacity - 1)) + 0.14128f * Math.cos((4 * Math.PI * n) / (capacity - 1)) - 0.01168f * Math.cos((6 * Math.PI * n) / (capacity - 1))));
				default:
					return value;
			}
		}
	}
	
	public static class Double extends DataBuffer.Float {
		
		public Double(int capacity) {
			super(capacity);
		}
				
		public void getData(double[] target, Window window) {
			for(int i=0; i<capacity; i++) {
				target[i] = applyWindow(data[((index%size) + i) % capacity], i, window);
			}
		}
		
		private double applyWindow(double value, int n, Window window) {
			switch(window) {
				case HANN:
					return value * (0.5 * (1 - Math.cos((2 * Math.PI * n) / (capacity - 1))));
				case HAMMING:
					return value * (0.54 - 0.46 * Math.cos((2 * Math.PI * n) / (capacity - 1)));
				case BLACKMAN:
					return value * ((1 - BLACKMAN_ALPHA) / 2 - 0.5 * Math.cos((2 * Math.PI * n) / (capacity - 1)) + (BLACKMAN_ALPHA / 2) * Math.cos((4 * Math.PI * n) / (capacity - 1)));
				case BLACKMAN_HARRIS:
					return value * (0.35875f - 0.48829f * Math.cos((2 * Math.PI * n) / (capacity - 1)) + 0.14128f * Math.cos((4 * Math.PI * n) / (capacity - 1)) - 0.01168f * Math.cos((6 * Math.PI * n) / (capacity - 1)));
				default:
					return value;
			}
		}
	}
}
