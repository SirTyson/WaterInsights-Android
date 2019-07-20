package insights.water.waterinsightsv005;

import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class CvUtil {
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("plz", "Unable to load OpenCV");
        } else {
            System.loadLibrary("CvExample");
        }
    }

    public static int processImage(Mat mat)
    {
        return processImage(mat.getNativeObjAddr());
    }

    public static void DEBUG_DRAW_REFERENCE(Mat mat)
    {
        DEBUG_DRAW_REFERENCE(mat.getNativeObjAddr());
    }

    public static void DEBUG_DRAW_SAMPLE(Mat mat)
    {
        DEBUG_DRAW_SAMPLE(mat.getNativeObjAddr());
    }

    public static void DEBUG_DRAW_TARGET(Mat mat)
    {
        DEBUG_DRAW_TARGET(mat.getNativeObjAddr());
    }

    public static int TEST_LOAD_IMAGE(String filepath)
    {
        return loadImage(filepath);
    }

    public static native int loadImage(String filepath);
    public static native int processImage(long matAddr);
    public static native void DEBUG_DRAW_REFERENCE(long matAddr);
    public static native void DEBUG_DRAW_SAMPLE(long matAddr);
    public static native void DEBUG_DRAW_TARGET(long matAddr);
}
