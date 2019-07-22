package insights.water.waterinsightsv005;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class DataCollectionActivity extends AppCompatActivity {

    private static final int STEP_1_TIME = 30;

    TextView timerText;
    AppCompatButton startTimerButton;
    FrameLayout dim;

    public int counter = 0;
    private boolean canBeClicked = true;
    private int currStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        startTimerButton = findViewById(R.id.start_timer_button);
        dim = findViewById(R.id.background_dim_frame);
        timerText = findViewById(R.id.timer_view);

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
            }
        }.start();
    }

    private void triggerStep1Popup() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_collection_step1, null);

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
}
