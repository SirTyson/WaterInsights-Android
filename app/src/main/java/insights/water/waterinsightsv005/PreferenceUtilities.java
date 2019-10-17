package insights.water.waterinsightsv005;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PreferenceUtilities {

    public static final String SCISTARTER_USER_ID_PREFERENCE_KEY = "SciStarterUserIDPref";
    public static final String ANALYSIS_SUCCESS_KEY = "analysisSuccessKey";
    public static final String LOCATION_KEY = "locationKey";
    public static final String WATER_TYPE_KEY = "waterTypeKey";
    public static final String OBSERVATIONS_KEY = "observationsKey";

    public static final String NITRATE_RESULTS_PREFERENCE_KEY = "nitrateKey";
    public static final String NITRITE_RESULTS_PREFERENCE_KEY = "nitriteKey";
    public static final String T_HARDNESS_RESULTS_PREFERENCE_KEY = "totalHardnessKey";
    public static final String T_CHLORINE_RESULTS_PREFERENCE_KEY = "totalChlorineKey";
    public static final String T_ALKALINITY_RESULTS_PREFERENCE_KEY = "totalAlkalinityKey";
    public static final String PH_RESULTS_PREFERENCE_KEY = "PHKey";

    public static final String EXPECTED_NITRATE_KEY = "expectedNitrateKey";
    public static final String EXPECTED_NITRITE_KEY = "expectedNitriteKey";
    public static final String EXPECTED_T_HARDNESS_KEY = "expectedTotalHardnessKey";
    public static final String EXPECTED_T_CHLORINE_KEY = "expectedTotalChlorineKey";
    public static final String EXPECTED_PH_KEY = "expectedPHKey";
    public static final String EXPECTED_T_ALAKLINITY_KEY = "expectedTotalAlkalinityKey";

    private static void writeToPreferences(@NonNull String key, @NonNull String value, @NonNull Context context, boolean isAsync) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        if (isAsync) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static void writeBoolToPreferences(@NonNull String key, boolean value, @NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolFromPreferences(@NonNull String key, @NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, false);
    }

    public static void writeToPreferences(@NonNull String key, @NonNull String value, @NonNull Context context) {
        writeToPreferences(key, value, context, false);
    }

    public static void writeToPreferencesAsync(@NonNull String key, @NonNull String value, @NonNull Context context) {
        writeToPreferences(key, value, context, true);
    }

    public static void removeFromPreferences(@NonNull String key, @NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
    }

    @Nullable
    public static String getFromPreferences(@NonNull String key, @NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public static void putAnalysisResultsPreferences(@NonNull float[] results, @NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(NITRATE_RESULTS_PREFERENCE_KEY, results[0]);
        editor.putFloat(NITRITE_RESULTS_PREFERENCE_KEY, results[1]);
        editor.putFloat(T_HARDNESS_RESULTS_PREFERENCE_KEY, results[2]);
        editor.putFloat(T_CHLORINE_RESULTS_PREFERENCE_KEY, results[3]);
        editor.putFloat(T_ALKALINITY_RESULTS_PREFERENCE_KEY, results[4]);
        editor.putFloat(PH_RESULTS_PREFERENCE_KEY, results[5]);
        editor.apply();
    }

    public static void putExpectedAnalysisResultsPreferences(@NonNull float[] results, @NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(EXPECTED_NITRATE_KEY, results[0]);
        editor.putFloat(EXPECTED_NITRITE_KEY, results[1]);
        editor.putFloat(EXPECTED_T_HARDNESS_KEY, results[2]);
        editor.putFloat(EXPECTED_T_CHLORINE_KEY, results[3]);
        editor.putFloat(EXPECTED_T_ALAKLINITY_KEY, results[4]);
        editor.putFloat(EXPECTED_PH_KEY, results[5]);
        editor.apply();
    }

    @Nullable
    public static float[] getAnalysisResultsPreferences(@NonNull Context context) {
        float[] results = new float[6];
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        results[0] = preferences.getFloat(NITRATE_RESULTS_PREFERENCE_KEY, -1);
        results[1] = preferences.getFloat(NITRITE_RESULTS_PREFERENCE_KEY, -1);
        results[2] = preferences.getFloat(T_HARDNESS_RESULTS_PREFERENCE_KEY, -1);
        results[3] = preferences.getFloat(T_CHLORINE_RESULTS_PREFERENCE_KEY, -1);
        results[4] = preferences.getFloat(T_ALKALINITY_RESULTS_PREFERENCE_KEY, -1);
        results[5] = preferences.getFloat(PH_RESULTS_PREFERENCE_KEY, -1);

        for (float item : results) {
            if (item == -1) {
                return null;
            }
        }

        return results;
    }

    @Nullable
    public static float[] getExpectedResultsPreferences(@NonNull Context context) {
        float[] results = new float[6];
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        results[0] = preferences.getFloat(EXPECTED_NITRATE_KEY, -1);
        results[1] = preferences.getFloat(EXPECTED_NITRITE_KEY, -1);
        results[2] = preferences.getFloat(EXPECTED_T_HARDNESS_KEY, -1);
        results[3] = preferences.getFloat(EXPECTED_T_CHLORINE_KEY, -1);
        results[4] = preferences.getFloat(EXPECTED_T_ALAKLINITY_KEY, -1);
        results[5] = preferences.getFloat(EXPECTED_PH_KEY, -1);

        for (float item : results) {
            if (item == -1) {
                return null;
            }
        }

        return results;
    }
}
