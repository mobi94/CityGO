package com.android.socialnetworks;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.parse.Parse;
import com.parse.ParseUser;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }
        Parse.initialize(this, "fjgIsvCABdMM2QGwqlWBlwckK0uwBwJLg9Fx3sxX", "UIp2kIOJ5C7ZVs9VMb8922FhhZ8vVs5BE126CjA8");

        if (isFirstTime()) {
            startActivity(new Intent(this, TutorialActivity.class));
            finish();
        }
        else{
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null) {
                startActivity(new Intent(this, SignUpActivity.class));
                finish();
            }
            else{
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new MainFragment())
                        .commit();
            }
        }
    }

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean runBefore = preferences.getBoolean("RunBefore", false);
        if (!runBefore) {
            // first time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RunBefore", true);
            editor.apply();
        }
        return !runBefore;
    }
}
