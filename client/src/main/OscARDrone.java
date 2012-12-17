package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.data.FileHandler;

import oscP5.*;
import processing.core.*;
// TODO import controlP5 to improve GUI
import sensors.EEGCap;
import sensors.Sensor;

import com.shigeodayo.ardrone.processing.ARDroneForP5;

import constants.*;

import eegUtils.gui.FFTPlot;
import eegUtils.util.FFTDataBuffer;
import exceptions.FullFrameException;
import exceptions.WrongSensorException;
import frame.*;

public class OscARDrone extends PApplet{
	private static final long serialVersionUID = 1L;
	private static final String user = "vince";
	OscP5 oscP5;
	private String savedData = "emocopter_vince.data";
	private ARDroneForP5 ardrone;
	private boolean isConnected = false;
	private boolean showBattery = false;
	private boolean showContactQuality = false;
	private boolean showHelp = true;
	private boolean showPlots = false;
	private boolean showGyro = false;
	private boolean recordData = false;	//TODO
	private boolean loadData = false;		//TODO
	private boolean saveData = false;		//TODO
	private int frameWidth;
	private int frameHeight;
	private ArrayList<EmoFrame> frameBuffer;
	private int frameBufferIndex = 0;
	private Dataset sensorData;
	private Dataset[] freqData;
	private FFTDataBuffer[] fftBuffer;
	private Sensor[] buffer;
	//Plots the fft of a few chosen cap sensors.
	private String[] selectedSensors = {"F3", "FC6", "P7", "T8", "F7", "F8", "T7", "P8", "AF4", "F4", "AF3", "O2", "O1", "FC5"};
	private FFTPlot[] fftplot = new FFTPlot[EmoConst.NUMBER_OF_EEG_CAPS];
	private boolean plots_inited = false;

	public void setup() {
		frameWidth = displayHeight*4/3;
		frameHeight = displayHeight*3/4;
		size(frameWidth, frameHeight, OPENGL);
		noStroke();
		frameRate(25);
		arrays_init();
		
		oscP5 = new OscP5(this, 7000);
		//ardrone=new ARDroneForP5("192.168.1.1");
		//isConnected = ardrone.connect();
		if(isConnected){
			ardrone.connectNav();
			ardrone.connectVideo();
			ardrone.start();
		}
	}
	
	/**
	 * Initialize buffers and arrays.
	 */
	private void arrays_init(){
		//if(loadData) loadData();
		frameBuffer = new ArrayList<EmoFrame>();
		frameBuffer.add(new EmoFrame());
		sensorData = new DefaultDataset();
		freqData = new Dataset[EmoConst.NUMBER_OF_EEG_CAPS];
		fftBuffer = new FFTDataBuffer[EmoConst.NUMBER_OF_EEG_CAPS];
		buffer = new Sensor[EmoConst.NUMBER_OF_SENSORS];
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
			if(sensor < EmoConst.NUMBER_OF_EEG_CAPS){
				freqData[sensor] = new DefaultDataset();
				fftBuffer[sensor] = new FFTDataBuffer(EmoConst.FFT_BUFFER_SIZE, EmoConst.SAMPLE_RATE);
				buffer[sensor] = new EEGCap(EmoConst.SENSOR_NAMES[sensor]);
			} else buffer[sensor] = new Sensor(EmoConst.SENSOR_NAMES[sensor]);
		}
	}
	
	/**
	 * Initialize fft plots.
	 */
	private void plots_init(){
		int[][] plotsGrid = setPlotsGrid();
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_EEG_CAPS ; sensor++){
			fftplot[sensor] = new FFTPlot(EmoConst.FFT_BUFFER_SIZE, EmoConst.SAMPLE_RATE);
			fftplot[sensor].setTitle(EmoConst.SENSOR_NAMES[sensor]);
			fftplot[sensor].setSize(plotsGrid[EmoConst.NUMBER_OF_EEG_CAPS][0], plotsGrid[EmoConst.NUMBER_OF_EEG_CAPS][1]);
			fftplot[sensor].setLocation(plotsGrid[sensor][0], plotsGrid[sensor][1]);
			fftplot[sensor].setMinFrequency(2);
		}
	}
	
	/**
	 * Computes and returns a grid for fftplots.
	 * The last two elements of the returned array are the width and height of the plots.
	 * @return a 2D integer array containing x and y coordinates for each plot and their size.
	 */
	private int[][] setPlotsGrid(){
		int width = 300;
		int height = 200;
		int n = EmoConst.NUMBER_OF_EEG_CAPS;
		java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int x = 0, y = 10;
		int[][] grid = new int[n+1][2];
		for (int i = 0 ; i < n ; i++){
			if (x > dim.width - width) {
				y += height;
				x = 0;
			}
			grid[i][0] = x;
			grid[i][1] = y;
			x += width;
		}
		grid[n] = new int[]{width, height};
		return grid;
	}
	
	/**
	 * Saves the sensor data as well as each specific sensor frequency data.
	 */
	private void saveDataset(){	// TODO no values or zero values
		try {
			long filename_ext = (new java.util.Date()).getTime();
			FileHandler.exportDataset(sensorData, new File("emocopter_"+user+"_sensor_"+filename_ext+".data"));
			for (int sensor=0 ; sensor<EmoConst.NUMBER_OF_EEG_CAPS ; sensor++){
				String sensorName = EmoConst.SENSOR_NAMES[sensor];
				FileHandler.exportDataset(freqData[sensor], new File("emocopter_"+user+"_freq_"+sensorName+"_"+filename_ext+".data"));
			}
		} catch (IOException e) {
			System.out.println("Couldn't create file, check for permissions.");
			e.printStackTrace();
		}
	}
	
	private void loadDataset(){
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showOpenDialog(this);
			sensorData = FileHandler.loadDataset(fileChooser.getSelectedFile(), 0, ",");
		} catch (IOException e) {
			System.out.println("No data to load, you should start from scratch.");
			e.printStackTrace();
		}
	}

	/**
	 * Save data for this session.
	 */
	/*private void saveData(){
		String[] dataTmp = new String[epocData.size()];
		for(int frame=0 ; frame<epocData.size() ; frame++){
			dataTmp[frame] = new String();
			for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
				dataTmp[frame] = dataTmp[frame] + epocData.get(frame).getData(sensor) + " ";
			}
		}
		saveStrings(savedData, dataTmp);
	}*/
	
	/**
	 * Load saved data from a previous session.
	 */
	/*private void loadData(){
		String[] dataTmp = loadStrings(savedData);
		if(dataTmp != null){
			for(int frame=0 ; frame<dataTmp.length ; frame++){
				StringTokenizer tokens = new StringTokenizer(dataTmp[frame], " ");
				int numberOfSensors = tokens.countTokens();
				if(numberOfSensors > EmoConst.NUMBER_OF_SENSORS) throw new TooManySensorsException(numberOfSensors);
				Sensor[] sensors = new Sensor[numberOfSensors];
				for(int sensor=0 ; sensor<numberOfSensors ; sensor++) {
					sensors[sensor] = new Sensor(EmoConst.SENSOR_NAMES[sensor], Integer.parseInt(tokens.nextToken()));
				}
				epocData.add(new EmoFrame(sensors));
			}
		}
	}*/

	/*
	private void doBaseLevels(){
		float[][] tmp = new float[EmoConst.MEAN_PRECISION][EmoConst.NUMBER_OF_SENSORS];
		for(int precision=0 ; precision<EmoConst.MEAN_PRECISION ; precision++){
			java.util.Date time0 = new java.util.Date();
			for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
				tmp[precision][sensor] = buffer[sensor].value();
			}
			if(precision<EmoConst.MEAN_PRECISION-1){
				while(new java.util.Date().getTime()-time0.getTime() < 1000/EmoConst.PRECISION_RATE){}
			}
		}
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
			for(int precision=0 ; precision<EmoConst.MEAN_PRECISION ; precision++){
				baseLevels[sensor] += tmp[precision][sensor];
			}
			baseLevels[sensor] = Math.round(baseLevels[sensor] / EmoConst.MEAN_PRECISION);
		}
	}*/
	
	/**
	 * This is used to record new data.
	 * Given a String index 'sensor', the 'data' is attributed to the
	 * correct frame position.
	 * 
	 * @param data to be inserted
	 * @param sensor name
	 * @throws FullFrameException 
	 */
	private void recordSensorData(int sensor, int data, double[] freqBins) throws FullFrameException{
		EmoFrame currentFrame = frameBuffer.get(frameBufferIndex);
		if(currentFrame.isFull()){
			sensorData.add(currentFrame.getInstance(false));
			frameBufferIndex+=1;
			frameBuffer.add(new EmoFrame());
			recordSensorData(sensor, data, freqBins);
		} else {
			currentFrame.put(new EEGCap(sensor, data, freqBins));
		}
	}
	
	/**
	 * Inherited oscEvent method, reacts to OSC messages.
	 * The received message is parsed for address pattern and then the data is collected
	 * in the buffer.
	 * @param msg
	 */
	void oscEvent(OscMessage msg){
		if(msg.checkAddrPattern(EmoConst.CHANNEL_ADDR_PATTERN)){
			String typetag = msg.typetag();
			int number_of_signals = typetag.length();
			if(number_of_signals == EmoConst.NUMBER_OF_EEG_CAPS){
				for(int sensor=0 ; sensor<number_of_signals ; sensor++){
					buffer[sensor].updateValue(msg.get(sensor).intValue());
					fftBuffer[sensor].add(msg.get(sensor).intValue());
					fftBuffer[sensor].applyFFT();
					try {
						buffer[sensor].setFreqs(fftBuffer[sensor].getBins());
						println(buffer[sensor].getBinsInstance());
					} catch (WrongSensorException e1) {
						e1.printStackTrace();
					}
					if(showPlots && selectedSensors[sensor] == EmoConst.SENSOR_NAMES[sensor])
						fftplot[sensor].add(buffer[sensor].value());
					if(recordData){
						try {
							freqData[sensor].add(buffer[sensor].getBinsInstance());
							recordSensorData(sensor, buffer[sensor].value(), fftBuffer[sensor].getBins());
						} catch (FullFrameException e) {
							e.printStackTrace();
						} catch (WrongSensorException e1) {
							e1.printStackTrace();
						}
					}
				}
			} else throw new IllegalArgumentException("Typetag length should be the same as the number of EEG caps.");
		} else if(msg.checkAddrPattern(EmoConst.GYRO_ADDR_PATTERN)){
			buffer[EmoConst.gyroX_index].updateValue(msg.get(0).intValue());
			buffer[EmoConst.gyroY_index].updateValue(msg.get(1).intValue());
		} else if(msg.checkAddrPattern(EmoConst.INFO_ADDR_PATTERN)){
			// Get battery level
			buffer[EmoConst.battery_index].updateValue(msg.get(0).intValue());
			// Get sensor quality
			for(int i=0 ; i<EmoConst.NUMBER_OF_EEG_CAPS ; i++){
				((EEGCap)buffer[i]).updateQuality(msg.get(i+1).intValue());
			}
		}
	}

	/**
	 * Inherited draw method.
	 */
	public void draw(){
		background(0);  
		ambientLight(64, 64, 64);
		lightSpecular(255, 255, 255);
		directionalLight(224, 224, 224, (float) .5, 1, -1);

		/*
		 * Drawing AR.Drone info
		 */
		if(isConnected){
			PImage img=ardrone.getVideoImage(false);
			if (img==null)
				return;
			image(img, frameWidth-img.width, frameHeight-img.height);
			float pitch=ardrone.getPitch();
			float roll=ardrone.getRoll();
			float yaw=ardrone.getYaw();
			float altitude=ardrone.getAltitude();
			float[] velocity=ardrone.getVelocity();
			int battery=ardrone.getBatteryPercentage();
			String attitude="pitch:"+pitch+"\nroll:"+roll+"\nyaw:"+yaw+"\naltitude:"+altitude;
			text(attitude, GUIConst.margin, 35);
			String vel="vx:"+velocity[0]+"\nvy:"+velocity[1];
			text(vel, GUIConst.margin, 90);
			String bat="battery:"+battery+" %";
			text(bat, GUIConst.margin, 120);
		} else {
			text("NO ARDRONE CONNECTION", 40, 80);
		}

		/*
		 * Drawing EPOC data
		 */
		text("Sensor\nNames", GUIConst.margin+40, GUIConst.dataPos-35);
		text("Levels", GUIConst.margin+85, GUIConst.dataPos-35);
		for (int i=0 ; i<EmoConst.NUMBER_OF_EEG_CAPS ; i++){
			text(EmoConst.SENSOR_NAMES[i], GUIConst.margin+40, GUIConst.dataPos+i*12);
			text(" : " + buffer[i].value(), GUIConst.margin+80, GUIConst.dataPos+i*12);
		}
		if(showContactQuality){
			text("Contact\nQuality", GUIConst.margin-10, GUIConst.dataPos-35);
			for (int i=0 ; i<EmoConst.NUMBER_OF_EEG_CAPS ; i++){
				text(((EEGCap)buffer[i]).quality(), GUIConst.margin, GUIConst.dataPos+i*12);
			}
		}
		if(showGyro){
			stroke(GUIConst.white);
			strokeWeight(2);
			//GyroX:
			rect(GUIConst.gyroXx, GUIConst.gyroXy, -buffer[EmoConst.gyroX_index].value(), GUIConst.stdThickness);
			line(GUIConst.gyroXx, GUIConst.gyroXy-5, GUIConst.gyroXx, GUIConst.gyroXy+GUIConst.stdThickness+5);
			//GyroY:
			rect(GUIConst.gyroYx, GUIConst.gyroYy, GUIConst.stdThickness, buffer[EmoConst.gyroY_index].value());
			line(GUIConst.gyroYx-5, GUIConst.gyroYy, GUIConst.gyroYx+GUIConst.stdThickness+5, GUIConst.gyroYy);
		}
		if(showBattery){
			text(buffer[EmoConst.battery_index].value() + "%", GUIConst.batteryX+GUIConst.batterySize+5, GUIConst.batteryY+GUIConst.stdThickness/2);
			strokeWeight(1);
			stroke(GUIConst.white);
			fill(GUIConst.white);
			rect(GUIConst.batteryX+1, GUIConst.batteryY+1, (buffer[EmoConst.battery_index].value()*(GUIConst.batterySize-2)/100), (GUIConst.stdThickness-4)/2);
			noFill();
			rect(GUIConst.batteryX, GUIConst.batteryY, GUIConst.batterySize, GUIConst.stdThickness/2);
		}
		if(showHelp){
			String help = "-- HOW TO USE --\n" +
					"  GUI:\n" +
					"    'a': show all\n" +
					"    'b': show Battery\n" +
					"    'g': show Gyro\n" +
					"    'h': show Help\n" +
					"    'p': show FFT Plots\n" +
					"    'q': show contact quality\n" +
					"  Controls:\n" +
					"    Arrow UP: forward\n" +
					"    Arrow DOWN: backward\n" +
					"    Arrow LEFT: go Left\n" +
					"    Arrow RIGHT: go Right\n" +
					"    SHIFT: take off\n" +
					"    CTRL: land\n" +
					"    's': stop\n" +
					"    'r': spin right\n" +
					"    'l': spin left\n" +
					"    'u': go up\n" +
					"    'd': go down\n" +
					"    '1': set Horizontal Camera\n" +
					"    '2': set Horizontal Camera With Vertical\n" +
					"    '3': set Vertical Camera\n" +
					"    '4': set Vertical Camera With Horizontal\n" +
					"    '5': toggle Camera\n" +
					"  Data:\n" +
					"    ENTER: starts recording data and saves it after a second press.\n";
			text(help, GUIConst.helpX, GUIConst.helpY);
		}
	}

	/**
	 * Overrides stop method. This is compulsory in the case where saving data is needed.
	 */
	public void stop(){
		if(recordData) {
			recordData = false;
			saveDataset();
		}
		super.stop();
	}

	/**
	 * Keyboard command interpreter.
	 */
	public void keyPressed() {
		switch (key){
		case CODED:
			if(!isConnected) break;
			switch(keyCode) {
			case UP:		ardrone.forward(50); break;
			case DOWN:		ardrone.backward(50); break;
			case LEFT:		ardrone.goLeft(100); break;
			case RIGHT:		ardrone.goRight(100); break;
			case SHIFT:		ardrone.takeOff(); break;
			case CONTROL:	ardrone.landing(); break;
			}
			break;
		case ENTER:
			recordData ^= true;
			if(!recordData) saveDataset();
			break;
		case 'a':	/* show all */
			boolean show = !(showBattery && showGyro && showHelp && showContactQuality);
			showBattery = show;
			showGyro = show;
			showHelp = show;
			showContactQuality = show;
			break;
		case 'b':	showBattery ^= true; break;
		case 'g':	showGyro ^= true; break;
		case 'h':	showHelp ^= true; break;
		case 'p':
			if(!plots_inited){
				plots_init();
				plots_inited = true;
			}
			showPlots ^= true;
			for(int i = 0 ; i < fftplot.length ; i++){
				fftplot[i].setVisible(showPlots);
			}
			break;
		case 'q':	showContactQuality ^= true; break;
		default:	if(!isConnected) break;
		switch(key) {
			case 's':	ardrone.stop(); break;
			case 'r':	ardrone.spinRight(10); break;
			case 'l':	ardrone.spinLeft(10); break;
			case 'u':	ardrone.up(10); break;
			case 'd':	ardrone.down(10); break;
			case '1':	ardrone.setHorizontalCamera(); break;
			case '2':	ardrone.setHorizontalCameraWithVertical(); break;
			case '3':	ardrone.setVerticalCamera(); break;
			case '4':	ardrone.setVerticalCameraWithHorizontal(); break;
			case '5':	ardrone.toggleCamera(); break;
			}
		}
	}
}
