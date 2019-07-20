package insights.water.waterinsightsv005;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import static insights.water.waterinsightsv005.CollectDataActivity.IMAGE_FILENAME;

public class SubmitActivity extends AppCompatActivity {

    Button restart;
    TextView ppmTextView;

    String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        restart = findViewById(R.id.restartButton);
        ppmTextView = findViewById(R.id.ppm_text_view);

        Intent i = getIntent();
        if (i != null) {
//            int ppm = i.getIntExtra("PPM", -1);
//            ppmDisplay.setText("PPM: " + ppm);
            // TODO: Add constant for intent extras
            imgPath = i.getStringExtra("IMAGE");
        }
        findPPM();
    }

    public void findPPM() {
        int ppmVal = CvUtil.processImage(imgPath + "/" + IMAGE_FILENAME);
        Log.d("plz", "PPM: " + ppmVal);
        ppmTextView.setText("PPM: " + ppmVal);
    }

    public void restartAction(View view) {
        Intent i = new Intent(this, CollectDataActivity.class);
        startActivity(i);
    }
}
