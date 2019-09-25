package insights.water.waterinsightsv005;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PreferenceUtilities {

    public static final String SCISTARTER_USER_ID_PREFERENCE_KEY = "SciStarterUserIDPref";

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

    public static void writeToPreferences(@NonNull String key, @NonNull String value, @NonNull Context context) {
        writeToPreferences(key, value, context, false);
    }

    public static void writeToPreferencesAsync(@NonNull String key, @NonNull String value, @NonNull Context context) {
        writeToPreferences(key, value, context, true);
    }

    @Nullable
    public static String getFromPreferences(@NonNull String key, @NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
}
