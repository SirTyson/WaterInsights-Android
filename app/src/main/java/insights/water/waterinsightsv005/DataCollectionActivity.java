package insights.water.waterinsightsv005;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.TextView;

public class DataCollectionActivity extends AppCompatActivity {

    public static final String STEP_KEY = "STEP_KEY";

    private static final int STEP_1_TIME = 30;

    TextView timerText;
    AppCompatButton startTimerButton;

    public int counter = 0;
    private boolean isStart = true;
    private int currStep = 1;

    private CountDownTimer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        startTimerButton = findViewById(R.id.start_timer_button);
        timerText = findViewById(R.id.timer_view);

        if (currStep == 1) {
            timerText.setText("0:30");
        }

        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimerClicked();
            }
        });
    }

    private void startTimerClicked() {
        if (isStart) {
            isStart = false;
            startTimerButton.setText(getString(R.string.reset_timer_string));
        }
        if (currStep == 1) {
            setTimer(STEP_1_TIME);
            isStart = false;
        }
    }

    private void setTimer(int seconds) {
        counter = seconds;
        if (timer == null) {
            timer = new CountDownTimer(seconds * 1000,1000) {
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
        } else {
            timer.cancel();
            timer.start();
        }
    }

    private void timerFinished() {
        Intent intent = new Intent(this, TakePictureActivity.class);
        intent.putExtra(STEP_KEY, currStep);
        startActivity(intent);
        finish();
    }
}
