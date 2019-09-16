package insights.water.waterinsightsv005;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class InitialInformationActivity extends AppCompatActivity {

    private Spinner waterTypeSpinner;
    private AppCompatButton continueButton;

    private int spinnerSize;
    private int spinnerSelectedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_information);

        waterTypeSpinner = findViewById(R.id.water_type_spinner);
        continueButton = findViewById(R.id.continue_button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueButtonPress();
            }
        });

        initSpinner();
    }

    private void continueButtonPress() {
        if (spinnerSelectedIndex == spinnerSize) {
            Toast.makeText(this, getString(R.string.please_select_type_string), Toast.LENGTH_SHORT).show();
        } else {
            Intent activity = new Intent(this, DataCollectionActivity.class);
            startActivity(activity);
        }
    }

    private void initSpinner() {
        List<String> waterTypes = Arrays.asList(getResources().getStringArray(R.array.water_type_spinner));
        spinnerSize = waterTypes.size() - 1;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_water_type_selected_background, waterTypes) {
            @Override
            public int getCount() {
                return spinnerSize;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_water_type_dropdown_background);

        waterTypeSpinner.setAdapter(adapter);
        waterTypeSpinner.setSelection(spinnerSize);
        spinnerSelectedIndex = spinnerSize;
        waterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelectedIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}