package com.bigocoding.audiology;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SearchEvent;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://eastus.api.cognitive.microsoft.com/face/v1.0/", "c3964bae02b64e34810a5ba1a2df4207");

    private TabLayout tabLayout = null;
    private ViewPager viewPager = null;
    private JSONObject emotionJsonObj;

    private void initComponent() {
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Trending"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));
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
        if (face_url == null) {
            try {
                emotionJsonObj.put("happiness" , 1.0);
                emotionJsonObj.put("sadness" , 0.0);
                emotionJsonObj.put("surprise" , 0.0);
                emotionJsonObj.put("neutral"  , 0.0);
                emotionJsonObj.put("anger" , 0.0);
                emotionJsonObj.put("contempt" , 0.0);
                emotionJsonObj.put("disgust" , 0.0);
                emotionJsonObj.put("fear" , 0.0);
                populateListVideo("Son tung mtp");
                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Bitmap bitmap = rotateBitmap(BitmapFactory.decodeFile(face_url));

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
                        populateListVideo("Hay trao cho anh");
                    }
                };

        detectTask.execute(inputStream);
    }

    void populateListVideo(String query) {
        final PagerAdapter adapter = new com.bigocoding.audiology.adapters.PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), query);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewPager.setCurrentItem(0);
                populateListVideo(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
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

    Bitmap rotateBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(), bitmap.getHeight(), true);

        return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
    }
}