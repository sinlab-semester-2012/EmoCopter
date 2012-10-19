/**
 *  This example is yet to be improved. It does not handle exceptions and may prove faulty.
 */

import processing.opengl.*;

import oscP5.*;
import netP5.*;

//import AR DRONE stuff
import com.shigeodayo.ardrone.*;

OscP5 oscP5;

// ARDRONE OBJ
ARDroneForP5 ardrone;
int takeoff=0;

void setup() {
  size(displayHeight*4/3, displayHeight, OPENGL);
  frameRate(25);
  oscP5 = new OscP5(this, 7000);
  //hint(ENABLE_OPENGL_4X_SMOOTH);
  noStroke();

  //ar drone init (in principle possible to control 2 or more with more Wireless net card)
  /*ardrone=new ARDroneForP5("192.168.1.1");
  ardrone.connect();
  ardrone.connectNav();
  ardrone.connectVideo();
  ardrone.start();*/
}

void oscEvent(OscMessage msg){
  if(msg.checkAddrPattern("/emokit/channels")){
    println("1) Channel readings:");
    for(int i=0 ; i<14 ; i++){
      println("[" + i + "] : " + msg.get(i).intValue());
    }
    println();
  } else if(msg.checkAddrPattern("/emokit/gyro")){
    println("2) Gyro:");
    println("x: " + msg.get(0).intValue() + " ; y: " + msg.get(1).intValue());
    println();
  }
}

void safeDrone() {
  ardrone.stop();
  ardrone.landing();
  takeoff=0;
}

void draw()
{
  background(0);  
  ambientLight(64, 64, 64);
  lightSpecular(255, 255, 255);
  directionalLight(224, 224, 224, .5, 1, -1);

 /* //AR.Drone show image
  PImage img=ardrone.getVideoImage(false);
  if (img==null)
    return;
  image(img, 0, 0);
  // ardrone.printARDroneInfo();
  float pitch=ardrone.getPitch();
  float roll=ardrone.getRoll();
  float yaw=ardrone.getYaw();
  float altitude=ardrone.getAltitude();
  float[] velocity=ardrone.getVelocity();
  int battery=ardrone.getBatteryPercentage();
  String attitude="pitch:"+pitch+"\nroll:"+roll+"\nyaw:"+yaw+"\naltitude:"+altitude;
  text(attitude, 20, 85);
  String vel="vx:"+velocity[0]+"\nvy:"+velocity[1];
  text(vel, 20, 140);
  String bat="battery:"+battery+" %";
  text(bat, 20, 170);
  */
}


// KEYBOARD COMMANDS
void keyPressed() {
  if (key == CODED) {
    if (keyCode == UP) {
      ardrone.forward(50);
    }
    else if (keyCode == DOWN) {
      ardrone.backward(50);
    }
    else if (keyCode==LEFT) {
      ardrone.goLeft(100);
    }
    else if (keyCode==RIGHT) {
      ardrone.goRight(100);
    }
    else if (keyCode==SHIFT) {
      ardrone.takeOff();
      takeoff=1;
    }
    else if (keyCode==CONTROL) {
      ardrone.landing();
      takeoff=0;
    }
  }
  else {
    if (key=='s') {
      ardrone.stop();
    }
    else if (key=='r') {
      ardrone.spinRight(10);
    }
    else if (key=='l') {
      ardrone.spinLeft(10);
    }
    else if (key=='u') {
      ardrone.up(10);
    }
    else if (key=='d') {
      ardrone.down(10);
    }
    else if (key=='1') {
      ardrone.setHorizontalCamera();
    }
    else if (key=='2') {
      ardrone.setHorizontalCameraWithVertical();
    }
    else if (key=='3') {
      ardrone.setVerticalCamera();
    }
    else if (key=='4') {
      ardrone.setVerticalCameraWithHorizontal();
    }
    else if (key=='5') {
      ardrone.toggleCamera();
    }
  }
}

