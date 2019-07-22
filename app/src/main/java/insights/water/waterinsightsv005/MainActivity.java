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

    public static final String WATER_INSIGHTS_URL = "http://waterinsights.org";

    AppCompatTextView learnMoreLink;
    AppCompatButton guestButton;
    AppCompatButton scistarterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        learnMoreLink = findViewById(R.id.learn_more_link);
        guestButton = findViewById(R.id.guest_button);
        scistarterButton = findViewById(R.id.scistarter_sign_in_button);

        learnMoreLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToWebsite();
            }
        });

        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guestLogin();
            }
        });
    }

    private void goToWebsite() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WATER_INSIGHTS_URL)));
    }

    private void guestLogin() {
        startActivity(new Intent(this, OverviewActivity.class));
        finish();
    }

}
