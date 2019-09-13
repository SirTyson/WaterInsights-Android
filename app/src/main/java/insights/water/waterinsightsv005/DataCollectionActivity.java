package insights.water.waterinsightsv005;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.TextView;

public class DataCollectionActivity extends AppCompatActivity {

    private static final boolean debug = true;

    public static final String STEP_KEY = "STEP_KEY";
    public static final String RESULTS_KEY = "RESULT_KEY";
    public static final int NUM_RESULTS = 8;

    private static final int S6_TIME = 30;

    private TextView timerText;
    private AppCompatButton startTimerButton;
    private TextView instructionText;

    public int counter = 0;
    private boolean isStart = true;
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

    private CountDownTimer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        startTimerButton = findViewById(R.id.start_timer_button);
        timerText = findViewById(R.id.timer_view);
        instructionText = findViewById(R.id.instruction_body_text);

        // TODO: Localize
        timerText.setText("1:00");
        instructionText.setText(R.string.step1_instructions);

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

        if (debug) {
            setTimer(2);
        } else {
            setTimer(S6_TIME);
        }

        isStart = false;
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
        startActivity(intent);
        finish();
    }
}
