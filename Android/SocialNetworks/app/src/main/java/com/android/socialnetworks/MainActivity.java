package com.android.socialnetworks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Window;

import com.parse.Parse;
import com.parse.ParseUser;

public class MainActivity extends ActionBarActivity{

    public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";
    private static ProgressDialog pd;
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        Parse.initialize(this, "fjgIsvCABdMM2QGwqlWBlwckK0uwBwJLg9Fx3sxX", "UIp2kIOJ5C7ZVs9VMb8922FhhZ8vVs5BE126CjA8");

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            Intent intent = new Intent(this, LoggedInActivity.class);
            this.startActivity(intent);
            finish();
        } else {
            // show the signup or login screen
            setContentView(R.layout.activity_main);
            context = this;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new MainFragment())
                        .commit();
            }
        }
    }

    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Fragment mainFragment = getSupportFragmentManager().findFragmentByTag(SOCIAL_NETWORK_TAG);
            if (mainFragment != null) {
                mainFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    protected static void showProgress(String message) {
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage(message);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    protected static void hideProgress() {
        pd.dismiss();
    }

    protected static boolean isNetworkOn(Context context) { ConnectivityManager connMgr =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    protected static void startLoggedInActivity(){
        Intent intent = new Intent(context, LoggedInActivity.class);
        context.startActivity(intent);
        ((Activity)context).finish();
    }
}
