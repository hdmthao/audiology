package com.bigocoding.audiology;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bigocoding.audiology.voice_it.BiometricAssistant;
import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = SignupActivity.class.getSimpleName();

    private final String phrase = "Never forget tomorrow is a new day";
    private final String userId = "usr_5abcb05344d141e398f1489b159d5ae5";
    private final String userId2 = "usr_d603ab00414b414f90a1491fa07f7c93";
    private final String contentLanguage = "en-US";

    private BiometricAssistant mBiometricAssistant;
    boolean voiceEnrolled = false;
    boolean faceErolled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        OnclickButtonListener();
        mBiometricAssistant = new BiometricAssistant("key_186c7a4a0d0f42e3a6adc06e61339d09", "tok_a4d34a9a28f44841bf083dfd6acbc2a0");
    }

    private void OnclickButtonListener() {
        ImageView voice = (ImageView) findViewById(R.id.signup_voice);
        ImageView face = (ImageView) findViewById(R.id.signup_face);
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceEnroll();
            }
        });
        face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faceEnroll();
            }
        });
    }

    public void onClick_signupassword(View view) {
        if (!faceErolled || !voiceEnrolled) {
            if (!faceErolled && !voiceEnrolled) {
                Toast.makeText(SignupActivity.this, R.string.MUST_REGISTER_BIOMETRIC, Toast.LENGTH_LONG).show();
            } else if (!voiceEnrolled){
                Toast.makeText(SignupActivity.this, R.string.MUST_REGISTER_VOICE, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SignupActivity.this, R.string.MUST_REGISTER_FACE, Toast.LENGTH_LONG).show();
            }
            return;
        }
        Intent createpassword = new Intent (SignupActivity.this, CreatePassword.class);
        startActivity(createpassword) ;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    private void voiceEnroll() {
        mBiometricAssistant.encapsulatedVoiceEnrollment(this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "Voice Enrolled");
                voiceEnrolled = true;
                findViewById(R.id.signup_voice).setVisibility(View.GONE);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                checkResponse(errorResponse);
                Log.d(TAG, "Voice Failed");
                voiceEnrolled = false;
            }
        });
    }

    private void faceEnroll() {
        mBiometricAssistant.encapsulatedFaceEnrollment(this, userId2, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "Face enrolled");
                faceErolled = true;
                findViewById(R.id.signup_face).setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                checkResponse(errorResponse);
                Log.d(TAG, "Face enroll failed");
                faceErolled = false;
            }
        });
    }
    public void checkResponse(JSONObject response) {
        try {
            if (response.getString("responseCode").equals("IFVD")
                    || response.getString("responseCode").equals("ACLR")
                    || response.getString("responseCode").equals("IFAD")
                    || response.getString("responseCode").equals("SRNR")
                    || response.getString("responseCode").equals("UNFD")
                    || response.getString("responseCode").equals("MISP")
                    || response.getString("responseCode").equals("DAID")
                    || response.getString("responseCode").equals("UNAC")
                    || response.getString("responseCode").equals("CLNE")
                    || response.getString("responseCode").equals("INCP")
                    || response.getString("responseCode").equals("NPFC")) {
//                Toast.makeText(this, "responseCode: " + response.getString("responseCode")
//                        + ", " + getString(com.voiceit.voiceit2.R.string.CHECK_CODE), Toast.LENGTH_LONG).show();
                Log.e("MainActivity", "responseCode: " + response.getString("responseCode")
                        + ", " + getString(com.bigocoding.audiology.R.string.CHECK_CODE));
            }
        } catch (JSONException e) {
            Log.d("MainActivity", "JSON exception : " + e.toString());
        }
    }
}