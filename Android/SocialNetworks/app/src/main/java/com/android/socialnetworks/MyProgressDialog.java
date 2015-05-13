package com.android.socialnetworks;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressDialog {

    private static ProgressDialog pd;
    private boolean isShowing = false;
    Context context;

    MyProgressDialog(Context context){
        this.context = context;
    }

    protected void showProgress(String message) {
        if (!isShowing) {
            pd = new android.app.ProgressDialog(context);
            pd.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
            pd.setMessage(message);
            pd.setCancelable(false);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            isShowing = true;
        }
    }

    protected void hideProgress() {
        if (isShowing) {
            pd.dismiss();
            isShowing = false;
        }
    }
}
