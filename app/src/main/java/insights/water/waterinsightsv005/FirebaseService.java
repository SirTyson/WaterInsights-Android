package insights.water.waterinsightsv005;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

import androidx.annotation.NonNull;

import static insights.water.waterinsightsv005.TakePictureActivity.IMAGE_FILENAME;

public class FirebaseService {

    private static final String TAG = "FirebaseService";

    private static final String VISION_VERSION = "1.0.0";
    private static final boolean SHOULD_TEST_VAL = true;

    private static final String ROOT_DATA_NODE = "androidData";
    private static final String ACTUAL_NODE = "Actual";
    private static final String EXPECTED_NODE = "Expected";
    private static final String IMAGE_KEY = "Image";
    private static final String LOCATION_KEY = "Location";
    private static final String OBSERVATIONS_KEY = "Observations";
    private static final String SHOULD_TEST_KEY = "ShouldTest";
    private static final String TIMESTAMP_KEY = "Timestamp";
    private static final String USER_KEY = "User";
    private static final String VISION_VERSION_KEY = "VisionVersion";
    private static final String WATER_TYPE_KEY = "WaterType";
    private static final String NITRATE_KEY = "Nitrate";
    private static final String NITRITE_KEY = "Nitrite";
    private static final String PH_KEY = "PH";
    private static final String T_ALKALINITY_KEY = "TotalAlkalinity";
    private static final String T_CHLORINE_KEY = "TotalChlorine";
    private static final String T_HARDNESS_KEY = "TotalHardness";

    private static final String IMAGE_STORAGE_ROOT = "androidTestImages";

    private FirebaseDatabase mDatabaseReference;
    private FirebaseStorage mStorage;
    private Context mContext;

    FirebaseService(@NonNull Context context) {
        mDatabaseReference = FirebaseDatabase.getInstance();
        mContext = context;
        mStorage = FirebaseStorage.getInstance();
    }

    void writeData() {
        float[] actual = PreferenceUtilities.getAnalysisResultsPreferences(mContext);
        float[] expected = PreferenceUtilities.getExpectedResultsPreferences(mContext);
        String location = PreferenceUtilities.getFromPreferences(PreferenceUtilities.LOCATION_KEY, mContext);
        String observations = PreferenceUtilities.getFromPreferences(PreferenceUtilities.OBSERVATIONS_KEY, mContext);
        String user = PreferenceUtilities.getFromPreferences(PreferenceUtilities.SCISTARTER_USER_ID_PREFERENCE_KEY, mContext);
        String waterType = PreferenceUtilities.getFromPreferences(PreferenceUtilities.WATER_TYPE_KEY, mContext);

        DatabaseReference root = mDatabaseReference.getReference(ROOT_DATA_NODE);
        final DatabaseReference newNode = root.push();

        newNode.child(LOCATION_KEY).setValue(location);
        newNode.child(OBSERVATIONS_KEY).setValue(observations);
        newNode.child(SHOULD_TEST_KEY).setValue(SHOULD_TEST_VAL);
        newNode.child(TIMESTAMP_KEY).setValue(System.currentTimeMillis());
        newNode.child(USER_KEY).setValue(user);
        newNode.child(VISION_VERSION_KEY).setValue(VISION_VERSION);
        newNode.child(WATER_TYPE_KEY).setValue(waterType);

        if (actual != null) {
            DatabaseReference actualNode = newNode.child(ACTUAL_NODE);
            actualNode.child(NITRATE_KEY).setValue(actual[0]);
            actualNode.child(NITRITE_KEY).setValue(actual[1]);
            actualNode.child(T_HARDNESS_KEY).setValue(actual[2]);
            actualNode.child(T_CHLORINE_KEY).setValue(actual[3]);
            actualNode.child(T_ALKALINITY_KEY).setValue(actual[4]);
            actualNode.child(PH_KEY).setValue(actual[5]);
        }

        if (expected != null) {
            DatabaseReference expectedNode = newNode.child(EXPECTED_NODE);
            expectedNode.child(NITRATE_KEY).setValue(expected[0]);
            expectedNode.child(NITRITE_KEY).setValue(expected[1]);
            expectedNode.child(T_HARDNESS_KEY).setValue(expected[2]);
            expectedNode.child(T_CHLORINE_KEY).setValue(expected[3]);
            expectedNode.child(T_ALKALINITY_KEY).setValue(expected[4]);
            expectedNode.child(PH_KEY).setValue(expected[5]);
        }

        StorageReference imageRoot = mStorage.getReference().child(IMAGE_STORAGE_ROOT);

        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        Uri imageFile = Uri.fromFile(new File(directory + "/" + IMAGE_FILENAME));

        String uniqueID = UUID.randomUUID().toString() + ".jpeg";
        final StorageReference newImage = imageRoot.child(uniqueID);

        UploadTask task = newImage.putFile(imageFile);

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "writeData: Failed to upload image");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                newImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        newNode.child(IMAGE_KEY).setValue(uri.toString());
                    }
                });
            }
        });
    }
}
