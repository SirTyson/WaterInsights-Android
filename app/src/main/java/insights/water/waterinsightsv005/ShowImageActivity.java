package insights.water.waterinsightsv005;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static insights.water.waterinsightsv005.CollectDataActivity.IMAGE_FILENAME;
import static insights.water.waterinsightsv005.SubmitActivity.OP_CODE_DRAW_REF;
import static insights.water.waterinsightsv005.SubmitActivity.OP_CODE_DRAW_SAMPLE;
import static insights.water.waterinsightsv005.SubmitActivity.OP_CODE_DRAW_TARGET;
import static insights.water.waterinsightsv005.SubmitActivity.OP_CODE_FIND_PPM;
import static insights.water.waterinsightsv005.SubmitActivity.OP_CODE_KEY;

public class ShowImageActivity extends AppCompatActivity {

    String imgPath;
    ImageView debugImage;
    TextView ppmTextView;

    private static final String TAG = "OpenCVDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        debugImage = findViewById(R.id.debug_image_viewer);
        ppmTextView = findViewById(R.id.ppm_text_view);

        Intent i = getIntent();
        int opCode = i.getIntExtra(OP_CODE_KEY, -1);
        imgPath = i.getStringExtra("IMAGE");

        drawMat(opCode);
    }

    private void drawMat(int opCode) {
        /* Create mat from bitmap */
        Bitmap bmp = loadImageFromStorage(imgPath);
        if (bmp == null) {
            Log.d(TAG, "ERROR: Loading Image returned null");
            return;
        }
        Mat mat = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
        float ratio = (float) mat.height() / (float) mat.width();
        Log.d("plz", "RATIO " + ratio);
        Imgproc.resize(mat, mat, new Size(600 / ratio, 600));
        Log.d("plz", "Width: " + mat.width() + " Height: " + mat.height());
        /* Do operation */
        switch (opCode) {
            case OP_CODE_DRAW_TARGET:
                CvUtil.DEBUG_DRAW_TARGET(mat);
                break;
            case OP_CODE_DRAW_REF:
                CvUtil.DEBUG_DRAW_REFERENCE(mat);
                break;
            case OP_CODE_DRAW_SAMPLE:
                CvUtil.DEBUG_DRAW_SAMPLE(mat);
                break;
            case OP_CODE_FIND_PPM:
                ppmTextView.setText("PPM: " + CvUtil.processImage(mat));
                break;
            default:
                Log.e(TAG, "INVALID OP_CODE: " + opCode);
                return;
        }

        /* Convert mat of changed size to bitmap */
        try {
            bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bmp);
        }
        catch (CvException e) {
            Log.d(TAG, e.getMessage());
        }
        debugImage.setImageBitmap(bmp);
    }

    private Bitmap loadImageFromStorage(String path) {
        try {
            File f = new File(path, IMAGE_FILENAME);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
