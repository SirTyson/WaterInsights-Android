package insights.water.waterinsightsv005;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class ScistarterUtilities {

    public static final String SCISTARTER_REGISTER_URL = "https://scistarter.org/login";
    public static final String SCISTARTER_RESPONSE_IS_KNOWN_KEY = "known";
    public static final String SCISTARTER_RESPONSE_PROFILE_ID_KEY = "scistarter_profile_id";

    private static final String PROFILE_ID_REQUEST_URL = "https://scistarter.com/api/profile/id";
    private static final String PROFILE_ID_REQUEST_IDENTIFIER_PARAM = "identifier";
    private static final String SCISTARTER_REQUEST_API_TOKEN_PARAM = "key";
    private static final String SCISTARTER_API_AUTH_TOKEN = "97eeac98d396a1111c3de2fddefeddb45f356dab05e43e152914ce952206cec0a4e19b96b5ed47820c87f0ce7e94d1f34831a7d2e93bc95cb20adc08f151a26f";
    private static final String SIGNIN_POST_PROFILE_ID_KEY = "profile_id";
    private static final String SIGNIN_POST_PROJECT_ID_KEY = "project_id";
    private static final String SIGNIN_POST_TYPE_KEY = "type";
    private static final String SIGNIN_POST_WHEN_KEY = "when";
    private static final String SCISTARTER_PROJECT_ID = "21886";
    private static final String SIGNIN_POST_TYPE_VALUE = "signup";
    private static final String COMPLETE_POST_TYPE_VALUE = "collection";
    private static final String SCISTARTER_POST_URL = "https://scistarter.com/api/record_event";

    @NonNull
    static Response profileIDRequest(@NonNull String email) throws IOException {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(PROFILE_ID_REQUEST_URL).newBuilder();
        urlBuilder.addQueryParameter(PROFILE_ID_REQUEST_IDENTIFIER_PARAM, email);
        urlBuilder.addQueryParameter(SCISTARTER_REQUEST_API_TOKEN_PARAM, SCISTARTER_API_AUTH_TOKEN);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        return client.newCall(request).execute();
    }

    static void completeTestingPost(@NonNull String profileID) {
        postHelper(profileID, COMPLETE_POST_TYPE_VALUE);
    }

    static void signinPost(@NonNull String profileID) {
        postHelper(profileID, SIGNIN_POST_TYPE_VALUE);
    }

    private static void postHelper(@NonNull String profileID, @NonNull String type) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
        String formattedDate = sdf.format(new Date());

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(SIGNIN_POST_PROFILE_ID_KEY, profileID)
                .addFormDataPart(SIGNIN_POST_PROJECT_ID_KEY, SCISTARTER_PROJECT_ID)
                .addFormDataPart(SIGNIN_POST_TYPE_KEY, type)
                .addFormDataPart(SIGNIN_POST_WHEN_KEY, formattedDate)
                .build();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(SCISTARTER_POST_URL).newBuilder();
        urlBuilder.addQueryParameter(SCISTARTER_REQUEST_API_TOKEN_PARAM, SCISTARTER_API_AUTH_TOKEN);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }
}
