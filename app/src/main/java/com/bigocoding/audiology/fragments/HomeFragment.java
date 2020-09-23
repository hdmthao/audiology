package com.bigocoding.audiology.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigocoding.audiology.R;
import com.bigocoding.audiology.VideoPlayActivity;
import com.bigocoding.audiology.adapters.VideoPostAdapter;
import com.bigocoding.audiology.models.YoutubeDataModel;
import com.bigocoding.interfaces.OnItemClickListener;
import com.loopj.android.http.HttpGet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class HomeFragment extends Fragment {
    private static String GOOGLE_YOUTUBE_API_KEY = "AIzaSyC9l_ocweobg9Xh6l58qtBBsqiwNEx6N3s";
    private static String HOME_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=20&regionCode=VN&type=video&key=" + GOOGLE_YOUTUBE_API_KEY + "&q=";

    private RecyclerView mListVideos = null;
    private VideoPostAdapter adapter = null;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();


    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String query) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("query", query);
        homeFragment.setArguments(args);
        return homeFragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListVideos = (RecyclerView) view.findViewById(R.id.listVideo);
        initList(mListData);

        Bundle args = getArguments();
        new RequestYoutubeAPI().execute(args != null ? args.getString("query") : "trending");
        return view;
    }

    private void initList(ArrayList<YoutubeDataModel> mListData) {
        mListVideos.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new VideoPostAdapter(getActivity(), mListData, new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
                intent.putExtra(YoutubeDataModel.class.toString(), item);
                startActivity(intent);
            }
        });
        mListVideos.setAdapter(adapter);
    }

    private class RequestYoutubeAPI extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = null;
            try {
                httpGet = new HttpGet(HOME_GET_URL +  URLEncoder.encode(params[0], StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.e("URL", HOME_GET_URL + params[0]);
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                return EntityUtils.toString(httpEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject.toString());
                    mListData = parseVideoListFromResponse(jsonObject);
                    initList(mListData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject) {
        ArrayList<YoutubeDataModel> mList = new ArrayList<>();

        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("kind")) {
                        if (json.getString("kind").equals("youtube#searchResult")) {
                            YoutubeDataModel youtubeObject = new YoutubeDataModel();
                            String id = json.getJSONObject("id").getString("videoId");
                            JSONObject jsonSnippet = json.getJSONObject("snippet");
                            String title = jsonSnippet.getString("title");
                            String description = jsonSnippet.getString("description");
                            String publishedAt = jsonSnippet.getString("publishedAt");
                            String thumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

                            youtubeObject.setTitle(title);
                            youtubeObject.setDescription(description);
                            youtubeObject.setPublishedAt(publishedAt);
                            youtubeObject.setThumbnail(thumbnail);
                            youtubeObject.setVideo_id(id);
                            mList.add(youtubeObject);
                        }
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }

}