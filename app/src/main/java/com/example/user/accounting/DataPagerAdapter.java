package com.example.user.accounting;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DataPagerAdapter extends FragmentPagerAdapter {
    int num=3;

    public DataPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
       switch (i)
       {
           case 0:return new TodayFragment();
           case 1:return new MonthFragment();
           case 2:return new YearFragment();
           default:return null;
       }
    }

    @Override
    public int getCount() {
        return num;
    }
}
