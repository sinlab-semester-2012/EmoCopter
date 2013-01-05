package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import mapping.KeyControl;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.data.FileHandler;

import oscP5.*;
import potentials.ARDRoneSpeller;
import potentials.ARDroneSpellerGrid;
import processing.core.*;
// TODO import controlP5 to improve GUI

import com.shigeodayo.ardrone.processing.ARDroneForP5;

import constants.*;

import eegUtils.gui.FFTPlot;
import eegUtils.gui.RawSignalPlot;
import eegUtils.util.FFTDataBuffer;
import emokit.EEGCap;
import emokit.EmoFrame;
import exceptions.FullFrameException;
import exceptions.WrongSensorException;

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
	private EmoFrame buffer;
	private ARDRoneSpeller speller;
	private ARDroneSpellerGrid spellerGrid; 
	//Plots the fft of a few chosen cap sensors.
	private String[] selectedSensors = {"", "", "P7", "", "", "", "", "P8", "", "", "", "", "", ""};
	//private String[] selectedSensors = {"F3", "FC6", "P7", "T8", "F7", "F8", "T7", "P8", "AF4", "F4", "AF3", "O2", "O1", "FC5"};
	private FFTPlot[] fftplot = new FFTPlot[EmoConst.NUMBER_OF_EEG_CAPS];
	private RawSignalPlot rawSignalPlotP7;
	private RawSignalPlot rawSignalPlotP8;
	private boolean plots_inited = false;

	public void setup() {
		frameWidth = displayHeight*4/3;
		frameHeight = displayHeight*3/4;
		size(frameWidth, frameHeight, OPENGL);
		noStroke();
		frameRate(25);
		init_env();
		spellerGrid = new ARDroneSpellerGrid();
		speller = new ARDRoneSpeller(spellerGrid);
		speller.start();
		
		oscP5 = new OscP5(this, 7000);
		/*ardrone=new ARDroneForP5("192.168.1.1");
		isConnected = ardrone.connect();*/
		if(isConnected){
			ardrone.connectNav();
			ardrone.connectVideo();
			ardrone.start();
		}
	}
	
	/**
	 * Initialize buffers and arrays.
	 */
	private void init_env(){
		//if(loadData) loadData();
		frameBuffer = new ArrayList<EmoFrame>();
		frameBuffer.add(new EmoFrame());
		sensorData = new DefaultDataset();
		freqData = new Dataset[EmoConst.NUMBER_OF_EEG_CAPS];
		fftBuffer = new FFTDataBuffer[EmoConst.NUMBER_OF_EEG_CAPS];
		buffer = new EmoFrame();
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
			if(sensor < EmoConst.NUMBER_OF_EEG_CAPS){
				freqData[sensor] = new DefaultDataset();
				fftBuffer[sensor] = new FFTDataBuffer(EmoConst.FFT_BUFFER_SIZE, EmoConst.SAMPLE_RATE);
			}
		}
	}
	
	/**
	 * Initialize plots.
	 */
	private void plots_init(){
		rawSignalPlotP7 = new RawSignalPlot(EmoConst.SAMPLE_RATE);
		rawSignalPlotP7.setYExtrema(EmoConst.USUALMIN, EmoConst.USUALMAX);
		rawSignalPlotP7.setSize(GUIConst.screenW, GUIConst.screenH/2-5);
		rawSignalPlotP7.setLocation(0, 10);
		rawSignalPlotP8 = new RawSignalPlot(EmoConst.SAMPLE_RATE);
		rawSignalPlotP8.setYExtrema(EmoConst.USUALMIN, EmoConst.USUALMAX);
		rawSignalPlotP8.setSize(GUIConst.screenW, GUIConst.screenH/2-5);
		rawSignalPlotP8.setLocation(0, GUIConst.screenH/2+5);
		int[][] plotsGrid = setPlotsGrid();
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_EEG_CAPS ; sensor++){
			fftplot[sensor] = new FFTPlot(EmoConst.FFT_BUFFER_SIZE, EmoConst.SAMPLE_RATE);
			fftplot[sensor].setTitle(EmoConst.SENSOR_NAMES[sensor]);
			fftplot[sensor].setSize(plotsGrid[EmoConst.NUMBER_OF_EEG_CAPS][0], plotsGrid[EmoConst.NUMBER_OF_EEG_CAPS][1]);
			fftplot[sensor].setLocation(plotsGrid[sensor][0], plotsGrid[sensor][1]);
			fftplot[sensor].setMinFrequency(1);
			fftplot[sensor].setYExtrema(0, 30);
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
	 * The received OSC message is parsed and data is collected in the buffer.
	 * @param msg
	 */
	void oscEvent(OscMessage msg){
		try{
		if(msg.checkAddrPattern(EmoConst.CHANNEL_ADDR_PATTERN)){
			String typetag = msg.typetag();
			int number_of_signals = typetag.length();
			if(number_of_signals == EmoConst.NUMBER_OF_EEG_CAPS){
				for(int sensor=0 ; sensor<number_of_signals ; sensor++){
					buffer.set(sensor, msg.get(sensor).intValue());
					fftBuffer[sensor].add(msg.get(sensor).intValue());
					fftBuffer[sensor].applyFFT();
					try {
						if(sensor < EmoConst.NUMBER_OF_EEG_CAPS)
							buffer.setFreqs(sensor, fftBuffer[sensor].getBins());
					} catch (WrongSensorException e1) {
						e1.printStackTrace();
					}
					if(showPlots) {
						if(selectedSensors[sensor].equals(EmoConst.SENSOR_NAMES[sensor])) {
							fftplot[sensor].add(buffer.getData(sensor));
						}
						try{
							if(EmoConst.SENSOR_NAMES[sensor].equals("P7")) rawSignalPlotP7.add(buffer.getData(sensor));
							if(EmoConst.SENSOR_NAMES[sensor].equals("P8")) rawSignalPlotP8.add(buffer.getData(sensor));
						} catch (Exception e){
							e.printStackTrace();
						}
					}
					if(recordData){
						try {
							freqData[sensor].add(buffer.getBinsInstance(sensor));
							recordSensorData(sensor, buffer.getData(sensor), fftBuffer[sensor].getBins());
						} catch (FullFrameException e) {
							e.printStackTrace();
						} catch (WrongSensorException e1) {
							e1.printStackTrace();
						}
					}
				}
			} else throw new IllegalArgumentException("Typetag length should be the same as the number of EEG caps.");
		} else if(msg.checkAddrPattern(EmoConst.GYRO_ADDR_PATTERN)){
			buffer.set(EmoConst.gyroX_index, msg.get(0).intValue());
			buffer.set(EmoConst.gyroY_index, msg.get(1).intValue());
		} else if(msg.checkAddrPattern(EmoConst.INFO_ADDR_PATTERN)){
			// Get battery level
			buffer.set(EmoConst.battery_index, msg.get(0).intValue());
			// Get sensor quality
			for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_EEG_CAPS ; sensor++){
				buffer.setQuality(sensor, msg.get(sensor+1).intValue());
			}
		}
		} catch(Exception e){
			e.printStackTrace();
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
			/*PImage img=ardrone.getVideoImage(false);
			if (img==null)
				return;
			image(img, frameWidth-img.width, frameHeight-img.height);*/
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
		 * TODO Get speller data
		 */

		/*
		 * Drawing EPOC data and info
		 */
		text("Sensor\nNames", GUIConst.margin+40, GUIConst.dataPos-35);
		text("Levels", GUIConst.margin+85, GUIConst.dataPos-35);
		for (int i=0 ; i<EmoConst.NUMBER_OF_EEG_CAPS ; i++){
			text(EmoConst.SENSOR_NAMES[i], GUIConst.margin+40, GUIConst.dataPos+i*12);
			text(" : " + buffer.getData(i), GUIConst.margin+80, GUIConst.dataPos+i*12);
		}
		if(showContactQuality){
			text("Contact\nQuality", GUIConst.margin-10, GUIConst.dataPos-35);
			for (int i=0 ; i<EmoConst.NUMBER_OF_EEG_CAPS ; i++){
				text(buffer.getQuality(i), GUIConst.margin, GUIConst.dataPos+i*12);
			}
		}
		if(showGyro){
			stroke(GUIConst.white);
			strokeWeight(2);
			//GyroX:
			rect(GUIConst.gyroXx, GUIConst.gyroXy, -buffer.getData(EmoConst.gyroX_index), GUIConst.stdThickness);
			line(GUIConst.gyroXx, GUIConst.gyroXy-5, GUIConst.gyroXx, GUIConst.gyroXy+GUIConst.stdThickness+5);
			//GyroY:
			rect(GUIConst.gyroYx, GUIConst.gyroYy, GUIConst.stdThickness, buffer.getData(EmoConst.gyroY_index));
			line(GUIConst.gyroYx-5, GUIConst.gyroYy, GUIConst.gyroYx+GUIConst.stdThickness+5, GUIConst.gyroYy);
		}
		if(showBattery){
			text(buffer.getData(EmoConst.battery_index) + "%", GUIConst.batteryX+GUIConst.batterySize+5, GUIConst.batteryY+GUIConst.stdThickness/2);
			strokeWeight(1);
			stroke(GUIConst.white);
			fill(GUIConst.white);
			rect(GUIConst.batteryX+1, GUIConst.batteryY+1, (buffer.getData(EmoConst.battery_index)*(GUIConst.batterySize-2)/100), (GUIConst.stdThickness-4)/2);
			noFill();
			rect(GUIConst.batteryX, GUIConst.batteryY, GUIConst.batterySize, GUIConst.stdThickness/2);
		}
		if(showHelp){
			String help = "-- HOW TO USE --\n" +
					"  GUI:\n" +
					"    '0': show all\n" +
					"    '6': show Battery\n" +
					"    '7': show Gyro\n" +
					"    '8': show Help\n" +
					"    'p': show FFT Plots\n" +
					"    '9': show contact quality\n" +
					"  Controls:\n" +
					"    Arrow UP: forward\n" +
					"    Arrow DOWN: backward\n" +
					"    Arrow LEFT: go Left\n" +
					"    Arrow RIGHT: go Right\n" +
					"    SHIFT: take off\n" +
					"    CTRL: land\n" +
					"    'q': stop\n" +
					"    'd': spin right\n" +
					"    'a': spin left\n" +
					"    'w': go up\n" +
					"    's': go down\n" +
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
		
		if(isConnected) KeyControl.map(ardrone, key, keyCode);
		switch (key){
		case ENTER:
			recordData ^= true;
			if(!recordData) saveDataset();
			break;
		case '0':	/* show all */
			boolean show = !(showBattery && showGyro && showHelp && showContactQuality);
			showBattery = show;
			showGyro = show;
			showHelp = show;
			showContactQuality = show;
			break;
		case '6':	showBattery ^= true; break;
		case '7':	showGyro ^= true; break;
		case '8':	showHelp ^= true; break;
		case 'p':
			if(!plots_inited){
				plots_init();
				plots_inited = true;
			}
			showPlots ^= true;
			for(int i = 0 ; i < fftplot.length ; i++){
				fftplot[i].setVisible(showPlots && selectedSensors[i].equals(EmoConst.SENSOR_NAMES[i]));
			}
			rawSignalPlotP7.setVisible(showPlots);
			rawSignalPlotP8.setVisible(showPlots);
			break;
		case '9':	showContactQuality ^= true; break;
		}
	}
	
	/**
	 * Makes sure the drone stops as soon as no key is pressed.
	 */
	public void keyReleased(){
		if(isConnected) KeyControl.map(ardrone, WAIT, keyCode);
	}
}
