package com.android.socialnetworks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTimePicker extends DialogFragment {
    public static final String TAG_FRAG_DATE_TIME = "fragDateTime";
    private static final String KEY_DIALOG_TITLE = "dialogTitle";
    private static final String KEY_INIT_DATE = "initDate";
    private static final String TAG_DATE = "date";
    private static final String TAG_TIME = "time";
    private Context mContext;
    private ButtonClickListener mButtonClickListener;
    private OnDateTimeSetListener mOnDateTimeSetListener;
    private Bundle mArgument;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    // DialogFragment constructor must be empty
    public DateTimePicker() {
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mButtonClickListener = new ButtonClickListener();
    }
    /**
     *
     * @param dialogTitle Title of the DateTimePicker DialogFragment
     * @param initDate Initial Date and Time set to the Date and Time Picker
     * @return Instance of the DateTimePicker DialogFragment
     */
    public static DateTimePicker newInstance(CharSequence dialogTitle, Date initDate) {
        // Create a new instance of DateTimePicker
        DateTimePicker mDateTimePicker = new DateTimePicker();
        // Setup the constructor parameters as arguments
        Bundle mBundle = new Bundle();
        mBundle.putCharSequence(KEY_DIALOG_TITLE, dialogTitle);
        mBundle.putSerializable(KEY_INIT_DATE, initDate);
        mDateTimePicker.setArguments(mBundle);
        // Return instance with arguments
        return mDateTimePicker;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Retrieve Argument passed to the constructor
        mArgument = getArguments();
        // Use an AlertDialog Builder to initially create the Dialog
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
        // Setup the Dialog
        mBuilder.setTitle(mArgument.getCharSequence(KEY_DIALOG_TITLE));
        mBuilder.setNegativeButton(android.R.string.no,mButtonClickListener);
        mBuilder.setPositiveButton(android.R.string.yes,mButtonClickListener);
        // Create the Alert Dialog
        AlertDialog mDialog = mBuilder.create();
        mDialog.setCanceledOnTouchOutside(false);
        // Set the View to the Dialog
        mDialog.setView(
                createDateTimeView(mDialog.getLayoutInflater())
        );
        // Return the Dialog created
        return mDialog;
    }
    /**
     * Inflates the XML Layout and setups the tabs
     * @param layoutInflater Layout inflater from the Dialog
     * @return Returns a view that will be set to the Dialog
     */
    private View createDateTimeView(LayoutInflater layoutInflater) {
        // Inflate the XML Layout using the inflater from the created Dialog
        View mView = layoutInflater.inflate(R.layout.date_time_picker,null);
        // Extract the TabHost
        TabHost mTabHost = (TabHost) mView.findViewById(R.id.tab_host);
        mTabHost.setup();
        // Create Date Tab and add to TabHost
        TabHost.TabSpec mDateTab = mTabHost.newTabSpec(TAG_DATE);
        mDateTab.setIndicator("Date");
        mDateTab.setContent(R.id.date_content);
        mTabHost.addTab(mDateTab);
        // Create Time Tab and add to TabHost
        final TabHost.TabSpec mTimeTab = mTabHost.newTabSpec(TAG_TIME);
        mTimeTab.setIndicator("Time");
        mTimeTab.setContent(R.id.time_content);
        mTabHost.addTab(mTimeTab);
        // Retrieve Date from Arguments sent to the Dialog
        final DateTime mDateTime = new DateTime((Date) mArgument.getSerializable(KEY_INIT_DATE));

        // Initialize Date and Time Pickers
        mDatePicker = (DatePicker) mView.findViewById(R.id.date_picker);
        mTimePicker = (TimePicker) mView.findViewById(R.id.time_picker);
        mDatePicker.init(mDateTime.getYear(), mDateTime.getMonthOfYear(),
                mDateTime.getDayOfMonth(), null);
        mTimePicker.setIs24HourView(true);
        setCorrectedTime();

        long addDaysInMillis = 0;
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if((currentMinute + 15) >= 60 && currentHour == 23) addDaysInMillis = TimeUnit.DAYS.toMillis(1);

        mDatePicker.setMinDate(System.currentTimeMillis() - 1000 + addDaysInMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.set(mDateTime.getYear() + 1, mDateTime.getMonthOfYear(), mDateTime.getDayOfMonth());
        mDatePicker.setMaxDate(calendar.getTimeInMillis());

        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                updateTime();
            }
        });
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals(mTimeTab.getTag()))
                    updateTime();
            }
        });
        // Return created view
        return mView;
    }

    private void updateTime(){
        Calendar minTime = Calendar.getInstance();
        minTime.setTimeInMillis(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60 + Calendar.getInstance().get(Calendar.MINUTE) + 15);
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(mTimePicker.getCurrentMinute() + mTimePicker.getCurrentHour()*60);
        Calendar minDate = Calendar.getInstance();
        minDate.setTimeInMillis(Calendar.getInstance().get(Calendar.YEAR) +
                Calendar.getInstance().get(Calendar.MONTH) + Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTimeInMillis(mDatePicker.getYear() + mDatePicker.getMonth() + mDatePicker.getDayOfMonth());
        if (currentTime.getTimeInMillis() < minTime.getTimeInMillis()
                && (currentDate.getTimeInMillis() == minDate.getTimeInMillis())) {
            setCorrectedTime();
        }
    }

    private void setCorrectedTime(){
        int addMinutes = 0;
        int addHour = 0;
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if((currentMinute + 15) >= 60) {
            addMinutes = (currentMinute + 15) - 60;
            addHour = 1;
        }
        else addMinutes = currentMinute + 15;
        mTimePicker.setCurrentMinute(addMinutes);
        mTimePicker.setCurrentHour(currentHour + addHour);
    }

    /**
     * Sets the OnDateTimeSetListener interface
     * @param onDateTimeSetListener Interface that is used to send the Date and Time
     *               to the calling object
     */
    public void setOnDateTimeSetListener(OnDateTimeSetListener onDateTimeSetListener) {
        mOnDateTimeSetListener = onDateTimeSetListener;
    }
    private class ButtonClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int result) {
            // Determine if the user selected Ok
            if(DialogInterface.BUTTON_POSITIVE == result) {
                DateTime mDateTime = new DateTime(
                        mDatePicker.getYear(),
                        mDatePicker.getMonth(),
                        mDatePicker.getDayOfMonth(),
                        mTimePicker.getCurrentHour(),
                        mTimePicker.getCurrentMinute()
                );
                updateTime();
                mOnDateTimeSetListener.DateTimeSet(mDateTime.getDate());
            }
            else if(DialogInterface.BUTTON_NEGATIVE == result) {
                mOnDateTimeSetListener.DateTimeSet(null);
            }
        }
    }
    /**
     * Interface for sending the Date and Time to the calling object
     */
    public interface OnDateTimeSetListener {
        public void DateTimeSet(Date date);
    }
}