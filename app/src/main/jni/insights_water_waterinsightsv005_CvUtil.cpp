#include <insights_water_waterinsightsv005_CvUtil.h>
#include <VisionFunctions.h>

JNIEXPORT jint JNICALL Java_insights_water_waterinsightsv005_CvUtil_loadImage
  (JNIEnv * env, jclass cls, jstring filepath)
{
    const char *nativeString = env->GetStringUTFChars(filepath, nullptr);

    std::string file(nativeString);

    int ret = processImageFromFile(file);

    env->ReleaseStringUTFChars(filepath, nativeString);
    return ret;
}