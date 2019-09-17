#include <insights_water_waterinsightsv005_CvUtil.h>
#include <VisionFunctions.h>
#include <VisionConstants.h>
#include <android/log.h>

JNIEXPORT jfloatArray JNICALL Java_insights_water_waterinsightsv005_CvUtil_loadImage
  (JNIEnv * env, jclass cls, jstring filepath)
{
    const char *nativeString = env->GetStringUTFChars(filepath, nullptr);

    std::string file(nativeString);

    std::vector<float> ret = processImageFromFile(file);
    env->ReleaseStringUTFChars(filepath, nativeString);

    int size = ret.size();

    jfloatArray result;
    result = env->NewFloatArray(size);
    if (result == NULL) {
        return NULL; // When le crap hits the fan
    }

    int i;
    jfloat fill[size];

    for (i = 0; i < size; i++) {
        fill[i] = ret[i];
    }

    env->SetFloatArrayRegion(result, 0, size, fill);
    return result;
}