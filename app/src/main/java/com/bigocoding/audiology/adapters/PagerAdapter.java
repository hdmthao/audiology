package com.bigocoding.audiology.adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.bigocoding.audiology.fragments.HistoryFragment;
import com.bigocoding.audiology.fragments.HomeFragment;
import com.bigocoding.audiology.fragments.TrendingFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    TrendingFragment trendingFragment;
    HistoryFragment historyFragment;
    String mQuery;

    public PagerAdapter(FragmentManager fm, int numOfTabs, String query) {
        super(fm);
        mNumOfTabs = numOfTabs;
        mQuery = query;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                HomeFragment home = HomeFragment.newInstance(mQuery);
                return home;
            case 1:
                TrendingFragment trendingFragment = new TrendingFragment();
                return trendingFragment;
            case 2:
                HistoryFragment historyFragment = new HistoryFragment();
                return historyFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
