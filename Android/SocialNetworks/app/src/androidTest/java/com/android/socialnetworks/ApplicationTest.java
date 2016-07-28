package com.android.socialnetworks;

import android.app.Application;
import android.support.v4.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ApplicationTestCase;
import android.view.View;

import com.android.socialnetworks.activities.MainActivity;
import com.android.socialnetworks.activities.SignUpActivity;
import com.android.socialnetworks.fragments.MainFragment;
import com.android.socialnetworks.fragments.SignUpFragment;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ActivityInstrumentationTestCase2<SignUpActivity> {
    private SignUpActivity mActivity;
    private MaterialEditText username;
    private SignUpFragment myFragment;

    public ApplicationTest() {
        super(SignUpActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mActivity = (SignUpActivity) getActivity();
        myFragment = mActivity.myFragment;

        username = (MaterialEditText) myFragment.getView().findViewById(R.id.username_edit);


    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCase1() throws Exception {
        assertEquals("mKmPerHourEditText не пустой", "", username.getText()
                .toString());

    }
}