package insights.water.waterinsightsv005;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SubmitActivity extends AppCompatActivity {

    TextView ppmDisplay;
    Button restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        ppmDisplay = (TextView) findViewById(R.id.ppmDisplay);
        restart = (Button) findViewById(R.id.restartButton);
        Intent i = getIntent();
        if (i != null) {
            int ppm = i.getIntExtra("PPM", -1);
            ppmDisplay.setText("PPM: " + ppm);
        }
    }

    public void restartAction(View view) {
        Intent i = new Intent(this, CollectDataActivity.class);
        startActivity(i);
    }
}
