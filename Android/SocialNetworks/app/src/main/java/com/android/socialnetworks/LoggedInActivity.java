package com.android.socialnetworks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.parse.ParseUser;

public class LoggedInActivity extends Activity  implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged_in);

        Button logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.logout:
                ParseUser.logOut();
                Intent intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                finish();
                break;
        }
    }
}
