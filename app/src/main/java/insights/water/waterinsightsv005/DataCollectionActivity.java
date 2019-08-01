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
    private static final int STEP_5_TIME = 60;

    private TextView timerText;
    private AppCompatButton startTimerButton;
    private TextView instructionText;

    public int counter = 0;
    private boolean isStart = true;
    private int currStep;

    private CountDownTimer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        Intent starter = getIntent();
        currStep = starter.getIntExtra(STEP_KEY, 1);

        startTimerButton = findViewById(R.id.start_timer_button);
        timerText = findViewById(R.id.timer_view);
        instructionText = findViewById(R.id.instruction_body_text);

        if (currStep == 5) {
            timerText.setText("1:00");
        } else {
            timerText.setText("0:30");
        }

        switch (currStep) {
            case 1:
                instructionText.setText(R.string.step1_instructions);
                break;
            case 2:
                instructionText.setText(R.string.step2_instructions);
                break;
            case 3:
                instructionText.setText(R.string.step3_instructions);
                break;
            case 4:
                instructionText.setText(R.string.step4_instructions);
                break;
            case 5:
                instructionText.setText(R.string.step5_instructions);
                break;
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
        if (currStep == 5) {
            setTimer(STEP_5_TIME);
        } else {
            setTimer(STEP_1_TIME);
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
        intent.putExtra(STEP_KEY, currStep);
        startActivity(intent);
        finish();
    }
}
