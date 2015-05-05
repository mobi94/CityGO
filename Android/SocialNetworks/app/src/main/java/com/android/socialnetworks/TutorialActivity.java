package com.android.socialnetworks;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/*import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
*/
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

        /*SliderLayout sliderShow = (SliderLayout) findViewById(R.id.slider);
        LinkedHashMap<String,String> url_maps = new LinkedHashMap<>();
        url_maps.put("1", "http://fc01.deviantart.net/fs27/i/2008/075/9/1/BE_ABROAD_Poster_by_thedesolateone.jpg");
        url_maps.put("2", "http://fc04.deviantart.net/fs16/f/2007/149/b/9/New_TechSupport_poster_by_Crittz.jpg");
        url_maps.put("3", "http://www.rit.edu/imagine/posters/2009/2009-01.jpg");
        url_maps.put("4", "http://www.youthedesigner.com/wp-content/uploads/2011/05/concert-gig-poster-designs-20.jpg");

        for(String name : url_maps.keySet()){
            DefaultSliderView sliderView = new DefaultSliderView(this);
            // initialize a SliderLayout
            sliderView
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            sliderShow.addSlider(sliderView);
        }*/
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