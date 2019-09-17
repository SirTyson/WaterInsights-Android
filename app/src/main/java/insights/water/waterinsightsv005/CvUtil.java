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

    static float[] processImage(String filepath)
    {
        return loadImage(filepath);
    }

    public static native float[] loadImage(String filepath);
}
