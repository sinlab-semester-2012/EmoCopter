EmoCopter
=========

A semester project proposed by sinlab at EPFL.

This project makes use of the following open source libraries and pieces of code:
- Emokit by Cody Brocious, Kyle Machulis, Severin Lemaignan and other contributors
- oscpack by Ross Bencina


Description
===========

The goal of this project is to be able to control a quadcopter with your mind. 
This is done using the Emotiv Epoc EEG headset to get the brainwave data and 
then controling the ARDrone QuadCopter through an assembled set of existing 
code and a few ajustments.


Required Libraries
==================

To compile the project you will need cmake version 2.6 or higher.
Of course the requirements for emokit and oscpack are also regarding this project. 
So libusb-1.0 and libmcrypt


Where things are now
====================

2012.10.08
----------
At the moment the project is far from being finished, I am still having troubles 
finding a way to send OSC messages (noob I am but noob I shall not remain...).
I am currently working with Gael Grosch on the emotiv part. He is helping a lot 
with the signal processing (actually he's doing it all :).
We are experimenting with the openVibe library, trying to figure out what signal 
corresponds to what state of mind.
The idea is to separate waves in terms of frequency, as this is the characteristic 
that differenciates one state from another. They are all related to certain brain 
activities like being relaxed, thinking of a simple concept or even moving your limbs.
Having done that we will be able to assign states to commands and send them to whichever 
device we like (the ARDrone in my case).
