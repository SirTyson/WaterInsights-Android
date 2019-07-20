package insights.water.waterinsightsv005;

import android.util.Log;

import org.opencv.android.OpenCVLoader;

public class CvUtil {
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("plz", "Unable to load OpenCV");
        } else {
            Log.d("plz", "LIBRARY LOADED");
            System.loadLibrary("CvExample");
        }
    }

    public static int processImage(String filepath)
    {
        return loadImage(filepath);
    }
    public static native int loadImage(String filepath);
}
