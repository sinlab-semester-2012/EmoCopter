package main;


import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import oscP5.*;
import processing.core.*;
import sensors.EEGCap;
import sensors.Sensor;

import com.shigeodayo.ardrone.processing.ARDroneForP5;

import constants.*;

import eegUtils.gui.FFTPlot;
import eegUtils.gui.RawSignalPlot;
import eegUtils.util.FFTDataBuffer;
import exceptions.TooManySensorsException;
import frame.*;

public class OscARDrone extends PApplet{
	private static final long serialVersionUID = 1L;
	OscP5 oscP5;
	private String savedData = "emocopter_vince.data";
	private ARDroneForP5 ardrone;
	private boolean isConnected = false;
	private boolean baseLevelsDone = false;
	private boolean showBattery = false;
	private boolean showContactQuality = false;
	private boolean showHelp = false;		//TODO
	private boolean showGyro = false;
	private boolean putData = false;		//TODO
	private boolean loadData = false;		//TODO
	private boolean saveData = false;		//TODO
	private int frameWidth;
	private int frameHeight;
	private float[] baseLevels;
	private ArrayList<EmoFrame> epocData;
	private FFTDataBuffer[] fftBuffer;
	private Sensor[] buffer;
	//Plots the fft of a *single* chosen cap sensor.
	private int selectedSensor = 4;
	private FFTPlot fftplot = new FFTPlot(EmoConst.WINDOW_SIZE, EmoConst.SAMPLE_RATE);
	private RawSignalPlot rawSignalPlot = new RawSignalPlot(EmoConst.WINDOW_SIZE);

	public void setup() {
		initialize();
		frameWidth = displayHeight*4/3;
		frameHeight = displayHeight*3/4;
		size(frameWidth, frameHeight, OPENGL);
		frameRate(25);
		oscP5 = new OscP5(this, 7000);
		noStroke();
		//ardrone=new ARDroneForP5("192.168.1.1");
		//isConnected = ardrone.connect();
		if(isConnected){
			ardrone.connectNav();
			ardrone.connectVideo();
			ardrone.start();
		} else {
			System.out.println("ARDrone not connected, please connect it and restart the app.");
		}
	}
	
	/**
	 * Initialize buffers and arrays.
	 */
	private void initialize(){
		baseLevels = new float[EmoConst.NUMBER_OF_SENSORS];
		epocData = new ArrayList<EmoFrame>();
		if(loadData) loadData();
		fftBuffer = new FFTDataBuffer[EmoConst.NUMBER_OF_EEG_CAPS];
		buffer = new Sensor[EmoConst.NUMBER_OF_SENSORS];
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
			if(sensor < EmoConst.NUMBER_OF_EEG_CAPS){
				fftBuffer[sensor] = new FFTDataBuffer(EmoConst.WINDOW_SIZE, EmoConst.SAMPLE_RATE);
				buffer[sensor] = new EEGCap(EmoConst.SENSOR_NAMES[sensor]);
			} else buffer[sensor] = new Sensor(EmoConst.SENSOR_NAMES[sensor]);
			baseLevels[sensor] = 0;
		}
	}

	/**
	 * Load saved data from a previous session.
	 */
	private void loadData(){
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
	}

	/**
	 * This is used to place new data inside the epocData array.
	 * Given an index 'sensor', the 'data' is attributed to the
	 * correct frame position.
	 * 
	 * @param data
	 * @param sensor
	 */
	private void putData(String sensor, int data){
		epocData.get(epocData.size()-1).put(new Sensor(sensor, data));
	}

	/**
	 * Save data for this session.
	 */
	private void saveData(){
		String[] dataTmp = new String[epocData.size()];
		for(int frame=0 ; frame<epocData.size() ; frame++){
			dataTmp[frame] = new String();
			for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
				dataTmp[frame] = dataTmp[frame] + epocData.get(frame).getData(sensor) + " ";
			}
		}
		saveStrings(savedData, dataTmp);
	}

	/**
	 * Creates an array of averages to allow the centering of values around zero.
	 */
	private void doBaseLevels(){
		float[][] tmp = new float[EmoConst.MEAN_PRECISION][EmoConst.NUMBER_OF_SENSORS];
		for(int precision=0 ; precision<EmoConst.MEAN_PRECISION ; precision++){
			Date time0 = new Date();
			for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
				tmp[precision][sensor] = buffer[sensor].value();
			}
			if(precision<EmoConst.MEAN_PRECISION-1){
				while(new Date().getTime()-time0.getTime() < 1000/EmoConst.PRECISION_RATE){}
			}
		}
		for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_SENSORS ; sensor++){
			for(int precision=0 ; precision<EmoConst.MEAN_PRECISION ; precision++){
				baseLevels[sensor] += tmp[precision][sensor];
			}
			baseLevels[sensor] = Math.round(baseLevels[sensor] / EmoConst.MEAN_PRECISION);
		}
	}

	/**
	 * Inherited oscEvent method, reacts to OSC messages.
	 * @param msg
	 */
	void oscEvent(OscMessage msg){
		if(msg.checkAddrPattern("/emokit/channels")){
			for(int sensor=0 ; sensor<EmoConst.NUMBER_OF_EEG_CAPS ; sensor++){
				buffer[sensor].updateValue(msg.get(sensor).intValue()-((int)baseLevels[sensor]*3/4));
				fftBuffer[sensor].add(msg.get(sensor).intValue());
				fftBuffer[sensor].applyFFT();
				if(sensor == selectedSensor) fftplot.add(msg.get(sensor).intValue()-((int)baseLevels[sensor]*3/4));
				if(sensor == selectedSensor) rawSignalPlot.add(msg.get(sensor).intValue()-((int)baseLevels[sensor]*3/4));
				if(putData) putData(buffer[sensor].name(), buffer[sensor].value());
			}
		} else if(msg.checkAddrPattern("/emokit/gyro")){
			buffer[EmoConst.gyroX_index].updateValue(msg.get(0).intValue());
			buffer[EmoConst.gyroY_index].updateValue(msg.get(1).intValue());
		} else if(msg.checkAddrPattern("/emokit/info")){
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

		//Draw AR.Drone info
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

		// Draw EPOC data
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
			stroke(GUIConst.WHITE);
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
			stroke(GUIConst.WHITE);
			fill(GUIConst.WHITE);
			rect(GUIConst.batteryX+1, GUIConst.batteryY+1, (buffer[EmoConst.battery_index].value()*(GUIConst.batterySize-2)/100), (GUIConst.stdThickness-4)/2);
			noFill();
			rect(GUIConst.batteryX, GUIConst.batteryY, GUIConst.batterySize, GUIConst.stdThickness/2);
		}
		if(!baseLevelsDone){
			String calibration = "PRESS ENTER WHEN YOU ARE READY FOR CALIBRATION\n" +
					"PLEASE TRY TO REMAIN CALM EVEN IF YOU ARE VERY HAPPY TO DO THIS ;)\n" +
					"\n" +
					"PLEASE WAIT, THIS IS ONLY GOING TO TAKE APPROX. " + EmoConst.MEAN_PRECISION/EmoConst.PRECISION_RATE + " SECONDS";
			text(calibration, frameWidth/2-60, frameHeight/2);
		}
	}

	/**
	 * Overrides stop method. This is compulsory in the case where saving data is needed.
	 */
	public void stop(){
		if(saveData) saveData();
		super.stop();
	}

	/**
	 * Keyboard command interpreter.
	 */
	public void keyPressed() {
		switch (key){
		case CODED:
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
			if(!baseLevelsDone) {
				doBaseLevels();
				baseLevelsDone = true;
			}
			break;
		case 'a':	//show all
			boolean show = !(showBattery && showGyro && showHelp && showContactQuality);
			showBattery = show;
			showGyro = show;
			showHelp = show;
			showContactQuality = show;
			break;
		case 'b':	showBattery ^= true; break;
		case 'g':	showGyro ^= true; break;
		case 'h':	showHelp ^= true; break;
		case 'q':	showContactQuality ^= true; break;
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
