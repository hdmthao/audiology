package com.bigocoding.audiology;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String userId = "usr_5abcb05344d141e398f1489b159d5ae5";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://eastus.api.cognitive.microsoft.com/face/v1.0/", "c3964bae02b64e34810a5ba1a2df4207");

    JSONObject emotionJsonObj;

    private void initComponent() {
        emotionJsonObj = new JSONObject();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponent();

        predictEmotion();
    }

    void predictEmotion() {
        Intent intent = this.getIntent();
        String face_url = intent.getStringExtra("url");
        Bitmap bitmap = Utils.rotateBitmap(BitmapFactory.decodeFile(face_url));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        @SuppressLint("StaticFieldLeak") AsyncTask<InputStream, String, Face[]> detectTask =

                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    // returnFaceAttributes:
                                    new FaceServiceClient.FaceAttributeType[] {
                                            FaceServiceClient.FaceAttributeType.Emotion,
                                            FaceServiceClient.FaceAttributeType.Gender }
                            );
                            for (int i=0;i<result.length;i++) {
                                emotionJsonObj.put("happiness" , result[i].faceAttributes.emotion.happiness);
                                emotionJsonObj.put("sadness" , result[i].faceAttributes.emotion.sadness);
                                emotionJsonObj.put("surprise" , result[i].faceAttributes.emotion.surprise);
                                emotionJsonObj.put("neutral"  , result[i].faceAttributes.emotion.neutral);
                                emotionJsonObj.put("anger" , result[i].faceAttributes.emotion.anger);
                                emotionJsonObj.put("contempt" , result[i].faceAttributes.emotion.contempt);
                                emotionJsonObj.put("disgust" , result[i].faceAttributes.emotion.disgust);
                                emotionJsonObj.put("fear" , result[i].faceAttributes.emotion.fear);
                                Log.e(TAG, "doInBackground: "+emotionJsonObj.toString()  );
                            }
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            Log.d(TAG, exceptionMessage);
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                    }

                    @Override
                    protected void onProgressUpdate(String... progress) {
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        Log.d(TAG, emotionJsonObj.toString());
                        Toast.makeText(MainActivity.this, emotionJsonObj.toString(), Toast.LENGTH_LONG).show();
                    }
                };

        detectTask.execute(inputStream);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
    }
}