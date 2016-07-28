package com.android.socialnetworks.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.android.socialnetworks.fragments.SignUpFragment;
import com.android.socialnetworks.R;

public class SignUpActivity extends AppCompatActivity {

    public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";
    private static ProgressDialog pd;
    static Context context;
    public SignUpFragment myFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.sign_up_activity);
        context = this;
        myFragment = new SignUpFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, myFragment)
                    .commit();
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

    public static void showProgress(String message) {
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage(message);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    public static void hideProgress() {
        pd.dismiss();
    }

    public static boolean isNetworkOn(Context context) { ConnectivityManager connMgr =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static void startMainActivity(){
        context.startActivity(new Intent(context, MainActivity.class));
        ((Activity)context).finish();
    }

}
