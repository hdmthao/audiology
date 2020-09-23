package com.bigocoding.audiology;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;

import org.json.JSONException;
import org.json.JSONObject;

public class FaceVerification extends AppCompatActivity implements SensorEventListener {

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private final File mPictureFile = Utils.getOutputMediaFile(".jpeg");
    private final Handler timingHandler = new Handler();

    private final String mTAG = "FaceVerificationView";
    private Context mContext;

    private RadiusOverlayView mOverlay;

    private boolean playInstructionalVideo;

    private BiometricAssistant mBiometricAssistant;
    private String mUserId = "";
    private boolean mDoLivenessCheck = false;
    private boolean mDoLivenessAudioCheck = false;
    private int mLivenessChallengeFailsAllowed;
    private int mLivenessChallengesNeeded;

    private final int mNeededEnrollments = 0;
    private int mFailedAttempts = 0;
    private final int mMaxFailedAttempts = 3;
    private boolean mContinueVerifying = false;

    private SensorManager sensorManager = null;
    private Sensor lightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        // Grab data from parent activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBiometricAssistant = new BiometricAssistant(bundle.getString("apiKey"), bundle.getString("apiToken"));
            mUserId = bundle.getString("userId");
            mDoLivenessCheck = bundle.getBoolean("doLivenessCheck");
            mDoLivenessAudioCheck = bundle.getBoolean("doLivenessAudioCheck");
            mLivenessChallengeFailsAllowed = bundle.getInt("livenessChallengeFailsAllowed");
            mLivenessChallengesNeeded = bundle.getInt("livenessChallengesNeeded");
            CameraSource.displayPreviewFrame = bundle.getBoolean("displayPreviewFrame");
        }

        // Hide action bar
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.d(mTAG, "Cannot hide action bar");
        }

        // Set screen brightness to full
        if(!Utils.setBrightness(this, 255)){
            exitViewWithMessage("assistant-failure","Hardware Permissions not granted");
        }

        // Set context
        mContext = this;
        // Set content view
        setContentView(R.layout.activity_face_verification);
        mPreview = findViewById(R.id.camera_preview);

        // Text output on mOverlay
        mOverlay = findViewById(R.id.overlay);
        CameraSource.mOverlay = mOverlay;

        // Lock orientation

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);


        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT)) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();

    }

    private void startVerificationFlow() {
        mContinueVerifying = true;
        // Try to setup camera source
        mCameraSource = Utils.createCameraSource(this, new FaceTrackerFactory(this));
        // Try to start camera
        if(!Utils.startCameraSource(this, mCameraSource, mPreview)){
            exitViewWithMessage("assistant-failure","Error starting camera");
        } else {
            mBiometricAssistant.getAllFaceEnrollments(mUserId, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, final JSONObject Response) {
                    try {
                        // Check If enough enrollments, otherwise return to previous activity
                        if (Response.getInt("count") < mNeededEnrollments) {
                            mOverlay.updateDisplayText(getString(R.string.NOT_ENOUGH_ENROLLMENTS));
                            // Wait for ~2.5 seconds
                            timingHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    exitViewWithMessage("assistant-failure", "Not enough enrollments");
                                }
                            }, 2500);
                        } else {
                            mOverlay.updateDisplayText(getString(R.string.LOOK_INTO_CAM));
                            // Start tracking faces
                            FaceTracker.continueDetecting = true;
                        }
                    } catch (JSONException e) {
                        Log.d(mTAG, "JSON exception : " + e.toString());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, final JSONObject errorResponse) {
                    if (errorResponse != null) {
                        try {
                            // Report error to user
                            mOverlay.updateDisplayText(getString((getResources().getIdentifier(errorResponse.
                                    getString("responseCode"), "string", getPackageName()))));
                        } catch (JSONException e) {
                            Log.d(mTAG, "JSON exception : " + e.toString());
                        }
                        // Wait for 2.0 seconds
                        timingHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                exitViewWithJSON("assistant-failure", errorResponse);
                            }
                        }, 2000);
                    } else {
                        Log.e(mTAG, "No response from server");
                        mOverlay.updateDisplayTextAndLock(getString(R.string.CHECK_INTERNET));
                        // Wait for 2.0 seconds
                        timingHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                exitViewWithMessage("assistant-failure", "No response from server");
                            }
                        }, 2000);
                    }
                }
            });
        }
    }

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class FaceTrackerFactory implements MultiProcessor.Factory<Face> {

        private final Activity mActivity;
        private final int [] livenessChallengeOrder = {1, 2, 3};

        private FaceTrackerFactory(FaceVerification activity) {
            mActivity = activity;
            FaceTracker.continueDetecting = false;
            FaceTracker.livenessChallengesPassed = 0;
            FaceTracker.livenessChallengeFails = 0;
            Utils.randomizeArrayOrder(livenessChallengeOrder);
        }

        @Override
        public Tracker<Face> create(Face face) {
            return new FaceTracker(mOverlay, mActivity, new FaceTrackerCallBackImpl(), livenessChallengeOrder, mDoLivenessCheck, mDoLivenessAudioCheck, mLivenessChallengeFailsAllowed, mLivenessChallengesNeeded);
        }
    }

    private void requestHardwarePermissions() {
        final int PERMISSIONS_REQUEST_CAMERA = 1;
        final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 2;
        // MY_PERMISSIONS_REQUEST_* is an app-defined int constant. The callback method gets the
        // result of the request.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ Manifest.permission.CAMERA},
                    ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions granted, so continue with view
            startVerificationFlow();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(mTAG,"Hardware Permissions not granted");
            exitViewWithMessage("assistant-failure","User Canceled");
        } else {
            // Permissions granted, so continue with view
            startVerificationFlow();
        }
    }

    private void exitViewWithMessage(String action, String message) {
        Utils.setBrightness(this, Utils.oldBrightness);
        mContinueVerifying = false;
        timingHandler.removeCallbacksAndMessages(null);
        FaceTracker.livenessTimer.removeCallbacksAndMessages(null);
        Intent intent = new Intent(action);
        JSONObject json = new JSONObject();
        try {
            json.put("message", message);
        } catch(JSONException e) {
            Log.d(mTAG,"JSON Exception : " + e.getMessage());
        }
        intent.putExtra("Response", json.toString());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void exitViewWithJSON(String action, JSONObject json) {
        Utils.setBrightness(this, Utils.oldBrightness);
        mContinueVerifying = false;
        timingHandler.removeCallbacksAndMessages(null);
        FaceTracker.livenessTimer.removeCallbacksAndMessages(null);
        Intent intent = new Intent(action);
        intent.putExtra("Response", json.toString());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float lux = event.values[0];
        if(lux < Utils.luxThreshold) {
            mOverlay.setLowLightMode(true);
        } else {
            mOverlay.setLowLightMode(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Confirm permissions and start enrollment flow
        requestHardwarePermissions();
    }

    @Override
    public void onBackPressed() {
        exitViewWithMessage("assistant-failure","User Canceled");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!playInstructionalVideo || !mDoLivenessCheck) {
            // Confirm permissions and start enrollment flow
            requestHardwarePermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        if(sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if(mContinueVerifying) {
            exitViewWithMessage("assistant-failure", "User Canceled");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private void takePicture() {

        // Verify after taking picture
        final CameraSource.PictureCallback mPictureCallback = new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data) {
                // Check file
                if (mPictureFile == null) {
                    Log.d(mTAG, "Error creating media file, check storage permissions");
                    return;
                }
                // Write picture to file
                try {
                    FileOutputStream fos = new FileOutputStream(mPictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(mTAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(mTAG, "Error accessing file: " + e.getMessage());
                }

                // With liveness checks enabled, a picture is taken before it is done
                // and verify is called later
                if(!mDoLivenessCheck) {
                    verifyUserFace();
                } else {
                    // Continue liveness detection
                    FaceTracker.continueDetecting = true;
                }
            }
        };

        try {
            // Take picture of face
            mCameraSource.takePicture(null, mPictureCallback);
        } catch (Exception e) {
            Log.e(mTAG, "Camera exception : " + e.getMessage());
            exitViewWithMessage("assistant-failure","Camera Error");
        }
    }

    private void failVerification(final JSONObject response) {

        // Continue showing live camera preview
        mOverlay.setPicture(null);

        mOverlay.setProgressCircleColor(getResources().getColor(R.color.failure));
        mOverlay.updateDisplayText(getString(R.string.VERIFY_FAIL));
        // Wait for ~1.5 seconds
        timingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // Report error to user
                    mOverlay.updateDisplayText(getString((getResources().getIdentifier(response.
                            getString("responseCode"), "string", getPackageName()))));
                } catch (JSONException e) {
                    Log.d(mTAG,"JSON exception : " + e.toString());
                }
                // Wait for ~4.5 seconds
                timingHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFailedAttempts++;

                        // User failed too many times
                        if (mFailedAttempts >= mMaxFailedAttempts) {
                            mOverlay.updateDisplayText(getString(R.string.TOO_MANY_ATTEMPTS));
                            // Wait for ~2 seconds
                            timingHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    exitViewWithJSON("assistant-failure", response);
                                }
                            }, 2000);
                        } else if (mContinueVerifying) {
                            if(FaceTracker.lookingAway) {
                                mOverlay.updateDisplayText(getString(R.string.LOOK_INTO_CAM));
                            }
                            // Reset liveness check and try again
                            FaceTracker.livenessChallengesPassed = 0;
                            FaceTracker.livenessChallengeFails = 0;
                            FaceTracker.continueDetecting = true;
                        }
                    }
                }, 4500);
            }
        }, 1500);
    }

    private  void verifyUserFace() {
        mOverlay.setProgressCircleColor(getResources().getColor(R.color.progressCircle));
        mOverlay.setProgressCircleAngle(270, 359);

        mBiometricAssistant.faceVerificationWithPhoto(mUserId, mPictureFile, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                try {
                    // If successful verification
                    if (response.getString("responseCode").equals("SUCC")) {
                        FaceTracker.continueDetecting = false;

                        mOverlay.setProgressCircleColor(getResources().getColor(R.color.success));
                        mOverlay.updateDisplayText(getString(R.string.VERIFY_SUCCESS));

                        // Wait for ~2 seconds
                        timingHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    response.put("image_url", mPictureFile.getPath());
                                    Log.d("debug2", response.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mPictureFile.deleteOnExit();
                                exitViewWithJSON("assistant-success", response);
                            }
                        }, 2000);
                    } else {
                        // If fail
                        mPictureFile.deleteOnExit();
                        failVerification(response);
                    }
                } catch (JSONException e) {
                    Log.d(mTAG, "JSON exception : " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, final JSONObject errorResponse) {
                if (errorResponse != null) {
                    mPictureFile.deleteOnExit();
                    failVerification(errorResponse);
                } else {
                    Log.e(mTAG, "No response from server");
                    mOverlay.updateDisplayTextAndLock(getString(R.string.CHECK_INTERNET));
                    // Wait for 2.0 seconds
                    timingHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exitViewWithMessage("assistant-failure", "No response from server");
                        }
                    }, 2000);
                }
            }
        });
    }

    class FaceTrackerCallBackImpl implements FaceTracker.viewCallBacks { // Implements callback methods defined in FaceTracker interface
        public void authMethodToCallBack() { verifyUserFace(); }
        public void takePictureCallBack() { takePicture(); }
    }

}