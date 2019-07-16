#include <insights_water_waterinsightsv005_CvUtil.h>
#include <VisionFunctions.h>

JNIEXPORT jint JNICALL Java_insights_water_waterinsightsv005_CvUtil_processImage
  (JNIEnv * env, jclass cls, jlong image_addr)
{
    cv::Mat* image = (cv::Mat*) image_addr;
    return processImage(*image);
}

JNIEXPORT void JNICALL Java_insights_water_waterinsightsv005_CvUtil_DEBUG_1DRAW_1REFERENCE
  (JNIEnv * env, jclass cls, jlong image_addr)
{
    cv::Mat* image = (cv::Mat*) image_addr;
    DEBUG_DRAW_REFERENCE(*image);
}

JNIEXPORT void JNICALL Java_insights_water_waterinsightsv005_CvUtil_DEBUG_1DRAW_1SAMPLE
  (JNIEnv * env, jclass cls, jlong image_addr)
{
    cv::Mat* image = (cv::Mat*) image_addr;
    DEBUG_DRAW_SAMPLE(*image);
}

JNIEXPORT void JNICALL Java_insights_water_waterinsightsv005_CvUtil_DEBUG_1DRAW_1TARGET
  (JNIEnv * env, jclass cls, jlong image_addr)
{
    cv::Mat* image = (cv::Mat*) image_addr;
    DEBUG_DRAW_TARGET(*image);
}