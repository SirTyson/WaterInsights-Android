package insights.water.waterinsightsv005;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class InitialInformationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "InitialInformationAct";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private Spinner waterTypeSpinner;
    private AppCompatButton continueButton;
    private int spinnerSize;
    private int spinnerSelectedIndex;
    private boolean mLocationPermissionsGranted;
    private GoogleMap mPlacePicker;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private AppCompatEditText mLocationSearchText;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mPlacePicker = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            mPlacePicker.setMyLocationEnabled(true);
            mPlacePicker.getUiSettings().setMyLocationButtonEnabled(false);
            mPlacePicker.getUiSettings().setCompassEnabled(false);
            mPlacePicker.getUiSettings().setTiltGesturesEnabled(false);
            mPlacePicker.getUiSettings().setIndoorLevelPickerEnabled(false);
            mPlacePicker.getUiSettings().setMapToolbarEnabled(false);
            init();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_information);

        waterTypeSpinner = findViewById(R.id.water_type_spinner);
        continueButton = findViewById(R.id.continue_button);
        mLocationSearchText = findViewById(R.id.input_search);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueButtonPress();
            }
        });


        initSpinner();
        getLocationPermission();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void init() {
        Log.d(TAG, "init: initializing");

        mLocationSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "onEditorAction: key pressed: " + actionId);
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    geoLocate();
                    hideKeyboard();
                    return true;
                }

                return false;
            }
        });
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geoLocating");

        String searchString = mLocationSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(InitialInformationActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException ex) {
            Log.e(TAG, "geoLocate: IOException: " + ex.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found location: " + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }
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

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionsGranted = true;
            initPlacePicker();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, getString(R.string.my_location_string));
                        } else {
                            Log.d(TAG, "onComplete: could not find location");
                            Toast.makeText(InitialInformationActivity.this, getString(R.string.location_error_string), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch (SecurityException ex) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + ex.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mPlacePicker.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        mPlacePicker.addMarker(options);
    }

    private void initPlacePicker() {
        Log.d(TAG, "initPlacePicker: initializing place picker");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.place_picker);
        mapFragment.getMapAsync(InitialInformationActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "onRequestPermissionsResult: Location Permission Failed");
                            return;
                        }
                    }

                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    initPlacePicker();
                }
        }
    }
}