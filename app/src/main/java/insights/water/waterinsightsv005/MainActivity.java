package insights.water.waterinsightsv005;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

public class MainActivity extends AppCompatActivity {

    public static final String WATER_INSIGHTS_URL = "https://www.waterinsights.org";

    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    AppCompatTextView learnMoreLink;
    AppCompatButton guestButton;
    AppCompatButton scistarterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initButtons();

        if (!isServicesOK()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Log.wtf(TAG, "onCreate: Interrupted thread while waiting to kill app.");
                    }

                    finishAffinity();
                    System.exit(0);
                }
            }).start();
        }
    }

    private void initButtons() {
        learnMoreLink = findViewById(R.id.learn_more_link);
        guestButton = findViewById(R.id.guest_button);
        scistarterButton = findViewById(R.id.scistarter_sign_in_button);

        /* If user is signed in */
        if (PreferenceUtilities.getFromPreferences(PreferenceUtilities.SCISTARTER_USER_ID_PREFERENCE_KEY, this) != null) {
            guestButton.setText(R.string.continue_string);
            scistarterButton.setText(R.string.logout_scistarter_string);
            scistarterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scistarterLogout();
                }
            });
        } else {
            guestButton.setText(R.string.start_screen_guest_sign_in);
            scistarterButton.setText(R.string.start_screen_scistarter_sign_in);
            scistarterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scistarterLogin();
                }
            });
        }

        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guestLogin();
            }
        });

        learnMoreLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToWebsite();
            }
        });
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking Google Services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: Google Play Services threw resolvable error");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Log.d(TAG, "isServicesOK: Google Play Services threw fatal error");
            Toast.makeText(this, getString(R.string.play_services_fatal_error_msg), Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void goToWebsite() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WATER_INSIGHTS_URL)));
    }

    private void guestLogin() {
        startActivity(new Intent(this, OverviewActivity.class));
        finish();
    }

    private void scistarterLogin() {
        startActivity(new Intent(this, ScistarterLoginActivity.class));
    }

    private void scistarterLogout() {
        PreferenceUtilities.removeFromPreferences(PreferenceUtilities.SCISTARTER_USER_ID_PREFERENCE_KEY, MainActivity.this);
        initButtons();
        Toast.makeText(this, getString(R.string.logout_scistarter_message_string), Toast.LENGTH_SHORT).show();
    }
}
