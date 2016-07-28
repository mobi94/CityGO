package com.android.socialnetworks.application;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.android.socialnetworks.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBSettings;

public class StarterApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "OZYscvQi1cKHCP0vo6hPbbGAPvWs6M6vuMvrMRHi", "CTt2KMlNavNfboR5Tt0f8bA0I0h38ZgCFPtE6I5s");
        QBSettings.getInstance().fastConfigInit("21742", "OHjwDjYZG58QChy", "7-QuGgDaATdY8fR");
        QBChatService.setDebugEnabled(true);


    }
}