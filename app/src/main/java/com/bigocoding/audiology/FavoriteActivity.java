package com.bigocoding.audiology;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {
    ArrayList<FavoriteComponent> favoriteComponentArrayList = new ArrayList<>();
    ArrayList<String> userInfo = new ArrayList<>() ;
    FavoriteComponent fav ;
    FavoriteComponent previous_fav ;
    int index;
    int count_favorite = 0 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        initComponent() ;
        runAnimation();
    }

    private void initComponent() {
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.ballad,"Ballad"));
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.rock, "Rock"));
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.hiphop, "Hip Hop"));
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.kpop, "K Pop Music")) ;
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.vietnammusic, "Việt Nam Music"));
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.usuk, "US-UK Music"));
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.sontung, "Sơn Tùng MTP"));
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.soobin, "Soobin Hoàng Sơn"));
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.amee, "Amee"));
        favoriteComponentArrayList.add(new FavoriteComponent(R.drawable.chipu, "Chi Pu"));


        fav = favoriteComponentArrayList.get(index);
    }

    public void FavoriteOnClick(View view) {
        SwipeNext(view);
        userInfo.add(previous_fav.name) ;
        Log.d("LOG", "LOG: " + previous_fav.name);
        count_favorite++;

        if (count_favorite >= 3)
            ((Button)findViewById(R.id.skipbutton)).setVisibility(View.VISIBLE);
    }
    public void SkipOnClick(View view) {
//        Send User Infomation
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


    }
    public void runAnimation(){
        ((TextView) findViewById(R.id.swipenext)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_to_left));
        ((ImageView) findViewById(R.id.favimg1)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_to_top));
        ((ImageView) findViewById(R.id.favimg)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_left_down));

        new CountDownTimer(800, 100) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                ((TextView) findViewById(R.id.swipenext)).setText(fav.name);
                ((ImageView) findViewById(R.id.favimg1)).setImageResource(fav.image);
                ((ImageView) findViewById(R.id.favimg)).setImageResource(fav.image);
                ((TextView) findViewById(R.id.swipenext)).startAnimation(AnimationUtils.loadAnimation(FavoriteActivity.this, R.anim.animation_right_to));
                ((ImageView) findViewById(R.id.favimg1)).startAnimation(AnimationUtils.loadAnimation(FavoriteActivity.this,R.anim.animation_top_to));
                ((ImageView) findViewById(R.id.favimg)).startAnimation(AnimationUtils.loadAnimation(FavoriteActivity.this,R.anim.animation_right_up));

            }
        }.start();


//        ((ImageView) findViewById(R.id.favimg1)).setImageResource(fav.image);
    }
    public void SwipeNext(View view) {
        index++ ;
        if (index == favoriteComponentArrayList.size())
            index = 0;
        fav = favoriteComponentArrayList.get(index);
        if (index != 0)
            previous_fav = favoriteComponentArrayList.get(index - 1);
        else
            previous_fav = favoriteComponentArrayList.get(favoriteComponentArrayList.size()-1);
        runAnimation();


    }
}