package insights.water.waterinsightsv005;

import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class DataCollectionActivity extends AppCompatActivity {

    TextView timerText;
    public int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        timerText = findViewById(R.id.timer_view);
        timerText.setText("0:30");
        triggerStep1Popup();
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
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }

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
