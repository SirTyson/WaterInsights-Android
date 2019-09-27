package insights.water.waterinsightsv005;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class DataCollectionActivity extends AppCompatActivity {

    public static final int NUM_RESULTS = 6;
    public static final boolean debug = true;
    private static final String COUNTER_BUNDLE_KEY = "countBundleKey";
    private static final String IS_RUNNING_BUNDLE_KEY = "isRunningBundleKey";
    private static final String END_TIME_KEY = "endTimeKey";
    private static final int S6_TIME = 30;
    public int counter = 0;
    private TextView timerText;
    private AppCompatButton startTimerButton;
    private TextView instructionText;
    private boolean isRunning;

    @Nullable
    private CountDownTimer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        startTimerButton = findViewById(R.id.start_timer_button);
        timerText = findViewById(R.id.timer_view);
        instructionText = findViewById(R.id.instruction_body_text);

        if (savedInstanceState == null) {
            counter = S6_TIME;
            isRunning = false;
        }

        setTimeTextView();
        instructionText.setText(R.string.step1_instructions);

        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimerClicked();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(COUNTER_BUNDLE_KEY, counter);
        outState.putLong(END_TIME_KEY, System.currentTimeMillis());
        outState.putBoolean(IS_RUNNING_BUNDLE_KEY, isRunning);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        counter = savedInstanceState.getInt(COUNTER_BUNDLE_KEY);
        isRunning = savedInstanceState.getBoolean(IS_RUNNING_BUNDLE_KEY);
        long endTime = savedInstanceState.getLong(END_TIME_KEY);
        if (isRunning) {
            startTimerButton.setText(getString(R.string.reset_timer_string));
            counter -= (int) ((System.currentTimeMillis() - endTime) / 1000);
            if (counter <= 0) {
                timerFinished();
            } else {
                setTimer(counter);
            }
        }
    }

    private void startTimerClicked() {
        if (!isRunning) {
            isRunning = true;
            startTimerButton.setText(getString(R.string.reset_timer_string));
        } else {
            counter = S6_TIME;
        }

        setTimer(counter);
        isRunning = true;
    }

    private void setTimer(int seconds) {
        counter = seconds;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setTimeTextView();
                counter--;
            }

            @Override
            public void onFinish() {
                timerText.setText(getString(R.string.finished_string));
                timerFinished();
            }
        }.start();
    }

    private void setTimeTextView() {
        String time;
        time = String.valueOf(counter / 60);
        time += ":";
        if (counter % 60 < 10) {
            time += "0";
        }
        time += String.valueOf(counter % 60);
        timerText.setText(time);
    }

    private void timerFinished() {
        Intent intent = new Intent(this, TakePictureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
