package com.bigocoding.audiology.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigocoding.audiology.R;
import com.bigocoding.audiology.VideoPlayActivity;
import com.bigocoding.audiology.adapters.VideoPostAdapter;
import com.bigocoding.audiology.models.YoutubeDataModel;
import com.bigocoding.interfaces.OnItemClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class HistoryFragment extends Fragment {
    private static final String userId = "usr_5abcb05344d141e398f1489b159d5ae5";

    DatabaseReference mDatabaseRef;
    private RecyclerView mListVideos = null;
    private VideoPostAdapter adapter = null;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        mListVideos = (RecyclerView) view.findViewById(R.id.listVideo);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference( userId+ "/history");
        initList(mListData);

        return  view;
    }

    private void initList(final ArrayList<YoutubeDataModel> mListData) {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<YoutubeDataModel> listData = new ArrayList<>();
                HashSet<String> idLists = new HashSet<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    YoutubeDataModel youtubeDataModel = data.getValue(YoutubeDataModel.class);
                    listData.add(youtubeDataModel);
                }
                mListData.clear();
                Collections.reverse(listData);
                for (YoutubeDataModel data : listData) {
                    if (data == null || idLists.contains(data.getVideo_id())) continue;
                    mListData.add(data);
                    idLists.add(data.getVideo_id());
                }
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}