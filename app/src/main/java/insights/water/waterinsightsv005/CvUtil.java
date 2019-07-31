package insights.water.waterinsightsv005;

import android.util.Log;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;

public class CvUtil {
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("plz", "Unable to load OpenCV");
        } else {
            Log.d("plz", "LIBRARY LOADED");
            System.loadLibrary("CvExample");
        }
    }

    public static float[] processImage(String filepath, int OP_CODE)
    {
        return loadImage(filepath, OP_CODE);
    }

    public static int STEP_1_OP_CODE()
    {
        return getStep1Code();
    }

    public static int STEP_2_OP_CODE()
    {
        return getStep2Code();
    }

    public static int STEP_3_OP_CODE()
    {
        return getStep3Code();
    }

    public static int STEP_4_OP_CODE()
    {
        return getStep4Code();
    }

    public static native float[] loadImage(String filepath, int OP_CODE);
    public static native int getStep1Code();
    public static native int getStep2Code();
    public static native int getStep3Code();
    public static native int getStep4Code();
}
