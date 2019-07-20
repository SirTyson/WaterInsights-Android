package insights.water.waterinsightsv005;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.util.Linkify;
import android.view.View;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    AppCompatTextView learnMoreLink;
    AppCompatButton guestButton;
    AppCompatButton scistarterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        learnMoreLink = findViewById(R.id.learn_more_link);
        learnMoreLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://waterinsights.org")));
            }
        });
    }

    public void collectDataButton(View view) {
        Intent startActivity = new Intent(this, CollectDataActivity.class);
        startActivity(startActivity);
        finish();
    }
}
