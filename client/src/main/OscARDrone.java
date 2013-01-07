package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import mapping.Command;
import mapping.ConsciousControl;
import mapping.KeyControl;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.*;
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
	//private String pathToDatasets = Thread.currentThread().getContextClassLoader().getResource("../datasets/").toString();
	private String pathToDatasets = "datasets/";
	private String pathToTrainingData = "data/";
	// TODO update to ARDroneForP5 2.0, see Nikita's work: https://github.com/MGrin/Quadrokinect
	private ARDroneForP5 ardrone;
	private boolean isConnected = false;
	private boolean showBattery = false;
	private boolean showContactQuality = false;
	private boolean showHelp = true;
	private boolean showPlots = false;
	private boolean showGyro = false;
	private boolean recordData = false;
	private boolean listen = false;
	private long timeReference;
	private boolean[] literateClassifiers;
	private int frameWidth;
	private int frameHeight;
	private ArrayList<EmoFrame> frameBuffer;
	private int frameBufferIndex = 0;
	private Dataset sensorData;
	private Dataset[] freqData;
	private Classifier classifier;
	private Dataset learningData;
	private boolean classifierIsLiterate = false;
	private FFTDataBuffer[] fftBuffer;
	private EmoFrame buffer;
	private ARDRoneSpeller speller;
	private ARDroneSpellerGrid spellerGrid; 
	//Plots the fft of a few chosen cap sensors.
	//private String[] selectedSensors = {"", "", "P7", "", "", "", "", "P8", "", "", "", "", "", ""};
	private String[] selectedSensors = {"F3", "FC6", "P7", "T8", "F7", "F8", "T7", "P8", "AF4", "F4", "AF3", "O2", "O1", "FC5"};
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
		/*spellerGrid = new ARDroneSpellerGrid();
		speller = new ARDRoneSpeller(spellerGrid);
		speller.start();*/
		
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
		frameBuffer.add(new EmoFrame(false));
		sensorData = new DefaultDataset();
		freqData = new Dataset[EmoConst.NUMBER_OF_EEG_CAPS];
		classifier = new KNearestNeighbors(EmoConst.COMMANDS.length + 1);
		learningData = new DefaultDataset();
		literateClassifiers = new boolean[EmoConst.COMMANDS.length];
		for(int i=0 ; i<EmoConst.COMMANDS.length ; i++) literateClassifiers[i] = false;
		fftBuffer = new FFTDataBuffer[EmoConst.NUMBER_OF_EEG_CAPS];
		buffer = new EmoFrame(false);
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
			if(sensor < EmoConst.NUMBER_OF_EEG_CAPS){
				freqData[sensor] = new DefaultDataset();
				fftBuffer[sensor] = new FFTDataBuffer(EmoConst.TRIAL_FFT_SIZE, EmoConst.SAMPLE_RATE);
			}
		}
	}
	
	/**
	 * Initialize plots.
	 */
	private void plots_init(){
		rawSignalPlotP7 = new RawSignalPlot(EmoConst.SAMPLE_RATE);
		rawSignalPlotP7.setYExtrema(GUIConst.usualMin, GUIConst.USUALMAX);
		rawSignalPlotP7.setSize(GUIConst.screenW, GUIConst.screenH/2-5);
		rawSignalPlotP7.setLocation(0, 10);
		rawSignalPlotP8 = new RawSignalPlot(EmoConst.SAMPLE_RATE);
		rawSignalPlotP8.setYExtrema(GUIConst.usualMin, GUIConst.USUALMAX);
		rawSignalPlotP8.setSize(GUIConst.screenW, GUIConst.screenH/2-5);
		rawSignalPlotP8.setLocation(0, GUIConst.screenH/2+5);
		int[][] plotsGrid = setPlotsGrid();
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_EEG_CAPS ; sensor++){
			fftplot[sensor] = new FFTPlot(EmoConst.TRIAL_FFT_SIZE, EmoConst.SAMPLE_RATE);
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
		int x = 0, y = 10;
		int[][] grid = new int[n+1][2];
		for (int i = 0 ; i < n ; i++){
			if (x > GUIConst.screenW - width) {
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
	 * TODO saveDataset, loadDataset and recordSensorData should be adapted to the way
	 * a classifier gets instances (ie vectors).
	 */
	
	/**
	 * Saves the sensor data as well as each specific sensor frequency data.
	 */
	private void saveDataset(){
		try {
			long filename_ext = (new Date()).getTime();
			File file = new File(pathToDatasets + "emocopter_"+user+"_sensor_"+filename_ext+".data");
			if(!file.getParentFile().exists()) file.getParentFile().mkdir();
			FileHandler.exportDataset(sensorData, file);
			for (int sensor=0 ; sensor<EmoConst.NUMBER_OF_EEG_CAPS ; sensor++){
				String sensorName = EmoConst.SENSOR_NAMES[sensor];
				FileHandler.exportDataset(freqData[sensor], new File(pathToDatasets + "emocopter_"+user+"_freq_"+sensorName+"_"+filename_ext+".data"));
			}
		} catch (IOException e) {
			System.out.println("Couldn't create file, check for permissions.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Load an existing dataset from the 'datasets' folder.
	 */
	private void loadDataset(){
		FileFilter filter = new FileNameExtensionFilter("Dataset files", "data", "DATA");
		sensorData = openDataset(filter, pathToDatasets);
	}
	
	/**
	 * Load an existing dataset and build classifier with it.
	 */
	private void loadTrainingFile(){
		FileFilter filter = new FileNameExtensionFilter("Training Data File", "tdf", "TDF");
		learningData = openDataset(filter, pathToTrainingData);
		classifier.buildClassifier(learningData);
		classifierIsLiterate = true;
	}
	
	// TODO this thing doesn't work, it freezes everything
	/**
	 * Generic method for opening datasets.
	 * @param filter
	 * @param preferredPath
	 * @return
	 */
	private Dataset openDataset(FileFilter filter, String preferredPath){
		Dataset dataset = null;
		try {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Choose a file");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(filter);
			fileChooser.setCurrentDirectory(new File(preferredPath));
			int returned = fileChooser.showOpenDialog(null);
			if(returned == JFileChooser.APPROVE_OPTION){
				dataset = FileHandler.loadDataset(fileChooser.getSelectedFile());
			}
		} catch (IOException e) {
			System.out.println("No data to load, you should start from scratch.");
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return dataset;
	}
	
	/**
	 * This is used to record new data.
	 * Given a String index 'sensor', the 'data' is attributed to the right frame position.
	 * @param data to be inserted
	 * @param sensor name
	 * @throws FullFrameException 
	 */
	private void recordSensorData(int sensor, int data, double[] freqBins) throws FullFrameException{
		EmoFrame currentFrame = frameBuffer.get(frameBufferIndex);
		if(currentFrame.isFull()){
			sensorData.add(currentFrame.getInstance());
			frameBufferIndex+=1;
			frameBuffer.add(new EmoFrame(false));
			recordSensorData(sensor, data, freqBins);
		} else {
			currentFrame.put(new EEGCap(sensor, data, freqBins));
		}
	}
	
	/**
	 * The learn method has to be called a certain number of times, that is equal to the number
	 * of commands you want the classifier to learn.
	 */
	private void learn(){
		int command=0;
		while(command<literateClassifiers.length && literateClassifiers[command]) { command++; }
		if(command < EmoConst.COMMANDS.length){
			System.out.println("Learning command: " + EmoConst.COMMANDS[command] +
					" in a few seconds for " + EmoConst.FFT_SIZE_RATIO + " seconds.");
			/* We have to create a new fft buffer with a new size for learning, so we have to
			 * make a new frame also. We then save current buffers and frames in temporary objects
			 * and restore them when done. */
			FFTDataBuffer[] learningBuffers = new FFTDataBuffer[EmoConst.NUMBER_OF_EEG_CAPS];
			for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_EEG_CAPS ; sensor++){
				learningBuffers[sensor] = new FFTDataBuffer(EmoConst.LEARNING_FFT_SIZE, EmoConst.SAMPLE_RATE);
			}
			FFTDataBuffer[] tmpBuffer = fftBuffer;
			fftBuffer = learningBuffers;
			EmoFrame learningFrame = new EmoFrame(true);
			EmoFrame tmpFrame = buffer;
			buffer = learningFrame;
			
			/* WAITING TIME, this is to avoid having wrong data */
			long time = (new Date()).getTime();
			// wait for a few seconds for subject to settle down
			while((new Date()).getTime() - time < EmoConst.SETTLE_TIME * 1000){}
			time = (new Date()).getTime();
			// wait for data to be collected
			while((new Date()).getTime() - time < EmoConst.FFT_SIZE_RATIO*1000 + 500){}
			
			Instance powerInstance = flattenPowerMatrix(learningFrame, EmoConst.COMMANDS[command]);
			learningData.add(command, powerInstance);
			
			fftBuffer = tmpBuffer;
			buffer = tmpFrame;
			literateClassifiers[command] = true;
			System.out.println("Learning " + EmoConst.COMMANDS[command] + " => Done");
			
			/* All data has been collected, time to teach this classifier how to behave */
			if(command == EmoConst.COMMANDS.length - 1){
				classifier.buildClassifier(learningData);
				classifierIsLiterate = true;
				/* Training Data File */
				try {
					File trainingData = new File(pathToTrainingData + "training_" + user + "_" + (new Date()).getTime() + ".tdf");
					if(!trainingData.getParentFile().exists()) trainingData.getParentFile().mkdir();
					FileHandler.exportDataset(learningData, trainingData);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * An Instance should be a vector, so this methods allows you to extract powers
	 * from a frame and sort them into a single vector.
	 * @param frame
	 * @param className - when learning new instances, a className has to be provided
	 * 		in order to classify learning material. But when submitting online instances
	 * 		this field can be set to null so that the created instance doesn't have a 
	 * 		className.
	 * @return
	 */
	private Instance flattenPowerMatrix(EmoFrame frame, String className){
		int powerIndex = 0;
		int powerIndexBound = EmoConst.TAKE_N_BANDS;
		double powers[] = new double[EmoConst.TAKE_N_BANDS * EmoConst.NUMBER_OF_EEG_CAPS];
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_EEG_CAPS ; sensor++){
			double[] sensorPowers = frame.extractPowers(sensor);
			int i=0;
			for(; powerIndex<powerIndexBound ; powerIndex++){
				powers[powerIndex] = sensorPowers[i];
				i++;
			}
			powerIndexBound += EmoConst.TAKE_N_BANDS;
		}
		if(className == null) return new DenseInstance(powers);
		else return new DenseInstance(powers, className);
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
						if(sensor < EmoConst.NUMBER_OF_EEG_CAPS){
							buffer.setFreqs(sensor, fftBuffer[sensor].getBins());
						}
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
		 * Listening to brain waves and taking action where needed.
		 */
		if(listen && (new Date()).getTime() - timeReference >= EmoConst.TRIAL_INTERVAL * 1000){
			Instance currentInstance = flattenPowerMatrix(buffer, null);
			Object predictedCommand = classifier.classify(currentInstance);
			timeReference = (new Date()).getTime();
			System.out.println(timeReference + " -> " + predictedCommand);
			try{
				ConsciousControl.map(ardrone, new Command(predictedCommand.toString()));
			} catch (NullPointerException e){
				System.out.println("There probably is no ardrone connection.");
			}
		}

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
					"    'o': load dataset\n" +
					"    'k': load existing training data\n" +
					"    'l': learn/start listening to your brain\n" +
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
		
		if(isConnected) KeyControl.map(ardrone, new Command(key, keyCode));
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
		case 'o':	loadDataset(); break;
		case 'k':	loadTrainingFile(); break;
		case 'l':	
			if(!classifierIsLiterate) learn(); 
			else {
				timeReference = (new Date()).getTime();
				listen ^= true;
			}
			break;
		case '9':	showContactQuality ^= true; break;
		}
	}
	
	/**
	 * Makes sure the drone stops as soon as no key is pressed.
	 */
	public void keyReleased(){
		if(isConnected) KeyControl.map(ardrone, new Command(WAIT, keyCode));
	}
}
