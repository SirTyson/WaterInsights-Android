package insights.water.waterinsightsv005;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

/**
 * A login screen that offers login via email/password.
 */
public class ScistarterLoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    @Nullable
    private UserLoginTask mAuthTask = null;
    @Nullable
    private String mProfileID = null;

    // UI references.
    private EditText mEmailView;
    private ConstraintLayout mProgressBar;
    private FrameLayout mDim;
    private Button mEmailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scistarter_login);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        AppCompatTextView registerLink = findViewById(R.id.register_link);
        registerLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                registerEmail();
            }
        });

        mProgressBar = findViewById(R.id.signin_progress_bar_layout);
        mDim = findViewById(R.id.signin_background_dim_frame);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void registerEmail() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ScistarterUtilities.SCISTARTER_REGISTER_URL)));
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(email);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(@NonNull String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;

        UserLoginTask(String email) {
            mEmail = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mDim.setVisibility(View.VISIBLE);
            mEmailSignInButton.setClickable(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Response response = ScistarterUtilities.profileIDRequest(mEmail);
                if (!response.isSuccessful() || response.body() == null) {
                    return false;
                }

                JSONObject json = new JSONObject(response.body().string());
                if (!json.getBoolean(ScistarterUtilities.SCISTARTER_RESPONSE_IS_KNOWN_KEY)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ScistarterLoginActivity.this, getString(R.string.email_not_found), Toast.LENGTH_LONG).show();
                        }
                    });

                    return false;
                }

                mProfileID = json.getString(ScistarterUtilities.SCISTARTER_RESPONSE_PROFILE_ID_KEY);

            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            } catch (JSONException ex) {
                ex.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            mProgressBar.setVisibility(View.GONE);
            mDim.setVisibility(View.GONE);
            mEmailSignInButton.setClickable(true);

            if (success && mProfileID != null) {
                Toast.makeText(ScistarterLoginActivity.this, getString(R.string.successful_login), Toast.LENGTH_SHORT).show();
                PreferenceUtilities.writeToPreferencesAsync(PreferenceUtilities.SCISTARTER_USER_ID_PREFERENCE_KEY, mProfileID, ScistarterLoginActivity.this);
                ScistarterUtilities.signinPost(mProfileID);
                startActivity(new Intent(ScistarterLoginActivity.this, OverviewActivity.class));
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            mProgressBar.setVisibility(View.GONE);
            mDim.setVisibility(View.GONE);
            mEmailSignInButton.setClickable(true);
        }
    }
}

