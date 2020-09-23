package com.bigocoding.audiology;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BuildConfig;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

public class BiometricAssistant {
    private static final String TAG = BiometricAssistant.class.getSimpleName();
    private static final String API_KEY = "key_186c7a4a0d0f42e3a6adc06e61339d09";
    private static final String API_TOKEN = "tok_a4d34a9a28f44841bf083dfd6acbc2a0";
    private static AsyncHttpClient mClient;

    public BiometricAssistant(String apiKey, String apiToken) {
        mClient = new AsyncHttpClient();
        mClient.removeAllHeaders();
        mClient.setTimeout(30 * 1000);
        mClient.setBasicAuth(apiKey, apiToken);
        mClient.addHeader("platformId", "40");
        mClient.addHeader("platformVersion", BuildConfig.VERSION_NAME);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        String BASE_URL = "https://api.voiceit.io";
        return BASE_URL + relativeUrl;
    }

    public void deleteAllEnrollments(String userId, AsyncHttpResponseHandler responseHandler) {
        mClient.delete(getAbsoluteUrl("/enrollments/" + userId + "/all"), responseHandler);
    }

    public void getAllVoiceEnrollments(String userId, AsyncHttpResponseHandler responseHandler) {
        mClient.get(getAbsoluteUrl("/enrollments/voice/" + userId), responseHandler);
    }

    public void getAllFaceEnrollments(String userId, AsyncHttpResponseHandler responseHandler) {
        mClient.get(getAbsoluteUrl("/enrollments/face/" + userId), responseHandler);
    }

    public void voiceVerification(String userId, String contentLanguage, String phrase, File recording, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        params.put("contentLanguage", contentLanguage);
        params.put("phrase", phrase);
        try {
            params.put("recording", recording);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e.getMessage());
            responseHandler.sendFailureMessage(200, null, buildJSONFormatMessage().toString().getBytes(), new Throwable());
            return;
        }
        mClient.post(getAbsoluteUrl("/verification/voice"), params, responseHandler);
    }

    public void createVoiceEnrollment(String userId, String contentLanguage, String phrase, File recording, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        params.put("contentLanguage", contentLanguage);
        params.put("phrase", phrase);
        try {
            params.put("recording", recording);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e.getMessage());
            responseHandler.sendFailureMessage(200, null, buildJSONFormatMessage().toString().getBytes(), new Throwable());
            return;
        }

        mClient.post(getAbsoluteUrl("/enrollments/voice"), params, responseHandler);
    }

    public void createFaceEnrollmentWithPhoto(String userId, File photo, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        try {
            params.put("photo", photo);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e.getMessage());
            responseHandler.sendFailureMessage(200, null, buildJSONFormatMessage().toString().getBytes(), new Throwable());
            return;
        }

        mClient.post(getAbsoluteUrl("/enrollments/face"), params, responseHandler);
    }

    public void faceVerificationWithPhoto(String userId, File photo, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        try {
            params.put("photo", photo);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e.getMessage());
            responseHandler.sendFailureMessage(200, null, buildJSONFormatMessage().toString().getBytes(), new Throwable());
            return;
        }

        mClient.post(getAbsoluteUrl("/verification/face"), params, responseHandler);
    }
    public void encapsulatedVoiceEnrollment(Activity activity, String userId, String contentLanguage,
                                            String phrase, final JsonHttpResponseHandler responseHandler) {
        Intent intent = new Intent(activity, VoiceEnrollment.class);
        Bundle bundle = new Bundle();
        bundle.putString("apiKey", API_KEY);
        bundle.putString("apiToken", API_TOKEN);
        bundle.putString("userId", userId);
        bundle.putString("contentLanguage", contentLanguage);
        bundle.putString("phrase", phrase);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);

        broadcastMessageHandler(activity, responseHandler);
    }

    public void encapsulatedVoiceVerification(Activity activity, String userId, String contentLanguage, String phrase, final JsonHttpResponseHandler responseHandler) {
        Intent intent = new Intent(activity, VoiceVerification.class);
        Bundle bundle = new Bundle();
        bundle.putString("apiKey", API_KEY);
        bundle.putString("apiToken", API_TOKEN);
        bundle.putString("userId", userId);
        bundle.putString("contentLanguage", contentLanguage);
        bundle.putString("phrase", phrase);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);

        broadcastMessageHandler(activity, responseHandler);
    }

    public void encapsulatedFaceEnrollment(Activity activity, String userId,
                                           JsonHttpResponseHandler responseHandler) {

        Intent intent = new Intent(activity, FaceEnrollment.class);
        Bundle bundle = new Bundle();
        bundle.putString("apiKey", API_KEY);
        bundle.putString("apiToken", API_TOKEN);
        bundle.putString("userId", userId);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);

        broadcastMessageHandler(activity, responseHandler);

        requestWritePermission(activity);
    }

    public void encapsulatedFaceVerification(Activity activity, String userId, boolean doLivenessCheck, boolean doLivenessAudioCheck, int livenessChallengeFailsAllowed, int livenessChallengesNeeded, final JsonHttpResponseHandler responseHandler) {

        Intent intent = new Intent(activity, FaceVerification.class);
        Bundle bundle = new Bundle();
        bundle.putString("apiKey", API_KEY);
        bundle.putString("apiToken", API_TOKEN);
        bundle.putString("userId", userId);
        bundle.putBoolean("doLivenessCheck", doLivenessCheck);
        bundle.putBoolean("doLivenessAudioCheck", doLivenessAudioCheck);
        bundle.putInt("livenessChallengeFailsAllowed", livenessChallengeFailsAllowed);
        bundle.putInt("livenessChallengesNeeded", livenessChallengesNeeded);
        bundle.putBoolean("displayPreviewFrame", false);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);

        broadcastMessageHandler(activity, responseHandler);

        requestWritePermission(activity);
    }
    private void broadcastMessageHandler(final Activity activity, final JsonHttpResponseHandler responseHandler) {
        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            boolean broadcastTriggered = false;

            @Override
            public void onReceive(Context context, Intent intent) {
                if (!broadcastTriggered) {
                    broadcastTriggered = true;
                    String Response = intent.getStringExtra("Response");

                    if (Objects.equals(intent.getAction(), "assistant-success")) {
                        responseHandler.sendSuccessMessage(200, null, Response != null ? Response.getBytes() : new byte[0]);
                    }
                    if (Objects.equals(intent.getAction(), "assistant-failure")) {
                        responseHandler.sendFailureMessage(200, null, Response != null ? Response.getBytes() : new byte[0], new Throwable());
                    }
                }
            }
        };

        // Register observers (mMessageReceiver) to receive Intents with named actions
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("assistant-success");
        intentFilter.addAction("assistant-failure");
        LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageReceiver, intentFilter);
    }

    private void requestWritePermission(Activity activity) {
        if (!Settings.System.canWrite(activity)) {
            Toast.makeText(activity, activity.getString(R.string.GRANT_WRITE_PERMISSON), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        }
    }

    private JSONObject buildJSONFormatMessage() {
        JSONObject json = new JSONObject();
        try {
            json.put("message", "Incorrectly formatted id argument. Check log output for more information");
        } catch (JSONException e) {
            Log.e(TAG,"JSON Exception : " + e.getMessage());
        }
        return json;
    }


}
