# Kinect
## Introduction
The Kinect for Xbox 360 (also known as the Kinect v1) was developed by [PrimeSense](https://github.com/PrimeSense) in collaboration with [OpenNI](https://github.com/OpenNI). A couple years later, Microsoft began development on the Kinect for Xbox One (also known as the Kinect v2). Additionally, PrimeSense who made the original Kinect was aquired by Apple.

While the Kinect v2 was designed for PC's and the Xbox One, many open-source developers continued the development of the Kinect v2 drivers which still obtain data aquisition via the OpenNI2 drivers. [Libfreenect2](https://github.com/OpenKinect/libfreenect2), a wrapper library, was written for obtaining the RGB and depth data which depends on the OpenNI2 drivers. This library is what the submodule in the [`Kinect`](https://github.gatech.edu/L42i/spatial-auditory-feedback/tree/main/Kinect) folder points to, and it contains all of the software one needs to control/view data from the Kinect v2 sensor. It does not contain the software which performs pose tracking.

However, because the data retrieved by the OpenNI2 drivers is similar to that of the OpenNI drivers written for the Kinect v1, it is possible to use the old tracking algorithms created by PrimeSense known as NiTE. A future goal of the Spatial Auditory Feedback project is to use open-source pose tracking algorithms, such as [OpenPose](https://github.com/CMU-Perceptual-Computing-Lab/openpose), [MediaPipe](https://github.com/google-ai-edge/mediapipe), or [Skeltrack](https://github.com/joaquimrocha/Skeltrack) which was designed specifically for the Kinect v2.

Further exploration and testing is needed here. For now, the installation guide on the next page will cover installing libfreenect2 and the NiTE2.2 shared library.

## Installation
Step 1: Update the package list
```shell
sudo apt update
```
Step 2: Install build tools:
```shell
sudo apt install build-essential cmake pkg-config
```
Step 3: Install libusb (for getting the Kinect to talk to the USB ports)
```shell
sudo apt install libusb-1.0-0-dev
```
Step 4: Install graphics libraries (for displaying the camera output from the sensor)
```shell
sudo apt install libturbojpeg0-dev libglfw3-dev
```
Step 5: Install OpenNI2 libraries/tools (they will be needed later for testing and installation of libfreenect2)
```shell
sudo apt install libopenni2-dev openni2-utils
```
Step 6: Clone libfreenect2 and enter the directory
```shell
git clone https://github.com/OpenKinect/libfreenect2.git
cd libfreenect2
```
Step 7: Build and install the libfreenect2 library from source
```shell
mkdir build && cd build
cmake .. -DCMAKE_INSTALL_PREFIX=$HOME/freenect2
make
make install
```
**Note: You need to specify `cmake -Dfreenect2_DIR=$HOME/freenect2/lib/cmake/freenect2` for CMake based third-party application to find libfreenect2.**

Step 8: Install the openni2 library from here
```shell
sudo make install-openni2
```
Step 9: Set up udev rules for device access.
```shell
sudo cp ../platform/linux/udev/90-kinect2.rules /etc/udev/rules.d/
```
**Note: If any Kinects were plugged into the USB ports, each one needs to be re-plugged again for the USB to recognize it with the updated rules.**

Step 10: Create symbolic links to installed libraries which are missing and required for everything to work.
```shell
sudo ln -s /usr/lib/x86_64-linux-gnu/OpenNI2/Drivers/libfreenect2-openni2.so /usr/lib/x86_64-linux-gnu/OpenNI2/Drivers/libFreenectDriver.so
sudo ln -s /usr/lib/x86_64-linux-gnu/OpenNI2/Drivers/libfreenect2-openni2.so.0 /usr/lib/x86_64-linux-gnu/OpenNI2/Drivers/libFreenectDriver.so.0
```
**Note: Remember these paths, and make sure they exist. They will be needed when we run the NiTE tracker in the next steps below. See: [https://github.com/OpenKinect/libfreenect2/issues/639](https://github.com/OpenKinect/libfreenect2/issues/639) for more context.**

## Programs
### Prelude
There are a couple items to complete before having fun with the Kinect sensors. Some of these you may need to do every time you boot the system. Others may be a one-time checklist item.

- Run `lsusb` and make sure each Kinect is attached to a bus which is on a USB 3.0 root controller. USB 3.0 is required for the Kinect Xbox One (Kinect v2)
- Download and unzip the NiTE tracking library by PrimeSense archived here: [https://bitbucket.org/kaorun55/openni-2.2/src/2f54272802bfd24ca32f03327fbabaf85ac4a5c4/NITE%202.2%20%CE%B1/?at=master](https://bitbucket.org/kaorun55/openni-2.2/src/2f54272802bfd24ca32f03327fbabaf85ac4a5c4/NITE%202.2%20%CE%B1/?at=master). Rich 133B has a 64-bit Linux system, so it should be the x64-Linux one.
- Remember the last note about those paths? This is where they are needed. Open the file located in the downloaded NiTE package as described below:
```shell
cd ~/Downloads/NiTE-Linux-x64-2.2/Samples/Bin
sudo nano OpenNI.ini
```

- Uncomment the line ';Repository=OpenNI2/Drivers' by removing the ';' and set repository to the path. It should look something like this:
`Repository=/usr/lib/x86_64-linux-gnu/OpenNI2/Drivers`. Save the file and exit.

- Set the UBFS buffer size to 32MB (this one is required on every boot). Otherwise libusb will not have enough memory, as the default on system boot is 16MB. Each Kinect v2 sensor uses 16MB which adds up to 32MB total. Read this guide for more info on how to calculate the memory: [Understanding USBFS on Linux](https://www.flir.com/support-center/iis/machine-vision/application-note/understanding-usbfs-on-linux/?srsltid=AfmBOood-y0AC2UhSXvvTEc693SOaK5ANXoG_BEoVPKJCJtMeKu25VND).
```shell
sudo sh -c 'echo 32 > /sys/module/usbcore/parameters/usbfs_memory_mb'
```

### Fun Stuff
Now any of the OpenNI2, libfreenect2, and NiTE programs should be able to run with the Kinects! The OpenNI2 programs should be in your path.

- The OpenNI2 programs are listed in [https://github.com/OpenNI/OpenNI2/tree/master/Samples](https://github.com/OpenNI/OpenNI2/tree/master/Samples)
- The libfreenect2 program is in `/path/to/libfreenect2/build/bin/`. It can be run via `./path/to/libfreenect2/build/bin/Protonect`.
- The NiTE programs are in `/path/to/NiTE-Linux-x64-2.2/Samples/Bin`. The one which shows the tracked skeleton is called `UserViewer` and can be run via `./path/to/NiTE-Linux-x64-2.2/Samples/Bin/UserViewer`. Note this program was intended for the original Kinect v1 sensor, so it doesn't perform optimally with the Kinect v2 sensors due to the down-scaled resolution. Further investigation on an alternative skeleton tracking method is in progress.

## OpenPose
The real question is what new solutions are out there for skeleton tracking using the Kinectv2 sensor? The following tutorial will guide you through installing a Kinect v2 compatible solution for Carnegie Mellon University's library for skeleton tracking called [OpenPose](https://github.com/CMU-Perceptual-Computing-Lab/openpose).

Many of the packages used for the Kinect are not supported on later distro versions anymore, so it would be nice to setup some sort of container which can hold and run our work. This is the perfect job for Docker, a package which allows the containerization of a Ubuntu image -- or pretty much any Linux distro for that matter. Docker has a nice wrapper which makes the job even easier called [Distrobox](https://wiki.archlinux.org/title/Distrobox). It can be installed with aptitude via the following command:

```shell
sudo apt install distrobox
```

A container can be created with a name and an image in a registry, such as the docker.io registry. Example url: docker.io/barebuild/ubuntu:14.04. In this installation, we will use the official Ubuntu 20.04 distro which is the latest compatible distro for this installation. Then we can give the container a name we will remember for our use case, such as kinectubuntu.

```shell
distrobox create --name kinectubuntu --image ubuntu:20.04
```

Then you can enter the container you created using:

```shell
distrobox enter kinectubuntu
```


