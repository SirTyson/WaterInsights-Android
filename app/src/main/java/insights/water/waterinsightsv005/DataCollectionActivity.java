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
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataCollectionActivity extends AppCompatActivity {

    private static final int STEP_1_TIME = 30;
    private static final boolean debug = true;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_STORAGE = 102;

    public static final String IMAGE_FILENAME = "WaterInsights_IMAGE_CAPTURE.png";

    TextView timerText;
    AppCompatButton startTimerButton;
    FrameLayout dim;
    ConstraintLayout progressBar;

    public int counter = 0;
    private boolean canBeClicked = true;
    private int currStep = 1;
    private String path;
    private Bitmap img;
    private int val;

    private boolean cameraAccess = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        startTimerButton = findViewById(R.id.start_timer_button);
        dim = findViewById(R.id.background_dim_frame);
        timerText = findViewById(R.id.timer_view);
        progressBar = findViewById(R.id.image_progress_bar_layout);

        timerText.setText("0:30");

        triggerStep1Popup();

        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimerClicked();
            }
        });
    }

    private void startTimerClicked() {
        if (!canBeClicked) {
            return;
        }
        canBeClicked = false;
        if (currStep == 1) {
            setTimer(STEP_1_TIME);
            canBeClicked = false;
        }
    }

    private void setTimer(int seconds) {
        counter = seconds;
        new CountDownTimer(seconds * 1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String time;
                time = String.valueOf(counter / 60);
                time += ":";
                if (counter % 60 < 10) {
                    time += "0";
                }
                time += String.valueOf(counter % 60);
                timerText.setText(time);
                counter--;
            }
            @Override
            public void onFinish() {
                timerText.setText(getString(R.string.finished_string));
                timerFinished();
            }
        }.start();
    }

    private void timerFinished() {
        if (currStep == 1) {
            triggerTakePicturePopup();
        }
    }

    private void triggerStep1Popup() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_collection_step1, null);

        // create the popup window
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
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

        /* Launch popup once activity begins running */
        findViewById(android.R.id.content).post(new Runnable() {
            public void run() {
                popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
            }
        });

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void triggerTakePicturePopup() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_take_picture, null, false);

        // create the popup window
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        final PopupWindow popupWindow = new PopupWindow(popupView, (int) width, height, true);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dim.setVisibility(View.GONE);
                pictureButtonClicked();
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
    }

    private void pictureButtonClicked() {
        if (debug) {
            Intent getPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(getPhoto, REQUEST_IMAGE_STORAGE);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    cameraAccess = false;
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                }
            }
            if (cameraAccess) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            pictureButtonClicked();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    img = (Bitmap) data.getExtras().get("data");
                    new AsyncImageProcess().execute();
                }
                break;
            case REQUEST_IMAGE_STORAGE:
                if (resultCode == RESULT_OK) {
                    progressBar.setVisibility(View.VISIBLE);
                    dim.setVisibility(View.VISIBLE);
                    Uri imageURI = data.getData();
                    try {
                        img = (Bitmap) MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                        new AsyncImageProcess().execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        /* path to /data/data/yourapp/app_data/imageDir */
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        /* Create imageDir */
        File path = new File(directory + "/" + IMAGE_FILENAME);
        if (path.exists()) {
            Log.d("plz", "File: " + path.getAbsolutePath());
            if(!path.delete()) {
                Log.d("plz", "File failed to delete");
            } else {
                Log.d("plz", "DELETED OLD FILE");
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path, false);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
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

    private void nextStep() {
        Toast.makeText(this, "PPM: " + Integer.toString(val), Toast.LENGTH_LONG).show();
    }

    private class AsyncImageProcess extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            dim.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... params) {
            path = saveToInternalStorage(img);
            val = CvUtil.processImage(path);
            return null;
        }
        @Override
        protected void onPostExecute(Void param) {
            progressBar.setVisibility(View.GONE);
            dim.setVisibility(View.GONE);
            // TODO: call function to do next test strip
            nextStep();
        }
    }
}
