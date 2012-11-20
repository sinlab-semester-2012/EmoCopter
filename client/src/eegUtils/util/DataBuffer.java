package eegUtils.util;

import java.util.Arrays;

public abstract class DataBuffer {
	
	protected int index = 0;
	protected int size = 0;
	
	protected int capacity;
	
	// the scale for incoming data
	protected float scale = 1.0f;
	
	public void setScale(float scale) {
		this.scale = scale;
	}
	
	public float getScale() {
		return this.scale;
	}
	
	public DataBuffer(int capacity) {
		this.capacity = capacity;
	}
	
	public static class Float extends DataBuffer {

		public float[] data;
		
		public Float(int capacity) {
			super(capacity);
			
			data = new float[capacity];
			Arrays.fill(data, 0);
		}
				
		public synchronized void add(float value) {
			data[index] = value * scale;
			index = (index+1) % capacity;
			size = Math.min(size+1, capacity);
		}
		
		public void getData(float[] target) {
			for(int i=0; i<capacity; i++) {
				target[i] = data[((index%size) + i) % capacity];
			}
		}		
	}
	
	public static class Double extends DataBuffer {
		
		public double[] data;
		
		public Double(int capacity) {
			super(capacity);
			
			data = new double[capacity];
			Arrays.fill(data, 0);
		}
		
		public void add(double value) {
			data[index] = value * scale;
			index = (index+1) % capacity;
			size = Math.min(size+1, capacity);
		}
		
		public void getData(double[] target) {
			if(size <= 0) {
				return;
			}
			
			for(int i=0; i<capacity; i++) {
				target[i] = data[((index%size) + i) % capacity];
			}
		}
	}
}
