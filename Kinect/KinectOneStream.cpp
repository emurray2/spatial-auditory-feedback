/*
-- Georgia Tech 2016 Spring
--
-- This is a sample code to show how to use the libfreenet2 with OpenCV

-- The code will streams RGB, IR and Depth images from an Kinect sensor.
-- To use multiple Kinect sensor, simply initial other "listener" and "frames"

-- This code refered from sample code provided from libfreenet2: Protonect.cpp
-- https://github.com/OpenKinect/libfreenect2
-- and another discussion from: http://answers.opencv.org/question/76468/opencvkinect-onekinect-for-windows-v2linuxlibfreenect2/

-- Contact: Chih-Yao Ma at <cyma@gatech.edu>
-- Edited to work with OpenPose by Evan Murray <emurray49@gatech.edu> - 10/2/2024
-- Original Repo: https://github.com/chihyaoma/KinectOneStream.git
-- OpenPose Tutorial: https://github.com/CMU-Perceptual-Computing-Lab/openpose/blob/master/examples/tutorial_api_cpp/18_synchronous_custom_all_and_datum.cpp
*/

#include <opencv2/opencv.hpp>
#include <libfreenect2/frame_listener_impl.h>
#include <libfreenect2/registration.h>
#include <openpose/flags.hpp>
#include <openpose/headers.hpp>

// Collect frames read from the Kinect
class WUserInput : public op::WorkerProducer<std::shared_ptr<std::vector<std::shared_ptr<op::Datum>>>>
{
public:
    WUserInput() : m_listener{libfreenect2::Frame::Color}
    {
        m_pipeline = new libfreenect2::CudaKdePacketPipeline(0);
        m_dev = m_freenect2.openDevice(m_freenect2.getDefaultDeviceSerialNumber(), m_pipeline);
    }
    ~WUserInput()
    {
        m_dev->stop();
        m_dev->close();
        delete m_dev;
        delete m_pipeline;
    }

    void initializationOnThread()
    {
        m_dev->start();
        m_dev->setColorFrameListener(&m_listener);
        std::cout << "device serial: " << m_dev->getSerialNumber() << std::endl;
        std::cout << "device firmware: " << m_dev->getFirmwareVersion() << std::endl;
    }

    std::shared_ptr<std::vector<std::shared_ptr<op::Datum>>> workProducer()
    {
        try
        {
            cv::Mat rgbmat;
            // Create new datum
            auto datumsPtr = std::make_shared<std::vector<std::shared_ptr<op::Datum>>>();
            datumsPtr->emplace_back();
            auto& datumPtr = datumsPtr->at(0);
            datumPtr = std::make_shared<op::Datum>();

            // Fill datum
            m_listener.waitForNewFrame(m_frames);
            libfreenect2::Frame *rgb = m_frames[libfreenect2::Frame::Color];
            cv::Mat(rgb->height, rgb->width, CV_8UC4, rgb->data).copyTo(rgbmat);
            cv::cvtColor(rgbmat, rgbmat, CV_RGB2BGR);
            const cv::Mat cvInputData = rgbmat;
            datumPtr->cvInputData = OP_CV2OPCONSTMAT(cvInputData);

            // If empty frame -> return nullptr
            if (datumPtr->cvInputData.empty())
            {
                op::opLog("Empty frame detected. Closing program.", op::Priority::High);
                this->stop();
                datumsPtr = nullptr;
            }
            m_listener.release(m_frames);
            return datumsPtr;
        }
        catch (const std::exception& e)
        {
            this->stop();
            op::error(e.what(), __LINE__, __FUNCTION__, __FILE__);
            return nullptr;
        }
    }

private:
    libfreenect2::Freenect2 m_freenect2;
    libfreenect2::Freenect2Device* m_dev;
    libfreenect2::PacketPipeline* m_pipeline;
    libfreenect2::SyncMultiFrameListener m_listener;
    libfreenect2::FrameMap m_frames;
};

// This worker will just read and return all the captured frames by the kinect
class WUserOutput : public op::WorkerConsumer<std::shared_ptr<std::vector<std::shared_ptr<op::Datum>>>>
{
public:
    void initializationOnThread() {}

    void workConsumer(const std::shared_ptr<std::vector<std::shared_ptr<op::Datum>>>& datumsPtr)
    {
        try
        {
            // User's displaying/saving/other processing here
            // datumPtr->cvOutputData: rendered frame with pose or heatmaps
            // datumPtr->poseKeypoints: Array<float> with the estimated pose
            if (datumsPtr != nullptr && !datumsPtr->empty())
            {
                // Get the mid-hip for person 0 from the BODY_25 model.
                // See: https://cmu-perceptual-computing-lab.github.io/openpose/web/html/doc/md_doc_02_output.html#pose-output-format-body_25
                const auto& poseKeypoints = datumsPtr->at(0)->poseKeypoints;
                if (poseKeypoints.getSize(0) > 0)
                {
                    const auto baseIndex = poseKeypoints.getSize(2)*(8);
                    const auto x = poseKeypoints[baseIndex];
                    const auto y = poseKeypoints[baseIndex + 1];
                    const auto score = poseKeypoints[baseIndex + 2];
                    op::opLog("X: " + std::to_string(x));
                    op::opLog("Y: " + std::to_string(y));
                    op::opLog("Score: " + std::to_string(score));
                }
            }
        }
        catch (const std::exception& e)
        {
            this->stop();
            op::error(e.what(), __LINE__, __FUNCTION__, __FILE__);
        }
    }
};

void configureWrapper(op::WrapperT<op::Datum>& opWrapperT)
{
    try
    {
        // Configuring OpenPose

        // logging_level
        op::checkBool(
            0 <= FLAGS_logging_level && FLAGS_logging_level <= 255, "Wrong logging_level value.",
            __LINE__, __FUNCTION__, __FILE__);
        op::ConfigureLog::setPriorityThreshold((op::Priority)FLAGS_logging_level);

        // Hardcoded stuff (no flags)
        // Set scale zero to one
        const auto keypointScaleMode = op::ScaleMode::ZeroToOne;
        // Set maximum one person
        int numPeopleMax = 1;

        // Applying user defined configuration - GFlags to program variables
        // outputSize
        const auto outputSize = op::flagsToPoint(op::String(FLAGS_output_resolution), "-1x-1");
        // netInputSize
        const auto netInputSize = op::flagsToPoint(op::String(FLAGS_net_resolution), "-1x368");
        // poseMode
        const auto poseMode = op::flagsToPoseMode(FLAGS_body);
        // poseModel
        const auto poseModel = op::flagsToPoseModel(op::String(FLAGS_model_pose));
        // JSON saving
        if (!FLAGS_write_keypoint.empty())
            op::opLog(
                "Flag `write_keypoint` is deprecated and will eventually be removed. Please, use `write_json`"
                " instead.", op::Priority::Max);

        // heatmaps to add
        const auto heatMapTypes = op::flagsToHeatMaps(FLAGS_heatmaps_add_parts, FLAGS_heatmaps_add_bkg,
                                                      FLAGS_heatmaps_add_PAFs);
        const auto heatMapScaleMode = op::flagsToHeatMapScaleMode(FLAGS_heatmaps_scale);
        // >1 camera view?
        const auto multipleView = (FLAGS_3d || FLAGS_3d_views > 1);
        // Enabling Google Logging
        const bool enableGoogleLogging = true;

        // Initializing the user custom classes
        // Frames producer (e.g., video, webcam, ...)
        auto wUserInput = std::make_shared<WUserInput>();
        // GUI (Display)
        auto wUserOutput = std::make_shared<WUserOutput>();

        // Add custom input
        const auto workerInputOnNewThread = false;
        opWrapperT.setWorker(op::WorkerType::Input, wUserInput, workerInputOnNewThread);
        // Add custom output
        const auto workerOutputOnNewThread = true;
        opWrapperT.setWorker(op::WorkerType::Output, wUserOutput, workerOutputOnNewThread);

        // Pose configuration (use WrapperStructPose{} for default and recommended configuration)
        const op::WrapperStructPose wrapperStructPose{
          poseMode, netInputSize, outputSize, keypointScaleMode, FLAGS_num_gpu, FLAGS_num_gpu_start,
          FLAGS_scale_number, (float)FLAGS_scale_gap, op::flagsToRenderMode(FLAGS_render_pose, multipleView),
          poseModel, !FLAGS_disable_blending, (float)FLAGS_alpha_pose, (float)FLAGS_alpha_heatmap,
          FLAGS_part_to_show, op::String("/home/lab/openpose/models"), heatMapTypes, heatMapScaleMode, FLAGS_part_candidates,
          (float)FLAGS_render_threshold, FLAGS_number_people_max, FLAGS_maximize_positives, FLAGS_fps_max,
          op::String(FLAGS_prototxt_path), op::String(FLAGS_caffemodel_path),
          (float)FLAGS_upsampling_ratio, enableGoogleLogging};
        opWrapperT.configure(wrapperStructPose);
        opWrapperT.configure(wrapperStructPose);
        // Face configuration (use op::WrapperStructFace{} to disable it)
        const op::WrapperStructFace wrapperStructFace{};
        opWrapperT.configure(wrapperStructFace);
        // Hand configuration (use op::WrapperStructHand{} to disable it)
        const op::WrapperStructHand wrapperStructHand{};
        opWrapperT.configure(wrapperStructHand);
        // Extra functionality configuration (use op::WrapperStructExtra{} to disable it)
        const op::WrapperStructExtra wrapperStructExtra{};
        opWrapperT.configure(wrapperStructExtra);
        // Output (comment or use default argument to disable any output)
        const op::WrapperStructOutput wrapperStructOutput{};
        opWrapperT.configure(wrapperStructOutput);
    }
    catch (const std::exception& e)
    {
        op::error(e.what(), __LINE__, __FUNCTION__, __FILE__);
    }
}

int main()
{
    try
    {
        op::opLog("Starting OpenPose demo...", op::Priority::High);

        // OpenPose wrapper
        op::opLog("Configuring OpenPose...", op::Priority::High);
        op::WrapperT<op::Datum> opWrapperT;
        configureWrapper(opWrapperT);

        // Start, run, and stop processing - exec() blocks this thread until OpenPose wrapper has finished
        op::opLog("Starting thread(s)...", op::Priority::High);
        opWrapperT.exec();
    }
    catch (const std::exception&)
    {
        return -1;
    }

    return 0;
}
