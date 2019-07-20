package insights.water.waterinsightsv005;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SubmitActivity extends AppCompatActivity {

    public static final int OP_CODE_DRAW_TARGET = 10;
    public static final int OP_CODE_DRAW_REF = 11;
    public static final int OP_CODE_DRAW_SAMPLE = 12;
    public static final int OP_CODE_FIND_PPM = 13;

    public static final String OP_CODE_KEY = "OP_CODE";

    Button showTargetBtn;
    Button showRefBtn;
    Button showSampleBtn;
    Button showPPMBtn;
    Button restart;
    TextView ppmTextView;

    String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        showTargetBtn = findViewById(R.id.show_target_button);
        showRefBtn = findViewById(R.id.show_reference_button);
        showSampleBtn = findViewById(R.id.show_sample_button);
        showPPMBtn = findViewById(R.id.show_ppm_button);
        restart = findViewById(R.id.restartButton);

        showTargetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTarget();
            }
        });

        showRefBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReference();
            }
        });

        showSampleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSample();
            }
        });

        showPPMBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPPM();
            }
        });

        Intent i = getIntent();
        if (i != null) {
//            int ppm = i.getIntExtra("PPM", -1);
//            ppmDisplay.setText("PPM: " + ppm);
            // TODO: Add constant for intent extras
            imgPath = i.getStringExtra("IMAGE");
        }
    }

    private void showTarget() {
        Intent imageActivity = new Intent(this, ShowImageActivity.class);
        imageActivity.putExtra(OP_CODE_KEY, OP_CODE_DRAW_TARGET);
        imageActivity.putExtra("IMAGE", imgPath);
        startActivity(imageActivity);
    }

    private void showReference() {
        Intent imageActivity = new Intent(this, ShowImageActivity.class);
        imageActivity.putExtra(OP_CODE_KEY, OP_CODE_DRAW_REF);
        imageActivity.putExtra("IMAGE", imgPath);
        startActivity(imageActivity);
    }

    private void showSample() {
        Intent imageActivity = new Intent(this, ShowImageActivity.class);
        imageActivity.putExtra(OP_CODE_KEY, OP_CODE_DRAW_SAMPLE);
        Log.d("plz", imgPath);
        imageActivity.putExtra("IMAGE", imgPath);
        startActivity(imageActivity);
    }

    private void showPPM() {
        Intent imageActivity = new Intent(this, ShowImageActivity.class);
        imageActivity.putExtra(OP_CODE_KEY, OP_CODE_FIND_PPM);
        imageActivity.putExtra("IMAGE", imgPath);
        startActivity(imageActivity);
    }

    public void restartAction(View view) {
        Intent i = new Intent(this, CollectDataActivity.class);
        startActivity(i);
    }
}
