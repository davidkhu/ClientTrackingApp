package com.id11013962.clienttrackingapp.View;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntroFragment;
import com.id11013962.clienttrackingapp.R;

/**
 * Implemented a public App Intro service. Just for the introduction screen.
 * Copyright of: Paolo Rotolo
 * refer to: https://github.com/PaoloRotolo/AppIntro for more details.
 */
public class AppIntro extends com.github.paolorotolo.appintro.AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance("Welcome!", "", R.drawable.post_office, Color.parseColor("#ffd62a")));
        addSlide(AppIntroFragment.newInstance("Track your parcel", "", R.drawable.parcel, Color.parseColor("#ff8d8d")));
        addSlide(AppIntroFragment.newInstance("Find its exact location", "", R.drawable.maps_icon, Color.parseColor("#00BCD4")));

        // Hide Skip/Done button.
        showStatusBar(true);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

}
