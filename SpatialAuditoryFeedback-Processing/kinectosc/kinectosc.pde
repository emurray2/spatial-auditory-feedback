//
// Copyright (C) 2024 Evan Murray
//
// kinectosc.pde
// SpatialAuditoryFeedback
// Created by Evan Murray on 4/18/24.
//
// This file is part of SpatialAuditoryFeedback.
//
// SpatialAuditoryFeedback is an application designed to map descriptors of joint position to parameters in ambisonics renderers.
//
// SpatialAuditoryFeedback is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SpatialAuditoryFeedback is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SpatialAuditoryFeedback. If not, see <https://www.gnu.org/licenses/>.
//


import SimpleOpenNI.*;
import oscP5.*;
import netP5.*;

SimpleOpenNI kinect;
OscP5 oscP5;
OscP5 oscP5two;
NetAddress myRemoteLocation;
NetAddress myRemoteLocation2;

void setup() {
  kinect = new SimpleOpenNI(this);
  kinect.enableDepth();
  kinect.enableHand();
  kinect.enableUser();// this changed
  size(640, 480);
  fill(255, 0, 0);
  oscP5 = new OscP5(this, 57120);
  oscP5two = new OscP5(this, 6666);
  myRemoteLocation = new NetAddress("192.168.0.85", 57120);
  myRemoteLocation2 = new NetAddress("192.168.0.85", 6666);
}

void draw() {
  kinect.update();
  image(kinect.depthImage(), 0, 0);

  IntVector userList = new IntVector();
  kinect.getUsers(userList);

  if (userList.size() > 0) {
    int userId = userList.get(0);

    if ( kinect.isTrackingSkeleton(userId)) {
      drawSkeleton(userId);
    }
  }
}

void drawSkeleton(int userId) {
  stroke(0);
  strokeWeight(5);

  kinect.drawLimb(userId, SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_LEFT_SHOULDER);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_LEFT_ELBOW);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_ELBOW, SimpleOpenNI.SKEL_LEFT_HAND);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_RIGHT_SHOULDER);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_RIGHT_ELBOW);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_ELBOW, SimpleOpenNI.SKEL_RIGHT_HAND);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_TORSO);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_TORSO);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_LEFT_HIP);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_HIP, SimpleOpenNI.SKEL_LEFT_KNEE);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_KNEE, SimpleOpenNI.SKEL_LEFT_FOOT);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_RIGHT_HIP);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_RIGHT_KNEE);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_KNEE, SimpleOpenNI.SKEL_RIGHT_FOOT);
  kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_LEFT_HIP);

  noStroke();

  fill(255, 0, 0);
  drawJoint(userId, SimpleOpenNI.SKEL_HEAD);
  drawJoint(userId, SimpleOpenNI.SKEL_NECK);
  drawJoint(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER);
  drawJoint(userId, SimpleOpenNI.SKEL_LEFT_ELBOW);
  drawJoint(userId, SimpleOpenNI.SKEL_NECK);
  drawJoint(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER);
  drawJoint(userId, SimpleOpenNI.SKEL_RIGHT_ELBOW);
  drawJoint(userId, SimpleOpenNI.SKEL_TORSO);
  drawJoint(userId, SimpleOpenNI.SKEL_LEFT_HIP);
  drawJoint(userId, SimpleOpenNI.SKEL_LEFT_KNEE);
  drawJoint(userId, SimpleOpenNI.SKEL_RIGHT_HIP);
  drawJoint(userId, SimpleOpenNI.SKEL_LEFT_FOOT);
  drawJoint(userId, SimpleOpenNI.SKEL_RIGHT_KNEE);
  drawJoint(userId, SimpleOpenNI.SKEL_LEFT_HIP);
  drawJoint(userId, SimpleOpenNI.SKEL_RIGHT_FOOT);
  drawJoint(userId, SimpleOpenNI.SKEL_RIGHT_HAND);
  drawJoint(userId, SimpleOpenNI.SKEL_LEFT_HAND);
}

void drawJoint(int userId, int jointID) {
  if (jointID == SimpleOpenNI.SKEL_LEFT_ELBOW) {
    PMatrix3D jointOrientation = new PMatrix3D();
    OscMessage myMessage1 = new OscMessage("/source/azim");
    OscMessage myMessage2 = new OscMessage("/source/elev");
    OscMessage myMessage3 = new OscMessage("/kinect/psi");
    float rotationConfidence = kinect.getJointOrientationSkeleton(userId, jointID, jointOrientation);
    float phi = atan2(jointOrientation.m20, jointOrientation.m21);
    float theta = acos(jointOrientation.m22);
    float psi = -1.0 * atan2(jointOrientation.m02, jointOrientation.m12);
    if (rotationConfidence < 0.5) {
      return;
    }
    myMessage1.add(0);
    myMessage1.add(phi);
    myMessage2.add(0);
    myMessage2.add(theta);
    myMessage3.add(psi);
    oscP5.send(myMessage1, myRemoteLocation);
    oscP5.send(myMessage2, myRemoteLocation);
    oscP5.send(myMessage3, myRemoteLocation);
    oscP5two.send(myMessage1, myRemoteLocation2);
    oscP5two.send(myMessage2, myRemoteLocation2);
    oscP5two.send(myMessage3, myRemoteLocation2);
  }
  PVector joint = new PVector();

  float confidence = kinect.getJointPositionSkeleton(userId, jointID,
    joint);
  if (confidence < 0.5) {
    return;
  }
  PVector convertedJoint = new PVector();
  kinect.convertRealWorldToProjective(joint, convertedJoint);
  ellipse(convertedJoint.x, convertedJoint.y, 5, 5);
}

//Calibration not required

void onNewUser(SimpleOpenNI kinect, int userID) {
  println("Start skeleton tracking");
  kinect.startTrackingSkeleton(userID);
}
