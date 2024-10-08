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
- The libfreenect2 program is in `/path/to/libfreenect2/build/bin/`. It can be run via `./path/to/libfreenect2/build/bin/Protonect`. **Note: As of recently, you might get an error along the lines of 'error: GLSL 3.30 is not supported. Supported versions are: 1.10, 1.20, 1.30, 1.40, 1.00 ES, and 3.00 ES'. To fix this error (in a simple yet possibly incorrect way), you can set this environment variable in your terminal before running Protonect:** `export MESA_GL_VERSION_OVERRIDE=3.3`. **Not sure why, but this does the trick for now. Thanks to: [https://stackoverflow.com/a/57966892](https://stackoverflow.com/a/57966892).**
- The NiTE programs are in `/path/to/NiTE-Linux-x64-2.2/Samples/Bin`. The one which shows the tracked skeleton is called `UserViewer` and can be run via `./path/to/NiTE-Linux-x64-2.2/Samples/Bin/UserViewer`. Note this program was intended for the original Kinect v1 sensor, so it doesn't perform optimally with the Kinect v2 sensors due to the down-scaled resolution. Further investigation on an alternative skeleton tracking method is in progress.

## OpenPose

![OpenPose](openpose.png)

The real question is what new solutions are out there for skeleton tracking using the Kinectv2 sensor? The following tutorial will guide you through installing a Kinect v2 compatible solution for Carnegie Mellon University's library for skeleton tracking called [OpenPose](https://github.com/CMU-Perceptual-Computing-Lab/openpose).

### Distrobox Installation

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

After this command, you should see something like this:

```shell
evan@evanmurray-MBP:~$ distrobox enter kinectubuntu
Starting container...                            [ OK ]
Installing basic packages...                     [ OK ]
Setting up devpts mounts...                      [ OK ]
Setting up read-only mounts...                   [ OK ]
Setting up read-write mounts...                  [ OK ]
Setting up host's sockets integration...         [ OK ]
Integrating host's themes, icons, fonts...       [ OK ]
Setting up package manager exceptions...         [ OK ]
Setting up package manager hooks...              [ OK ]
Setting up dpkg exceptions...                    [ OK ]
Setting up apt hooks...                          [ OK ]
Setting up distrobox profile...                  [ OK ]
Setting up sudo...                               [ OK ]
Setting up user groups...                        [ OK ]
Setting up kerberos integration...               [ OK ]
Setting up user's group list...                  [ OK ]
Setting up user home...                          [ OK ]
Ensuring user's access...                        [ OK ]

Container Setup Complete!
evan@kinectubuntu:~$
```

Notice how now the hostname is the name of the container. You should now install this package, as it will be needed later. You can also use it to verify the version of Ubuntu.

```shell
evan@kinectubuntu:~$ sudo apt install lsb-release
evan@kinectubuntu:~$ lsb_release -a
No LSB modules are available.
Distributor ID: Ubuntu
Description:    Ubuntu 20.04.6 LTS
Release:        20.04
Codename:       focal
evan@kinectubuntu:~$
```

You'll also want to install `nano` and `git`. Nano can be used as a text editor, and git can be used for cloning some required repositories for this installation.

```shell
sudo apt install nano git
```

### OpenPose installation

First, you'll want to install the OpenCV development libraries and create a symlink for opencv2 to find opencv4.

```shell
sudo apt install libopencv-dev
sudo ln -s /usr/include/opencv4/opencv2 /usr/include/opencv2
```

You'll also want to install the CMake GUI built with Qt to build the OpenPose project.

```shell
sudo apt install cmake-qt-gui
```

Then, since the Caffe machine learning models with the default version of OpenPose have issues, you can clone this fork to install them correctly. We'll also want to initialize the submodules.

```shell
git clone https://github.com/AlecDusheck/openpose.git
cd openpose
git submodule update --init --recursive --remote
```

Now that we've cloned the repo, you can run this script to install the OpenPose dependencies:

```shell
sudo bash ./scripts/ubuntu/install_deps.sh
```

Then create the build directory, go to it, and run configure to download the Caffe models.

```shell
mkdir build
cd build
cmake-gui ..
```

A window should pop up which looks like this. Make sure the source folder is set to the root of OpenPose and the build folder is set to the build folder you just created.

![CMakeWindow](CmakeGui1.png)

Now click the Configure button and make sure the generator is set to "Unix Makefiles".

![CMakeSetup](CMakeSetup.png)

Don't worry, once you run this step--configure should fail for the first time.

![CMakeError](CMakeError.png)

This is because we haven't specified how to install CUDA yet. This will be covered in a later tutorial. For now, you can set the GPU_MODE flag to CPU_ONLY as below:

![CPUOnly](CPUOnly.png)

Now run configure again to download the Caffe models. You should see "Configuring done" at the bottom of the console when it completes.

Now navigate to the root of your OpenPose folder and go to the models folder. Copy the three files located there to a convenient place, as they will be needed later:

```
/home/evan/openpose/models/pose/body_25/pose_iter_584000.caffemodel
/home/evan/openpose/models/hand/pose_iter_102000.caffemodel
/home/evan/openpose/models/face/pose_iter_116000.caffemodel
```

Now, delete your OpenPose folder. This may seem counter-intuitive, but you'll see why. Clone the OpenPose from the official CMU repo:

```shell
git clone https://github.com/CMU-Perceptual-Computing-Lab/openpose.git
```

Now you should checkout version 1.7.0, as this is the latest supported version by another tool we will use.

```shell
cd openpose
git checkout tags/v1.7.0
```

Now, follow the same steps we followed for the previous OpenPose repo. On the second CMake configure, you will get another error.

```
CMake Error at cmake/Utils.cmake:8 (file):
  file DOWNLOAD HASH mismatch

    for file: [/home/evan/openpose/models/pose/body_25/pose_iter_584000.caffemodel]
      expected hash: [78287b57cf85fa89c03f1393d368e5b7]
        actual hash: [d41d8cd98f00b204e9800998ecf8427e]
             status: [7;"Couldn't connect to server"]

Call Stack (most recent call first):
  CMakeLists.txt:994 (download_model)
```

This is why we used the other repo to download the models. Now locate those models, and copy each one to the correct directory to replace the existing model file there (which should be 0B in size since it failed to download). Once that's done, run configure again, and it should successfully complete this time. Now click the "Generate" button to generate the build files.

Now run this make command to make OpenPose:

```shell
make -j`nproc`
```

Then run this to install the library on the system:

```shell
sudo make install
```

### ROS Installation
First, add this source to your sources file to download the ROS packages:

```shell
sudo sh -c 'echo "deb http://packages.ros.org/ros/ubuntu $(lsb_release -sc) main" > /etc/apt/sources.list.d/ros-latest.list'
```

Then add it to your keys:

```shell
curl -s https://raw.githubusercontent.com/ros/rosdistro/master/ros.asc | sudo apt-key add -
```

Now update your package index:

```shell
sudo apt update
```

Now install ROS noetic:

```shell
sudo apt install ros-noetic-desktop-full
```

Now activate the ROS environment:

```
source /opt/ros/noetic/setup.bash
```

Install and initialize ROS dependencies

```shell
sudo apt install python3-rosdep python3-rosinstall python3-rosinstall-generator python3-wstool build-essential
sudo apt install python3-rosdep
sudo rosdep init
rosdep update
```

Now create and initialize catkin workspace via the following commands:

```shell
mkdir -p ~/catkin_ws/src
cd ~/catkin_ws/
catkin_make
source devel/setup.bash
```

Copy the contents of this script and paste it in a file called `fix_cpp.py` to fix the CMake files later: [https://gist.githubusercontent.com/Meltwin/1ee35296d2bb86fee19d639580e3c91f/raw/13b8d626733981cdf58244708e5cba1ee5d87e1c/change_cpp.py](https://gist.githubusercontent.com/Meltwin/1ee35296d2bb86fee19d639580e3c91f/raw/13b8d626733981cdf58244708e5cba1ee5d87e1c/change_cpp.py)

```shell
sudo nano fix_cpp.py
```

### Kinect Bridge Installation
If you've followed the steps [above](#installation), you should already have libfreenect2 installed. However, you may want to re-install it in the container since some of the dependencies won't be installed. You can do so by removing the libfreenect2 and freenect2 folders and re-following the installation steps inside the container. You can skip step 9 since the container looks to the host for usb devices. You can also skip step 10 since it's already been done, and again the container relies on the host system for these libraries.

Go to the `src` directory and clone the kinect bridge repo there:

```shell
cd src
git clone https://github.com/code-iai/iai_kinect2.git
```

Go to the repo directory and install all the ROS dependencies.

```shell
cd iai_kinect2
rosdep install -r --from-paths .
```

Temporarily move the CMakeLists.txt file at the root of `src` directory somewhere else, and run the `fix_cpp.py` script at the root of the workspace.

```shell
cd ~/catkin_ws
python3 fix_cpp.py
```

You should see something like this:

```shell
evan@kinectubuntu:~/catkin_ws$ python3 fix_cpp.py
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                                     CMakeLists to C++17 Utils                                      ┃
┃                                           Meltwin - 2023                                           ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛

▷ Found ./src/iai_kinect2/iai_kinect2/CMakeLists.txt Nothing to change
▷ Found ./src/iai_kinect2/kinect2_calibration/CMakeLists.txt Fixed !
▷ Found ./src/iai_kinect2/kinect2_bridge/CMakeLists.txt Fixed !
▷ Found ./src/iai_kinect2/kinect2_viewer/CMakeLists.txt Fixed !
▷ Found ./src/iai_kinect2/kinect2_registration/CMakeLists.txt Fixed !
evan@kinectubuntu:~/catkin_ws$
```

Next, move the CMakeLists.txt back and build the package via the following command:

```shell
catkin_make -DCMAKE_BUILD_TYPE="Release"
```

You'll get a bunch of errors, but don't worry. We will fix them now. Do the following:

* Open up the file(s) where the error occurs
* Replace all occurences of `CV_IMWRITE_PNG_COMPRESSION` with `cv::IMWRITE_PNG_COMPRESSION`
* Replace all occurences of `CV_IMWRITE_JPEG_QUALITY` with `cv::IMWRITE_JPEG_QUALITY`
* Replace all occurences of `CV_IMWRITE_PNG_STRATEGY` with `cv::IMWRITE_PNG_STRATEGY`
* Replace all occurences of `CV_IMWRITE_PNG_STRATEGY_RLE` with `cv::IMWRITE_PNG_STRATEGY_RLE`
* Replace all occurences of `CV_BGRA2BGR` with `cv::COLOR_BGRA2BGR`
* Replace all occurences of `CV_RGBA2BGR` with `cv::COLOR_RGBA2BGR`
* Replace all occurences of `CV_BGR2GRAY` with `cv::COLOR_BGR2GRAY`
* Replace all occurences of `CV_AA` with `cv::LINE_AA`

Now save all the files, run the command again, and it should build successfully!

### ROS OpenPose Installation
Go back to the `src` folder of the catkin workspace and clone another repo.

```shell
cd src
git clone https://github.com/ravijo/ros_openpose.git
```

Go to the repo directory and install all the ROS dependencies.

```shell
cd ros_openpose
rosdep install -r --from-paths .
```

Temporarily move the CMakeLists.txt file at the root of `src` directory somewhere else, and run the `fix_cpp.py` script at the root of the workspace.

```shell
cd ~/catkin_ws
python3 fix_cpp.py
```

You should see something like this:

```shell
evan@kinectubuntu:~/catkin_ws$ python3 fix_cpp.py
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                                     CMakeLists to C++17 Utils                                      ┃
┃                                           Meltwin - 2023                                           ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛

▷ Found ./src/iai_kinect2/iai_kinect2/CMakeLists.txt Nothing to change
▷ Found ./src/iai_kinect2/kinect2_calibration/CMakeLists.txt Nothing to change
▷ Found ./src/iai_kinect2/kinect2_bridge/CMakeLists.txt Nothing to change
▷ Found ./src/iai_kinect2/kinect2_viewer/CMakeLists.txt Nothing to change
▷ Found ./src/iai_kinect2/kinect2_registration/CMakeLists.txt Nothing to change
▷ Found ./src/ros_openpose/CMakeLists.txt Fixed !
evan@kinectubuntu:~/catkin_ws$
```

Now move the CMakeLists.txt file back and run the make command at the root of the workspace to build the package.

```shell
catkin_make
```

Next, go to the scripts folder of the ros openpose and make all the files executable.

```shell
roscd ros_openpose/scripts
chmod +x *.py
```

Now, install this python package to allow the visualizer to correctly work.

```shell
sudo apt install python-is-python3
```

Make sure the kinects are connected to the computer and run this command to launch the visualizer:

```shell
roslaunch ros_openpose run.launch camera:=kinect
```

There's some strange threading behavior with this example where it closes the process immediately, and you may have to spam to the command a couple of times for the visualizer to actually stay open.

### Uninstalling OpenPose
Since we installed everything in the Distrobox container, this is fairly simple.

Step 1 is to remove all of the folders you git cloned for the project.

```shell
cd ~
sudo rm -r catkin_ws
sudo rm -r openpose
sudo rm -r libfreenect2
sudo rm -r freenect2
```

Then, exit the container via the `logout` command. It should look something like this.

```shell
evan@kinectubuntu:~$ logout
evan@evanmurray-MBP:~$
```

Notice how now the host has changed back to our actual hostname. You can now stop the container and remove it.

```shell
distrobox stop kinectubuntu
distrobox rm kinectubuntu
```

### Installing with GPU
If you try to run OpenPose with the CPU only, you may notice it's very slow. One way the computation can be sped up is by using a GPU. The animation below was achieved realtime using an Nvidia GeForce GTX 1080 graphics card.

![GPUOpenPose](GPUOpenPose.gif)

To do this, first follow your GPU manufacturer's instructions on installing the GPU drivers. Make sure to install these outside of your distrobox on your regular system. An Nvidia GPU is recommended since Ubuntu makes it easy to install the drivers, and Nvidia devices support [CUDA](https://developer.nvidia.com/cuda-toolkit) by default (which we will need). Run the following command to automatically install the best GPU drivers for your system:

```shell
sudo ubuntu-drivers autoinstall
```

Once the drivers are installed, double check your graphics card is showing up through its drivers. There are a couple ways to do this with an Nvidia card:

```shell
lab@terra:~$ nvidia-smi
Tue Oct  1 01:09:13 2024
+-----------------------------------------------------------------------------------------+
| NVIDIA-SMI 550.107.02             Driver Version: 550.107.02     CUDA Version: 12.4     |
|-----------------------------------------+------------------------+----------------------+
| GPU  Name                 Persistence-M | Bus-Id          Disp.A | Volatile Uncorr. ECC |
| Fan  Temp   Perf          Pwr:Usage/Cap |           Memory-Usage | GPU-Util  Compute M. |
|                                         |                        |               MIG M. |
|=========================================+========================+======================|
|   0  NVIDIA GeForce GTX 1080        Off |   00000000:01:00.0 Off |                  N/A |
| 27%   31C    P8              6W /  180W |       7MiB /   8192MiB |      0%      Default |
|                                         |                        |                  N/A |
+-----------------------------------------+------------------------+----------------------+

+-----------------------------------------------------------------------------------------+
| Processes:                                                                              |
|  GPU   GI   CI        PID   Type   Process name                              GPU Memory |
|        ID   ID                                                               Usage      |
|=========================================================================================|
|    0   N/A  N/A      2541      G   /usr/lib/xorg/Xorg                              4MiB |
+-----------------------------------------------------------------------------------------+
lab@terra:~$
```

And:

```shell
lab@terra:~$ cat /proc/driver/nvidia/version
NVRM version: NVIDIA UNIX x86_64 Kernel Module  550.107.02  Wed Jul 24 23:53:00 UTC 2024
GCC version:
lab@terra:~$
```

You might need to reboot your system after installing the drivers for these changes to take effect.

Then, let's create a copy of the distrobox we setup earlier. However, this time we will pass the `--nvidia` flag to enable the environment to use the GPU drivers which we just installed on our host (regular) system.

```shell
distrobox create --name kinectubuntugpu --clone kinectubuntu --nvidia
```

Now, if you run `nvidia-smi` in the environment as well, you should see the same output as on your host.

Next, download the recommended [CUDA 11.7](https://developer.nvidia.com/cuda-11-7-1-download-archive) toolkit and follow the instructions listed on the website to install. Make sure you download the Run installer and specify you want to download Cuda 11.7! Otherwise a different version of Cuda will be installed!

When you run the CUDA installer, make sure to uncheck installing the drivers. Doing this will cause the installer to warn you that the drivers were not installed, and that is OK! Don't try to install any more drivers, as we already did that. You will likely get errors if you try to install another driver. The only thing you need to pay attention to is when the installer tells you to add some paths to your environment variables so CUDA can be recognized by the terminal. To add a path to an already existing path variable, you can use this chain-like syntax:

```shell
export PATH_I_WANT_TO_ADD_TO=$PATH_I_WANT_TO_ADD_TO:/path/i/want/to/add
```

This effectively will add `/path/i/want/to/add` to the list of paths contained in `$PATH_I_WANT_TO_ADD_TO`.

Whenever you restart your environment or terminal, you may need to run these commands again. If you don't want to do that, look into [adding them to your bashrc](https://stackoverflow.com/questions/14637979/how-to-permanently-set-path-on-linux-unix).

Next, you should install cuDNN which is a neural net extension to CUDA: [https://developer.nvidia.com/cudnn](https://developer.nvidia.com/cudnn). The install process is pretty similar to that of the CUDA install. Follow the instructions on the Nvidia website.

Now that both CUDA and cuDNN are installed, go back to your OpenPose build folder and run the `sudo bash ./scripts/ubuntu/install_deps.sh` command again. Additionally, open the CMake GUI again. This time before you configure, you should set `GPU_MODE` to `CUDA`.

![GPUMODECUDA](GPUMODECUDA.png)

Then, go through the whole build and install process as we did above.

Now, we're going to build libfreenect2 again but with GPU support. Before we do this, you'll want to clone the CUDA samples repo in the home directory of your environment (you'll see why)

```shell
git clone https://github.com/NVIDIA/cuda-samples.git
cd cuda-samples
git checkout tags/v11.6
```

The last command will checkout the CUDA version below ours (11.7). You want to try and get as close as possible to the version number without going above the version of CUDA you have installed.

Now, copy this folder to your CUDA folder which should be located in `/usr/local`

```shell
sudo cp -r cuda-samples /usr/local/cuda-11.7
```

Now go to your root libfreenect2:

```shell
cd ~/libfreenect2
```

Open the CMakeLists.txt file with your favorite text editor and change the line `"${CUDA_TOOLKIT_ROOT_DIR}/samples/common/inc"` to `"${CUDA_TOOLKIT_ROOT_DIR}/cuda-samples/Common"`. This will allow CMake to find the include files for these which are very important. Ever since CUDA 11.6, the samples are not shipped with the toolkit which is why we need to install them separately. Otherwise you will get some error about missing a math_helper.h file and a bunch of other files.

Now you run all the same commands we did to configure and build libfreenect2. If it complains about a freenect2 folder already existing, make sure to delete that one from the last install and retry.

After you complete the new install, you'll want to set another environment variable which tells libfreenect2 which graphics rendering pipeline to use.

```shell
export LIBFREENECT2_PIPELINE=cuda
```

You'll also want to re-build all of the ROS packages we installed above.

### Uninstalling GPU Drivers

```shell
sudo apt-get --purge remove "*cuda*" "*cublas*" "*cufft*" "*cufile*" "*curand*"  "*cusolver*" "*cusparse*" "*gds-tools*" "*npp*" "*nvjpeg*" "nsight*" "*nvvm*"
sudo apt-get remove --purge "*nvidia-driver*" "libxnvctrl*"
sudo apt autoremove --purge
sudo apt autoclean
sudo apt-get purge $(dpkg -l | awk '$2~/nvidia/ {print $2}')
```
