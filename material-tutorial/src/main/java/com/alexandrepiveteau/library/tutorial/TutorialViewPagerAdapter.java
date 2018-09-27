package com.alexandrepiveteau.library.tutorial;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class TutorialViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragments;

    public TutorialViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void setFragments(List<Fragment> fragments) {
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
