package insights.water.waterinsightsv005;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ManualEntryActivity extends AppCompatActivity {

    private static final float[] NITRATE_VALUES = {0.0f, 20.0f, 40.0f, 80.0f, 160.0f, 200.0f};
    private static final float[] NITRITE_VALUES = {0.0f, 0.5f, 1.0f, 3.0f, 5.0f, 10.0f};
    private static final float[] T_HARDNESS_VALUES = {0.0f, 25f, 75f, 150f, 300f};
    private static final float[] T_CHLORINE_VALUES = {0.0f, 0.5f, 1.0f, 2.0f, 4.0f, 6.0f};
    private static final float[] T_ALKALINITY_VALUES = {0.0f, 40f, 80f, 120f, 180f, 300f};
    private static final float[] PH_VALUES = {6.2f, 6.8f, 7.2f, 7.8f, 8.4f};

    int nitrateSpinnerIndex;
    int nitriteSpinnerIndex;
    int hardnessSpinnerIndex;
    int chlorineSpinnerIndex;
    int alkalinitySpinnerIndex;
    int phSpinnerIndex;

    private Spinner nitrateSpinner;
    private Spinner nitriteSpinner;
    private Spinner hardnessSpinner;
    private Spinner chlorineSpinner;
    private Spinner alkalinitySpinner;
    private Spinner phSpinner;

    private AppCompatButton continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        nitrateSpinner = findViewById(R.id.nitrate_spinner);
        nitriteSpinner = findViewById(R.id.nitrite_spinner);
        hardnessSpinner = findViewById(R.id.hardness_spinner);
        chlorineSpinner = findViewById(R.id.chlorine_spinner);
        alkalinitySpinner = findViewById(R.id.alkalinity_spinner);
        phSpinner = findViewById(R.id.ph_spinner);

        continueButton = findViewById(R.id.manual_entry_continue_button);

        initSpinners();

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueButtonPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    private void initSpinners() {
        /* Nitrate */
        List<String> nitrateTypes = Arrays.asList(getResources().getStringArray(R.array.nitrate_spinner_values));
        final int nitrateSpinnerSize = nitrateTypes.size();

        ArrayAdapter<String> nitrateAdapter = new ArrayAdapter<String>(this, R.layout.spinner_water_type_selected_background, nitrateTypes) {
            @Override
            public int getCount() {
                return nitrateSpinnerSize;
            }
        };

        nitrateAdapter.setDropDownViewResource(R.layout.spinner_water_type_dropdown_background);

        nitrateSpinner.setAdapter(nitrateAdapter);
        nitrateSpinner.setSelection(0);
        nitrateSpinnerIndex = 0;
        nitrateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nitrateSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* Hardness */
        List<String> nitriteTypes = Arrays.asList(getResources().getStringArray(R.array.nitrite_spinner_values));
        final int nitriteSpinnerSize = nitriteTypes.size();

        ArrayAdapter<String> nitriteAdapter = new ArrayAdapter<String>(this, R.layout.spinner_water_type_selected_background, nitriteTypes) {
            @Override
            public int getCount() {
                return nitriteSpinnerSize;
            }
        };

        nitriteAdapter.setDropDownViewResource(R.layout.spinner_water_type_dropdown_background);

        nitriteSpinner.setAdapter(nitriteAdapter);
        nitriteSpinner.setSelection(0);
        nitriteSpinnerIndex = 0;
        nitriteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nitriteSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* Hardness */
        List<String> hardnessTypes = Arrays.asList(getResources().getStringArray(R.array.total_hardness_spinner_values));
        final int hardnessSpinnerSize = hardnessTypes.size();

        ArrayAdapter<String> hardnessAdapter = new ArrayAdapter<String>(this, R.layout.spinner_water_type_selected_background, hardnessTypes) {
            @Override
            public int getCount() {
                return hardnessSpinnerSize;
            }
        };

        hardnessAdapter.setDropDownViewResource(R.layout.spinner_water_type_dropdown_background);

        hardnessSpinner.setAdapter(hardnessAdapter);
        hardnessSpinner.setSelection(0);
        hardnessSpinnerIndex = 0;
        hardnessSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hardnessSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* Chlorine */
        List<String> chlorineTypes = Arrays.asList(getResources().getStringArray(R.array.total_chlorine_spinner_values));
        final int chlorineSpinnerSize = chlorineTypes.size();

        ArrayAdapter<String> chlorineAdapter = new ArrayAdapter<String>(this, R.layout.spinner_water_type_selected_background, chlorineTypes) {
            @Override
            public int getCount() {
                return chlorineSpinnerSize;
            }
        };

        chlorineAdapter.setDropDownViewResource(R.layout.spinner_water_type_dropdown_background);

        chlorineSpinner.setAdapter(chlorineAdapter);
        chlorineSpinner.setSelection(0);
        chlorineSpinnerIndex = 0;
        chlorineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chlorineSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* Alkalinity */
        List<String> alkalinityTypes = Arrays.asList(getResources().getStringArray(R.array.total_alkalinity_spinner_values));
        final int alkalinitySpinnerSize = alkalinityTypes.size();

        ArrayAdapter<String> alkalinityAdapter = new ArrayAdapter<String>(this, R.layout.spinner_water_type_selected_background, alkalinityTypes) {
            @Override
            public int getCount() {
                return alkalinitySpinnerSize;
            }
        };

        alkalinityAdapter.setDropDownViewResource(R.layout.spinner_water_type_dropdown_background);

        alkalinitySpinner.setAdapter(alkalinityAdapter);
        alkalinitySpinner.setSelection(0);
        alkalinitySpinnerIndex = 0;
        alkalinitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                alkalinitySpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* PH */
        List<String> phTypes = Arrays.asList(getResources().getStringArray(R.array.ph_spinner_values));
        final int phSpinnerSize = phTypes.size();

        ArrayAdapter<String> phAdapter = new ArrayAdapter<String>(this, R.layout.spinner_water_type_selected_background, phTypes) {
            @Override
            public int getCount() {
                return phSpinnerSize;
            }
        };

        phAdapter.setDropDownViewResource(R.layout.spinner_water_type_dropdown_background);

        phSpinner.setAdapter(phAdapter);
        phSpinner.setSelection(0);
        phSpinnerIndex = 0;
        phSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                phSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void continueButtonPressed() {
        float[] results = {NITRATE_VALUES[nitrateSpinnerIndex],
                NITRITE_VALUES[nitriteSpinnerIndex],
                T_HARDNESS_VALUES[hardnessSpinnerIndex],
                T_CHLORINE_VALUES[chlorineSpinnerIndex],
                T_ALKALINITY_VALUES[alkalinitySpinnerIndex],
                PH_VALUES[phSpinnerIndex]};

        PreferenceUtilities.putExpectedAnalysisResultsPreferences(results, ManualEntryActivity.this);
        startActivity(new Intent(this, ShowResultsActivity.class));
        finish();
    }
}