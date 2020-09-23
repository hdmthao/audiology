package com.bigocoding.audiology.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bigocoding.audiology.R;
import com.bigocoding.audiology.models.YoutubeDataModel;
import com.bigocoding.interfaces.OnItemClickListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class VideoPostAdapter extends RecyclerView.Adapter<VideoPostAdapter.YoutubePostHolder> {
    private ArrayList<YoutubeDataModel> dataSet;
    private Context mContext = null;
    private final OnItemClickListener listener;

    public VideoPostAdapter(Context mContext, ArrayList<YoutubeDataModel> dataSet, OnItemClickListener listener) {
        this.dataSet = dataSet;
        this.mContext = mContext;
        this.listener = listener;
    }

    @NonNull
    @Override
    public YoutubePostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_post_layout,parent,false);
        YoutubePostHolder postHolder = new YoutubePostHolder(view);
        return postHolder;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onBindViewHolder(@NonNull YoutubePostHolder holder, int position) {
        TextView textViewTitle = holder.textViewTitle;
        TextView textViewDes = holder.textViewDes;
        TextView textViewDate = holder.textViewDate;
        ImageView imageThumb = holder.imageThumb;

        YoutubeDataModel object = dataSet.get(position);

        textViewTitle.setText(object.getTitle());
        textViewDes.setText(object.getDescription());
        textViewDate.setText(object.getPublishedAt());
        holder.bind(dataSet.get(position), listener);

        Picasso.with(mContext).load(object.getThumbnail()).into(imageThumb);
    }

    public static class YoutubePostHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDes;
        TextView textViewDate;
        ImageView imageThumb;

        public YoutubePostHolder(View itemView) {
            super(itemView);
            this.textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.textViewDes = (TextView) itemView.findViewById(R.id.textViewDes);
            this.textViewDate = (TextView) itemView.findViewById(R.id.textViewDate);
            this.imageThumb = (ImageView) itemView.findViewById(R.id.imageThumb);

        }

        public void bind(final YoutubeDataModel item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
