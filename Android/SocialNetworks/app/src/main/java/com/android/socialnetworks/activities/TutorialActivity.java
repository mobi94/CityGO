package com.android.socialnetworks.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.socialnetworks.R;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;

import java.util.LinkedHashMap;

public class TutorialActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.tutorial_activity);
        Button logoutButton = (Button) findViewById(R.id.skip_button);
        logoutButton.setOnClickListener(this);

        SliderLayout sliderShow = (SliderLayout) findViewById(R.id.slider);
        LinkedHashMap<String,Integer> url_maps = new LinkedHashMap<>();
        url_maps.put("1", R.drawable.tuto1);
        url_maps.put("2", R.drawable.tuto2);
        url_maps.put("3", R.drawable.tuto3);

        for(String name : url_maps.keySet()){
            DefaultSliderView sliderView = new DefaultSliderView(this);
            // initialize a SliderLayout
            sliderView
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            sliderShow.addSlider(sliderView);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.skip_button:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}