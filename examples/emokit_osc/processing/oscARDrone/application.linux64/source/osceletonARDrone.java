import processing.core.*; 
import processing.data.*; 
import processing.opengl.*; 

import processing.opengl.*; 
import oscP5.*; 
import netP5.*; 
import com.shigeodayo.ardrone.manager.*; 
import com.shigeodayo.ardrone.navdata.*; 
import com.shigeodayo.ardrone.utils.*; 
import com.shigeodayo.ardrone.processing.*; 
import com.shigeodayo.ardrone.command.*; 
import com.shigeodayo.ardrone.*; 

import com.shigeodayo.ardrone.manager.*; 
import com.shigeodayo.ardrone.navdata.*; 
import com.shigeodayo.ardrone.utils.*; 
import com.shigeodayo.ardrone.processing.*; 
import com.shigeodayo.ardrone.command.*; 
import com.shigeodayo.ardrone.*; 
import com.shigeodayo.ardrone.video.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class osceletonARDrone extends PApplet {






//import AR DRONE stuff







OscP5 oscP5;

// ARDRONE OBJ
ARDroneForP5 ardrone;
int takeoff=0;

/*int ballSize = 10;
Hashtable<Integer, SkeletonRed> skels = new Hashtable<Integer, SkeletonRed>();*/

public void setup() {
  size(displayHeight*4/3, displayHeight, OPENGL); //Keep 4/3 aspect ratio, since it matches the kinect's.
  frameRate(25);
  oscP5 = new OscP5(this, 7000);
  /*hint(ENABLE_OPENGL_4X_SMOOTH);*/
  noStroke();

  println("Obtaining ARDrone connection");
  //ar drone init (in principle possible to control 2 or more with more Wireless net card)
  /*ardrone=new ARDroneForP5("192.168.1.1");
  ardrone.connect();
  ardrone.connectNav();
  ardrone.connectVideo();
  ardrone.start();*/
}

/*
void oscEvent(OscMessage msg){
  print("### received an osc message.");
  print(" addrpattern: "+msg.addrPattern());
  println(" typetag: "+msg.typetag()); 
}*/

/************FOR EXAMPLE PURPOSE ONLY************/
/* incoming osc message are forwarded to the oscEvent method. */
// Here you can easily see the format of the OSC messages sent. For each user, the joints are named with 
// the joint named followed by user ID (head0, neck0 .... r_foot0; head1, neck1.....)
/*void oscEvent(OscMessage msg) {
  // msg.print();

  if (msg.checkAddrPattern("/joint") && msg.checkTypetag("sifff")) {
    // We have received joint coordinates, let's find out which skeleton/joint and save the values ;)
    Integer id = msg.get(1).intValue();
    SkeletonRed s = skels.get(id);   
    if (s == null) {
      s = new SkeletonRed(id);
      skels.put(id, s);
    }
    if (msg.get(0).stringValue().equals("head")) {
      s.headCoords[0] = msg.get(2).floatValue();
      s.headCoords[1] = msg.get(3).floatValue();
      s.headCoords[2] = msg.get(4).floatValue();
    }
    else if (msg.get(0).stringValue().equals("r_shoulder")) {
      s.rs[0] = msg.get(2).floatValue();
      s.rs[1] = msg.get(3).floatValue();
      s.rs[2] = msg.get(4).floatValue();
    }
    else if (msg.get(0).stringValue().equals("r_hand")) {
      s.rh[0] = msg.get(2).floatValue();
      s.rh[1] = msg.get(3).floatValue();
      s.rh[2] = msg.get(4).floatValue();
    }
    else if (msg.get(0).stringValue().equals("l_shoulder")) {
      s.ls[0] = msg.get(2).floatValue();
      s.ls[1] = msg.get(3).floatValue();
      s.ls[2] = msg.get(4).floatValue();
    }
    else if (msg.get(0).stringValue().equals("l_hand")) {
      s.lh[0] = msg.get(2).floatValue();
      s.lh[1] = msg.get(3).floatValue();
      s.lh[2] = msg.get(4).floatValue();
    }
    /*    else if (msg.get(0).stringValue().equals("neck")) {
     s.neckCoords[0] = msg.get(2).floatValue();
     s.neckCoords[1] = msg.get(3).floatValue();
     s.neckCoords[2] = msg.get(4).floatValue();
     }
     else if (msg.get(0).stringValue().equals("r_collar")) {
     s.rCollarCoords[0] = msg.get(2).floatValue();
     s.rCollarCoords[1] = msg.get(3).floatValue();
     s.rCollarCoords[2] = msg.get(4).floatValue();
     }
     else if (msg.get(0).stringValue().equals("r_elbow")) {
     s.rElbowCoords[0] = msg.get(2).floatValue();
     s.rElbowCoords[1] = msg.get(3).floatValue();
     s.rElbowCoords[2] = msg.get(4).floatValue();
     //            msg.print();
     }
     else if (msg.get(0).stringValue().equals("r_wrist")) {
     s.rWristCoords[0] = msg.get(2).floatValue();
     s.rWristCoords[1] = msg.get(3).floatValue();
     s.rWristCoords[2] = msg.get(4).floatValue();
     msg.print();
     }
     else if (msg.get(0).stringValue().equals("r_finger")) {
     s.rFingerCoords[0] = msg.get(2).floatValue();
     s.rFingerCoords[1] = msg.get(3).floatValue();
     s.rFingerCoords[2] = msg.get(4).floatValue();
     //            msg.print();
     }
     else if (msg.get(0).stringValue().equals("r_collar")) {
     s.lCollarCoords[0] = msg.get(2).floatValue();
     s.lCollarCoords[1] = msg.get(3).floatValue();
     s.lCollarCoords[2] = msg.get(4).floatValue();
     //            msg.print();
     }  
     else if (msg.get(0).stringValue().equals("l_elbow")) {
     s.lElbowCoords[0] = msg.get(2).floatValue();
     s.lElbowCoords[1] = msg.get(3).floatValue();
     s.lElbowCoords[2] = msg.get(4).floatValue();
     }
     else if (msg.get(0).stringValue().equals("l_wrist")) {
     s.lWristCoords[0] = msg.get(2).floatValue();
     s.lWristCoords[1] = msg.get(3).floatValue();
     s.lWristCoords[2] = msg.get(4).floatValue();
     }
     else if (msg.get(0).stringValue().equals("l_finger")) {
     s.lFingerCoords[0] = msg.get(2).floatValue();
     s.lFingerCoords[1] = msg.get(3).floatValue();
     s.lFingerCoords[2] = msg.get(4).floatValue();
     }
     else if (msg.get(0).stringValue().equals("torso")) {
     s.torsoCoords[0] = msg.get(2).floatValue();
     s.torsoCoords[1] = msg.get(3).floatValue();
     s.torsoCoords[2] = msg.get(4).floatValue();
     }
     else if (msg.get(0).stringValue().equals("r_hip")) {
     s.rHipCoords[0] = msg.get(2).floatValue();
     s.rHipCoords[1] = msg.get(3).floatValue();
     s.rHipCoords[2] = msg.get(4).floatValue();
     } 
     else if (msg.get(0).stringValue().equals("r_knee")) {
     s.rKneeCoords[0] = msg.get(2).floatValue();
     s.rKneeCoords[1] = msg.get(3).floatValue();
     s.rKneeCoords[2] = msg.get(4).floatValue();
     } 
     else if (msg.get(0).stringValue().equals("r_ankle")) {
     s.rAnkleCoords[0] = msg.get(2).floatValue();
     s.rAnkleCoords[1] = msg.get(3).floatValue();
     s.rAnkleCoords[2] = msg.get(4).floatValue();
     } 
     else if (msg.get(0).stringValue().equals("r_foot")) {
     s.rFootCoords[0] = msg.get(2).floatValue();
     s.rFootCoords[1] = msg.get(3).floatValue();
     s.rFootCoords[2] = msg.get(4).floatValue();
     } 
     else if (msg.get(0).stringValue().equals("l_hip")) {
     s.lHipCoords[0] = msg.get(2).floatValue();
     s.lHipCoords[1] = msg.get(3).floatValue();
     s.lHipCoords[2] = msg.get(4).floatValue();
     } 
     else if (msg.get(0).stringValue().equals("l_knee")) {
     s.lKneeCoords[0] = msg.get(2).floatValue();
     s.lKneeCoords[1] = msg.get(3).floatValue();
     s.lKneeCoords[2] = msg.get(4).floatValue();
     } 
     else if (msg.get(0).stringValue().equals("l_ankle")) {
     s.lAnkleCoords[0] = msg.get(2).floatValue();
     s.lAnkleCoords[1] = msg.get(3).floatValue();
     s.lAnkleCoords[2] = msg.get(4).floatValue();
     } 
     else if (msg.get(0).stringValue().equals("l_foot")) {
     s.lFootCoords[0] = msg.get(2).floatValue();
     s.lFootCoords[1] = msg.get(3).floatValue();
     s.lFootCoords[2] = msg.get(4).floatValue();
     } 
     
  }

  else if (msg.checkAddrPattern("/new_user") && msg.checkTypetag("i")) {
    // A new user is in front of the kinect... Tell him to do the calibration pose!
    println("New user with ID = " + msg.get(0).intValue());
  }
  else if (msg.checkAddrPattern("/new_skel") && msg.checkTypetag("i")) {
    //New skeleton calibrated! Lets create it!
    Integer id = msg.get(0).intValue();
    SkeletonRed s = new SkeletonRed(id);
    skels.put(id, s);
  }
  else if (msg.checkAddrPattern("/lost_user") && msg.checkTypetag("i")) {
    //Lost user/skeleton
    Integer id = msg.get(0).intValue();
    println("Lost user " + id);
    skels.remove(id);
    safeDrone();
  }
}
*/
/************************************************/

public void safeDrone() {
  ardrone.stop();
  ardrone.landing();
  takeoff=0;
}

public void draw()
{
  background(0);  
  ambientLight(64, 64, 64);
  lightSpecular(255, 255, 255);
  directionalLight(224, 224, 224, .5f, 1, -1);

/************FOR EXAMPLE PURPOSE ONLY************/
  /*for (SkeletonRed s: skels.values()) {
    fill(s.colors[0], s.colors[1], s.colors[2]);
    for (float[] j: s.allCoords) {
      pushMatrix();
      translate(j[0]*width, j[1]*height, -j[2]*300);
      sphere(2 * ballSize/j[2]);
      popMatrix();
    }
  }*/
/************************************************/

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
  
  
/************FOR EXAMPLE PURPOSE ONLY************/
/*
  // set the thresholds for movements 
  float ythreshold = 0.2;
  float zthreshold = 0.4;
  float rotate_threshold = 0.5;
  float side_threshold = 0.1;
  float arms_out_threshold = 0.3;
  int maxspeed = 10; //drone max speed

  for (SkeletonRed s:   skels.values()) {
    //  for (float[] j: s.allCoords) {
    float dx, dy, dz, dr, dh, dside;
    dx = (s.rs[0]+s.ls[0]) - (s.rh[0]+s.lh[0]);
    dy = (s.rs[1]+s.ls[1]) - (s.rh[1]+s.lh[1]);
    dz = (s.rs[2]+s.ls[2]) - (s.rh[2]+s.lh[2]) - 0.1;
    dh = abs(s.rh[0]-s.lh[0]) + abs(s.rh[1]-s.lh[1]) + abs(s.rh[2]-s.lh[2]);
    dr = s.rh[2]-s.lh[2];
    dside = s.rh[1]-s.lh[1];
    if (takeoff==1) {
       // rotations according to rotations of the arms 
      if (abs(dr) > rotate_threshold && dh > arms_out_threshold) {
        int speed=round(30*maxspeed*abs(dz)/rotate_threshold);
        if (dr<0) {
          ardrone.spinRight(speed);
        }
        else {
          ardrone.spinLeft(speed);
        }
      }    
      //left right according to tilt
      else if (abs(dside) > side_threshold && dh > arms_out_threshold) {
        int speed=round(maxspeed*abs(dside)/side_threshold);
        if (dside>0) {
          ardrone.goLeft(speed);
        }
        else {
          ardrone.goRight(speed);
        }
      }
      //fwd backward according to arms 
      else if (abs(dz) > zthreshold && dh > arms_out_threshold) {
        int speed=round(maxspeed*abs(dz)/zthreshold);
        if (dz>0) {
          ardrone.forward(speed);
        }
        else {
          ardrone.backward(speed);
        }
      }
          //up and down according to arms up / down 
      else if (abs(dy) > ythreshold && dh > arms_out_threshold) {
        int speed=round(5*maxspeed*abs(dy)/ythreshold);
        if (dy>0) {
          ardrone.up(speed);
        }
        else if (dy< (-2.5*ythreshold)) {
          ardrone.landing();
          takeoff=0;
        }
        else {          
          ardrone.down(speed);
        }
      }
 
      else ardrone.stop();
    }

    else {
      //print ("thresh =2  dy="+dy+"\n");
      if (dy> 3*ythreshold && dh > arms_out_threshold) {
        ardrone.takeOff();
        takeoff=1;
      }
    }
  }*/
/************************************************/
}


// KEYBOARD COMMANDS
public void keyPressed() {
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

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "osceletonARDrone" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
