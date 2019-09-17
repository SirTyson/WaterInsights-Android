package insights.water.waterinsightsv005;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static insights.water.waterinsightsv005.DataCollectionActivity.NUM_RESULTS;

public class TakePictureActivity extends AppCompatActivity {

    public static final String NITRATE_KEY = "Nitrate";
    public static final String NITRITE_KEY = "Nitrite";
    public static final String HARDNESS_KEY = "TotalHardness";
    public static final String TOTAL_CHLORINE_KEY = "TotalChlorine";
    public static final String ALKALINITY_KEY = "Alkalinity";
    public static final String PH_KEY = "PHKey";
    public static final String IMAGE_FILENAME = "WaterInsights_IMAGE_CAPTURE.jpeg";
    public static final int NUM_SAMPLES = 6;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_STORAGE = 102;
    private Button takePic;
    private Button uploadPic;
    private ConstraintLayout progressBar;
    private FrameLayout dim;
    private ImageView imageView;

    private String path;
    private Bitmap img;
    private float[] results;
    /*
     *  Results order:
     *  0: Nitrate
     *  1: Nitrite
     *  2: Total Hardness
     *  3: Total Chlorine
     *  4: Total Alkalinity
     *  5: PH
     */

    public static void populateBundleResults(@NonNull Intent intent, @NonNull float[] results) {
        intent.putExtra(NITRATE_KEY, results[0]);
        intent.putExtra(NITRITE_KEY, results[1]);
        intent.putExtra(HARDNESS_KEY, results[2]);
        intent.putExtra(TOTAL_CHLORINE_KEY, results[3]);
        intent.putExtra(ALKALINITY_KEY, results[4]);
        intent.putExtra(PH_KEY, results[5]);
    }

    @Nullable
    public static float[] getBundleResults(@Nullable Bundle extras) {
        float results[] = new float[NUM_RESULTS];
        if (extras == null) {
            return null;
        }

        results[0] = extras.getFloat(NITRATE_KEY, -1.0f);
        results[1] = extras.getFloat(NITRITE_KEY, -1.0f);
        results[2] = extras.getFloat(HARDNESS_KEY, -1.0f);
        results[3] = extras.getFloat(TOTAL_CHLORINE_KEY, -1.0f);
        results[4] = extras.getFloat(ALKALINITY_KEY, -1.0f);
        results[5] = extras.getFloat(PH_KEY, -1.0f);
        return results;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);

        takePic = findViewById(R.id.take_picture_button);
        uploadPic = findViewById(R.id.upload_gallery_button);
        progressBar = findViewById(R.id.image_progress_bar_layout);
        dim = findViewById(R.id.background_dim_frame);
        imageView = findViewById(R.id.current_image_view);


        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureButtonClicked();
            }
        });

        uploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPictureButtonClicked();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    private void launchPictureCapture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void takePictureButtonClicked() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                return;
            }
        }

        launchPictureCapture();
    }

    private void uploadPictureButtonClicked() {
        Intent getPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(getPhoto, REQUEST_IMAGE_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            launchPictureCapture();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    img = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(img);
                    new TakePictureActivity.AsyncImageProcess().execute();
                }
                break;

            case REQUEST_IMAGE_STORAGE:
                if (resultCode == RESULT_OK) {
                    progressBar.setVisibility(View.VISIBLE);
                    dim.setVisibility(View.VISIBLE);
                    Uri imageURI = data.getData();
                    try {
                        img = (Bitmap) MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                        imageView.setImageBitmap(img);
                        new TakePictureActivity.AsyncImageProcess().execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private String saveToInternalStorage(@NonNull Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        /* path to /data/data/yourapp/app_data/imageDir */
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        /* Create imageDir */
        File path = new File(directory + "/" + IMAGE_FILENAME);
        if (path.exists()) {
            Log.d("plz", "File: " + path.getAbsolutePath());
            if (!path.delete()) {
                Log.d("plz", "File failed to delete");
            } else {
                Log.d("plz", "DELETED OLD FILE");
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path, false);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return directory.getAbsolutePath() + "/" + IMAGE_FILENAME;
    }

    private void displayErrorPopup() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_retake_image, null);

        // create the popup window
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 325, getResources().getDisplayMetrics());
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, (int) width, height, true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dim.setVisibility(View.GONE);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }

        dim.setVisibility(View.VISIBLE);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dim.setVisibility(View.GONE);
                takePic.setClickable(true);
            }
        });
    }

    private boolean isValid(@NonNull float[] value) {
        if (value.length != NUM_SAMPLES) {
            return false;
        }

        for (float val : value) {
            if (val < 0) {
                return false;
            }
        }

        return true;
    }

    private void validPicture() {
        Intent intent = new Intent(this, ShowResultsActivity.class);
        populateBundleResults(intent, results);
        startActivity(intent);
        finish();
    }

    private class AsyncImageProcess extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            dim.setVisibility(View.VISIBLE);
            takePic.setClickable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            path = saveToInternalStorage(img);
            results = CvUtil.processImage(path, CvUtil.getStep1Code());
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            progressBar.setVisibility(View.GONE);
            dim.setVisibility(View.GONE);
            takePic.setClickable(true);
            if (isValid(results)) {
                validPicture();
            } else {
                displayErrorPopup();
            }
        }
    }
}
