package insights.water.waterinsightsv005;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void collectDataButton(View view) {
        Intent startActivity = new Intent(this, CollectDataActivity.class);
        startActivity(startActivity);
        finish();
    }
}
