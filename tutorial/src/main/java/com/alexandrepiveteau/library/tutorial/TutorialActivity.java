package com.alexandrepiveteau.library.tutorial;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alexandrepiveteau.library.tutorial.widgets.PageIndicator;

import java.util.ArrayList;
import java.util.List;


public abstract class TutorialActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, ViewPager.OnPageChangeListener {

    public abstract String getDoneText();
    public abstract String getIgnoreText();
    public abstract String getNextText();
    public abstract String getPreviousText();

    public abstract int getCount();
    public abstract int getBackgroundColor(int position);
    public abstract int getNavigationBarColor(int position);
    public abstract int getStatusBarColor(int position);
    public abstract Fragment getTutorialFragmentFor(int position);
    public abstract boolean isNavigationBarColored();
    public abstract boolean isStatusBarColored();

    public abstract ViewPager.PageTransformer getPageTransformer();

    //Views used
    private Button mButtonLeft;
    private Button mButtonRight;
    private ImageButton mImageButtonLeft;
    private ImageButton mImageButtonRight;
    private PageIndicator mPageIndicator;
    private RelativeLayout mRelativeLayout;
    private ViewPager mViewPager;

    //Objects needed
    private TutorialViewPagerAdapter mAdapter;
    private ColorMixer mColorMixerBackground;
    private ColorMixer mColorMixerNavigationBar;
    private ColorMixer mColorMixerStatusBas;

    private int mPreviousPage; //Needed if we want to animate the custom actions

    private List<Fragment> mFragmentList;

    private void setupFragmentList() {
        List<Fragment> fragments = new ArrayList<Fragment>();
        for(int i = 0; i < getCount(); i++) {
            fragments.add(getTutorialFragmentFor(i));
        }
        mFragmentList = fragments;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tutorial_button_left || v.getId() == R.id.tutorial_button_image_left) {
            boolean hasCustomAction = false;

            if(mFragmentList.get(mViewPager.getCurrentItem()) instanceof CustomAction) {
                if(((CustomAction)mFragmentList.get(mViewPager.getCurrentItem())).hasCustomAction()) {
                    hasCustomAction = true;
                }
            }
            if(hasCustomAction) {
                Uri uri = ((CustomAction)mFragmentList.get(mViewPager.getCurrentItem())).getCustomActionUri();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivity(intent);
            } else if(mViewPager.getCurrentItem() == 0) {
                finish();
            } else {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem()-1, true);
            }
        } else if (v.getId() == R.id.tutorial_button_right || v.getId() == R.id.tutorial_button_image_right) {
            if(mViewPager.getCurrentItem() == getCount()-1) {
                finish();
            } else {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1, true);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        mButtonLeft = (Button) findViewById(R.id.tutorial_button_left);
        mButtonRight = (Button) findViewById(R.id.tutorial_button_right);
        mImageButtonLeft = (ImageButton) findViewById(R.id.tutorial_button_image_left);
        mImageButtonRight = (ImageButton) findViewById(R.id.tutorial_button_image_right);
        mPageIndicator = (PageIndicator) findViewById(R.id.tutorial_page_indicator);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mButtonLeft.setOnClickListener(this);
        mButtonRight.setOnClickListener(this);
        mImageButtonLeft.setOnClickListener(this);
        mImageButtonRight.setOnClickListener(this);
        mImageButtonLeft.setOnLongClickListener(this);
        mImageButtonRight.setOnLongClickListener(this);

        setupFragmentList();

        mAdapter = new TutorialViewPagerAdapter(getSupportFragmentManager());

        mAdapter.setFragments(mFragmentList);

        mRelativeLayout.setBackgroundColor(Color.BLUE);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setPageTransformer(false, getPageTransformer());

        mPageIndicator.setViewPager(mViewPager);

        //We use this to actualize the Strings
        mPreviousPage = 0;
        onPageSelected(0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mColorMixerBackground = new ColorMixer();
        mColorMixerNavigationBar = new ColorMixer();
        mColorMixerStatusBas = new ColorMixer();

        mColorMixerBackground.setFirstColor(getBackgroundColor(position));
        try {
            mColorMixerBackground.setSecondColor(getBackgroundColor(position + 1));
        } catch (Exception e) {
            mColorMixerBackground.setSecondColor(getBackgroundColor(position));
        }

        mColorMixerNavigationBar.setFirstColor(getNavigationBarColor(position));
        try {
            mColorMixerNavigationBar.setSecondColor(getNavigationBarColor(position + 1));
        } catch (Exception e) {
            mColorMixerNavigationBar.setFirstColor(getNavigationBarColor(position));
        }

        mColorMixerStatusBas.setFirstColor(getStatusBarColor(position));
        try {
            mColorMixerStatusBas.setSecondColor(getStatusBarColor(position + 1));
        } catch (Exception e) {
            mColorMixerStatusBas.setFirstColor(getStatusBarColor(position));
        }

        setBackgroundColor(mColorMixerBackground.getMixedColor(positionOffset));
        setSystemBarsColors(mColorMixerNavigationBar.getMixedColor(positionOffset), mColorMixerStatusBas.getMixedColor(positionOffset));
    }

    @Override
    public void onPageSelected(int position) {

        if(mViewPager.getCurrentItem() == 0) {
            mButtonLeft.setText(getIgnoreText());

            animateViewFadeIn(mButtonLeft);
            animateViewFadeOut(mImageButtonLeft);

            animateViewFadeOut(mButtonRight);
            animateViewFadeIn(mImageButtonRight);

        } else if (mViewPager.getCurrentItem() == getCount()-1) {
            mButtonRight.setText(getDoneText());

            animateViewFadeOut(mButtonLeft);
            animateViewFadeIn(mButtonRight);
            animateViewFadeIn(mImageButtonLeft);
            animateViewFadeOut(mImageButtonRight);
        } else {
            animateViewFadeOut(mButtonLeft);
            animateViewFadeOut(mButtonRight);
            animateViewFadeIn(mImageButtonLeft);
            animateViewFadeIn(mImageButtonRight);
        }

        boolean hadPreviousPageCustomIcon = false;
        boolean hasCustomIcon = false;

        int previousPageIcon;
        final int currentPageIcon;

        if(mFragmentList.get(mPreviousPage) instanceof CustomAction) {
            hadPreviousPageCustomIcon = ((CustomAction)mFragmentList.get(mPreviousPage)).hasCustomAction();
        }

        if(hadPreviousPageCustomIcon) {
            previousPageIcon = ((CustomAction)mFragmentList.get(mPreviousPage)).getCustomActionIcon();
        } else {
            previousPageIcon = R.drawable.ic_action_tutorial_previous;
        }

        if(mFragmentList.get(position) instanceof CustomAction) {
            hasCustomIcon = ((CustomAction)mFragmentList.get(position)).hasCustomAction();
        }

        if(hasCustomIcon) {
            currentPageIcon = ((CustomAction)mFragmentList.get(position)).getCustomActionIcon();
        } else {
            currentPageIcon = R.drawable.ic_action_tutorial_previous;
        }

        if(currentPageIcon != previousPageIcon) {
            mImageButtonLeft.animate()
                    .alpha(0)
                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mImageButtonLeft.setImageResource(currentPageIcon);
                            mImageButtonLeft.animate()
                                    .alpha(1f)
                                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                                    .setListener(null)//We clear all listeners
                                    .start();
                        }
                    })
                    .start();
        }

        mPreviousPage = position;
    }

    private void animateViewFadeIn(final View view) {
        view.animate()
                .alpha(1f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        view.setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    private void animateViewFadeOut(final View view) {
        view.animate()
                .alpha(0f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void setBackgroundColor(int backgroundColor) {
        mRelativeLayout.setBackgroundColor(backgroundColor);
    }

    private void setSystemBarsColors(int colorNavigationBar, int colorStatusBar) {
        // Tinted status bar and navigation bars are available only on Lollipop, sadly :(
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(isNavigationBarColored()) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setNavigationBarColor(colorNavigationBar);
            }
            if(isStatusBarColored()) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(colorStatusBar);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == R.id.tutorial_button_image_left) {
            //Toast.makeText(this, getPreviousText(), Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.tutorial_button_image_right) {
            //Toast.makeText(this, getNextText(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}