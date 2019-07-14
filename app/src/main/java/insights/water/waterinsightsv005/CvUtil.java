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

    public static native int processImage(long matAddr);
}
