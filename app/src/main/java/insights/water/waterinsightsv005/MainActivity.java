package insights.water.waterinsightsv005;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String WATER_INSIGHTS_URL = "https://www.waterinsights.org";

    AppCompatTextView learnMoreLink;
    AppCompatButton guestButton;
    AppCompatButton scistarterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Skip intro screen if already logged in */
        if (PreferenceUtilities.getFromPreferences(PreferenceUtilities.SCISTARTER_USER_ID_PREFERENCE_KEY, this) != null) {
            guestLogin();
            finish();
        }

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

        scistarterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scistarterLogin();
            }
        });
    }

    private void goToWebsite() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WATER_INSIGHTS_URL)));
    }

    private void guestLogin() {
        startActivity(new Intent(this, OverviewActivity.class));
    }

    private void scistarterLogin() {
        startActivity(new Intent(this, ScistarterLoginActivity.class));
        finish();
    }
}
