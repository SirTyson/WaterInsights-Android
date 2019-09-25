package insights.water.waterinsightsv005;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class ShowResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_results);

        String mUserID = PreferenceUtilities.getFromPreferences(PreferenceUtilities.SCISTARTER_USER_ID_PREFERENCE_KEY, this);
        if (mUserID != null) {
            ScistarterUtilities.completeTestingPost(mUserID);
        }

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.results_list_frame, new ResultsListFragment()).commit();

        Button testAgainButton = findViewById(R.id.test_again_button);
        testAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testAgainPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    private void testAgainPressed() {
        startActivity(new Intent(this, InitialInformationActivity.class));
    }
}
