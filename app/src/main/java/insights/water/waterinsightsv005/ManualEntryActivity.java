package insights.water.waterinsightsv005;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class ManualEntryActivity extends AppCompatActivity {

    private Spinner nitrateSpinner;
    private Spinner nitriteSpiner;

    int nitrateSpinnerIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        nitrateSpinner = findViewById(R.id.nitrate_spinner);

        initSpinners();
    }

    private void initSpinners() {
        /* Nitrate */
        List<String> waterTypes = Arrays.asList(getResources().getStringArray(R.array.nitrate_spinner_values));
        final int nitrateSpinnerSize = waterTypes.size() - 1;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_water_type_selected_background, waterTypes) {
            @Override
            public int getCount() {
                return nitrateSpinnerSize;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_water_type_dropdown_background);

        nitrateSpinner.setAdapter(adapter);
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
    }
}
