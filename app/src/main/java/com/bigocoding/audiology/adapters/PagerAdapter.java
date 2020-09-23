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

    public PagerAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        mNumOfTabs = numOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                HomeFragment home = new HomeFragment();
                return home;
            case 1:
                TrendingFragment trending = new TrendingFragment();
                return trending;
            case 2:
                HistoryFragment history = new HistoryFragment();
                return history;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
