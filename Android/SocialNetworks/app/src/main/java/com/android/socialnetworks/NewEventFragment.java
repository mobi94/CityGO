package com.android.socialnetworks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Date;
import java.util.Locale;

public class NewEventFragment extends Fragment  implements DateTimePicker.OnDateTimeSetListener{

    private ActionMode mActionMode;
    private ImageView eventType;
    private String eventTypeString;
    private MaterialEditText editTitle;
    private MaterialEditText editDescription;
    private MaterialEditText editAvailableSits;
    private MaterialEditText editStartDate;
    private MaterialEditText editTemporary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.new_event_fragment, container, false);

        if (mActionMode == null) {
            mActionMode = getActivity().startActionMode(mActionModeCallback);
            //mActionMode.setCustomView(inflater.inflate(R.layout.contextual_action_bar,null));
        }

        eventType = (ImageView)rootView.findViewById(R.id.event_type);
        setEventType();
        editTitle = (MaterialEditText)rootView.findViewById(R.id.event_title);
        editTitle.setBackgroundResource(R.drawable.background_normal);
        titleListener();
        editDescription = (MaterialEditText)rootView.findViewById(R.id.event_description);
        editDescription.setBackgroundResource(R.drawable.background_normal);
        editAvailableSits = (MaterialEditText)rootView.findViewById(R.id.event_available_seats);
        editAvailableSits.setBackgroundResource(R.drawable.background_normal);
        editAvailableSits.setInputType(InputType.TYPE_NULL);
        setAvailableSits();
        editStartDate = (MaterialEditText)rootView.findViewById(R.id.event_start_date);
        editStartDate.setBackgroundResource(R.drawable.background_normal);
        editStartDate.setInputType(InputType.TYPE_NULL);
        setStartDate();
        editTemporary = (MaterialEditText)rootView.findViewById(R.id.event_temporary);
        editTemporary.setBackgroundResource(R.drawable.background_normal);
        editTemporary.setInputType(InputType.TYPE_NULL);
        setTemporary();

        return rootView;
    }

    private void titleListener() {
        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>0  &&  s.length()<=15 && !TextUtils.isEmpty(s.toString().trim())) {
                    editTitle.setBackgroundResource(R.drawable.background_normal);
                    editTitle.setFloatingLabelText("     " + "Title");
                }
                else {
                    editTitle.setBackgroundResource(R.drawable.background_error);
                    if (s.length()>15) editTitle.setFloatingLabelText("   " + getString(R.string.user_name_hint_2));
                    else if (TextUtils.isEmpty(s.toString().trim()))
                                editTitle.setFloatingLabelText("     " + "Shouldn't consist of spaces");
                }
            }
        });
    }

    private void setEventType(){
        final NumberPicker  aNumberPicker;
        AlertDialog.Builder alertBw;
        final AlertDialog alertDw;

        RelativeLayout linearLayout=new RelativeLayout(getActivity());
        aNumberPicker=new NumberPicker(getActivity());
        final String[] values=new String[6];
        values[0]="Meet";
        values[1]="Movie";
        values[2]="Sport";
        values[3]="Business";
        values[4]="Coffee";
        values[5]="Love";
        aNumberPicker.setMaxValue(values.length-1);
        aNumberPicker.setMinValue(0);
        aNumberPicker.setDisplayedValues(values);
        aNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250,100);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker,numPicerParams);

        alertBw=new AlertDialog.Builder(getActivity());
        alertBw.setTitle("Set event type:");
        alertBw.setView(linearLayout);
        alertBw.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventTypeString = values[aNumberPicker.getValue()];
                dialog.dismiss();
            }
        });
        alertBw.setNeutralButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        alertDw=alertBw.create();

        eventType.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v == eventType)  alertDw.show();
                return false;
            }
        });
    }

    private void setAvailableSits(){
        final NumberPicker aNumberPicker;
        AlertDialog.Builder alertBw;
        final AlertDialog alertDw;

        RelativeLayout linearLayout=new RelativeLayout(getActivity());
        aNumberPicker=new NumberPicker(getActivity());
        aNumberPicker.setMaxValue(100);
        aNumberPicker.setMinValue(1);
        aNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250,100);
        RelativeLayout.LayoutParams numPicerParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker,numPicerParams);

        alertBw=new AlertDialog.Builder(getActivity());
        alertBw.setTitle("Set available sits:");
        alertBw.setView(linearLayout);
        alertBw.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editAvailableSits.setText(Integer.toString(aNumberPicker.getValue()));
                dialog.dismiss();
            }
        });
        alertBw.setNeutralButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        alertDw=alertBw.create();

        editAvailableSits.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v == editAvailableSits) alertDw.show();
                return false;
            }
        });
    }

    private void setStartDate() {
        final SimpleDateTimePicker simpleDateTimePicker = SimpleDateTimePicker.make(
                "Set Date & Time",
                new Date(),
                NewEventFragment.this,
                getFragmentManager()
        );
        editStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editStartDate.setEnabled(false);
                simpleDateTimePicker.show();
            }
        });
        editStartDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editStartDate.setEnabled(false);
                    simpleDateTimePicker.show();
                }
            }
        });
    }

    @Override
    public void DateTimeSet(Date date) {
        if (date != null) {
            DateTime dateTime = new DateTime(date);
            if (getResources().getConfiguration().locale != Locale.US) {
                editStartDate.setText(dateTime.getDateString().replace("at", "на"));
                editStartDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.event_start_date_text_size));
            }
            else editStartDate.setText(dateTime.getDateString());
        }
        editStartDate.setEnabled(true);
    }

    private void setTemporary(){
        final NumberPicker  aNumberPicker;
        AlertDialog.Builder alertBw;
        final AlertDialog alertDw;

        RelativeLayout linearLayout=new RelativeLayout(getActivity());
        aNumberPicker=new NumberPicker(getActivity());
        final String[] values=new String[2];
        values[0]="Temporary";
        values[1]="Regular";
        aNumberPicker.setMaxValue(values.length-1);
        aNumberPicker.setMinValue(0);
        aNumberPicker.setDisplayedValues(values);
        aNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250,100);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker,numPicerParams);

        alertBw=new AlertDialog.Builder(getActivity());
        alertBw.setTitle("Set event duration:");
        alertBw.setView(linearLayout);
        alertBw.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editTemporary.setText(values[aNumberPicker.getValue()]);
                dialog.dismiss();
            }
        });
        alertBw.setNeutralButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        alertDw=alertBw.create();

        editTemporary.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v == editTemporary)  alertDw.show();
                return false;
            }
        });
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_create:
                    mode.finish(); // Action picked, so close the CAB
                    getFragmentManager().popBackStackImmediate();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            getFragmentManager().popBackStackImmediate();
        }
    };
}