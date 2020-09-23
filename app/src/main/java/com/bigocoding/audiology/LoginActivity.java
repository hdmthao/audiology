package com.bigocoding.audiology;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bigocoding.audiology.voice_it.BiometricAssistant;
import com.google.firebase.database.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String API_KEY = "key_186c7a4a0d0f42e3a6adc06e61339d09";
    private static final String API_TOKEN = "tok_a4d34a9a28f44841bf083dfd6acbc2a0";
    private final String phrase = "Never forget tomorrow is a new day";
    private final String userId = "usr_5abcb05344d141e398f1489b159d5ae5";
    private final String userId2 = "usr_d603ab00414b414f90a1491fa07f7c93";
    private final String contentLanguage = "en-US";
    private BiometricAssistant mBiometricAssistant;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {
            startSignUp();
        }



        setContentView(R.layout.login_layout);
        OnclickButtonListener();

        mBiometricAssistant = new BiometricAssistant(API_KEY, API_TOKEN);
        mDatabaseRef =
    }

    private void startSignUp() {
        //show start activity
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false)
                .apply();
    }


    public void OnclickButtonListener() {

        ImageView voice = (ImageView) findViewById(R.id.imgview_voice);
        ImageView face = (ImageView) findViewById(R.id.imgview_face);

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBiometricAssistant.encapsulatedVoiceVerification(LoginActivity.this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        goToGallery(null);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(LoginActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBiometricAssistant.encapsulatedFaceVerification(LoginActivity.this, userId2, false, false, 0, 2, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            goToGallery(response.getString("image_url"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(LoginActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void signin_button(View view) {
        EditText login_password = (EditText) findViewById(R.id.et_password);
        Log.d("MyString", login_password.getText().toString());
        if (login_password.getText().toString().equals("122333")) {
            goToGallery(null);
        } else
            Toast.makeText(LoginActivity.this, "Fail", Toast.LENGTH_SHORT).show();
    }

    void goToGallery(String image_url) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("url", image_url);

        startActivity(intent);
    }
}