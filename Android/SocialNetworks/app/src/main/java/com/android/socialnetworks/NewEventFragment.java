package com.android.socialnetworks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewEventFragment extends Fragment implements DateTimePicker.OnDateTimeSetListener{

    private ImageView eventType;
    private String eventTypeString = "";
    private MaterialEditText editTitle;
    private MaterialEditText editDescription;
    private MaterialEditText editAvailableSits;
    private MaterialEditText editStartDate;
    private MaterialEditText editTemporary;
    private MyProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.new_event_fragment, container, false);
        Bundle bundle = getArguments();
        String eventUser = bundle.getString("CURRENT_USER", "");
        String currentUser = ParseUser.getCurrentUser().getUsername();

        setHasOptionsMenu(true);

        progressDialog = new MyProgressDialog(getActivity());
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (eventUser.equals(currentUser)) actionBar.setTitle("Edit Go Event");
            else actionBar.setTitle("Create Go Event");
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

        if (eventUser.equals(currentUser)){
            if (MainActivity.newMarker != null) {
                eventType.setImageDrawable(getResources().getDrawable(getMarkerIcon(Integer.parseInt(MainActivity.newMarker.getCategory()))));
                eventTypeString = MainActivity.newMarker.getCategory();
                editTitle.setText(MainActivity.newMarker.getTitle());
                editDescription.setText(MainActivity.newMarker.getDescription());
                editAvailableSits.setText(MainActivity.newMarker.getAvailableSeats());
                SimpleDateFormat format;
                if (getResources().getConfiguration().locale != Locale.US)
                    format = new SimpleDateFormat("d MMMM, yyyy 'на' HH:mm");
                else
                    format = new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a");
                editStartDate.setText(format.format(MainActivity.newMarker.getStartDate()));
                editTemporary.setText(MainActivity.newMarker.getDuration());
            }
        }

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

    private int getMarkerIcon(int categoryIcon){
        switch(categoryIcon){
            case 0: return R.drawable.event_love;
            case 1: return R.drawable.event_movie;
            case 2: return R.drawable.event_sport;
            case 3: return R.drawable.event_business;
            case 4: return R.drawable.event_coffee;
            case 5: return R.drawable.event_meet;
            default: return R.drawable.event_meet;
        }
    }

    private void setEventType(){
        final NumberPicker  aNumberPicker;
        AlertDialog.Builder alertBw;
        final AlertDialog alertDw;

        RelativeLayout linearLayout=new RelativeLayout(getActivity());
        aNumberPicker=new NumberPicker(getActivity());
        final String[] values=new String[6];
        values[0]="Love";
        values[1]="Movie";
        values[2]="Sport";
        values[3]="Business";
        values[4]="Coffee";
        values[5]="Meet";
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
                eventType.setImageDrawable(getResources().getDrawable(getMarkerIcon(aNumberPicker.getValue())));
                eventTypeString = Integer.toString(aNumberPicker.getValue());
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
            if (getResources().getConfiguration().locale != Locale.US)
                editStartDate.setText(dateTime.getDateString("d MMMM, yyyy 'на' HH:mm"));
            else
                editStartDate.setText(dateTime.getDateString());
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_event, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MainActivity.EVENT_FRAGMENT_RESULT = "canceled";
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            case R.id.action_done:
                if(!SignUpActivity.isNetworkOn(getActivity())) {
                    Toast.makeText(getActivity(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
                } else {
                    if (getArguments().getString("CURRENT_USER", "").equals(ParseUser.getCurrentUser().getUsername())){
                        if (isEditFieldsEdited()) {
                            if (isEditFieldsFilled()) updateParseObject();
                            else Toast.makeText(getActivity(), "You must fill all necessary fields!", Toast.LENGTH_SHORT).show();
                        }
                        else getActivity().getSupportFragmentManager().popBackStack();
                    }
                    else {
                        if (isEditFieldsFilled()) createParseObject();
                        else Toast.makeText(getActivity(), "You must fill all necessary fields!", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isEditFieldsFilled() {
        return !editTitle.getText().toString().equals("") && !editTemporary.getText().toString().equals("")
        && !editStartDate.getText().toString().equals("") && !editAvailableSits.getText().toString().equals("")
        && !eventTypeString.equals("");
    }

    private boolean isEditFieldsEdited() {
        SimpleDateFormat format;
        if (getResources().getConfiguration().locale != Locale.US)
            format = new SimpleDateFormat("d MMMM, yyyy 'на' HH:mm");
        else
            format = new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a");
        return MainActivity.newMarker != null
                && (!editTitle.getText().toString().equals(MainActivity.newMarker.getTitle())
                || !editTemporary.getText().toString().equals(MainActivity.newMarker.getDuration())
                || !editStartDate.getText().toString().equals(format.format(MainActivity.newMarker.getStartDate()))
                || !editAvailableSits.getText().toString().equals(MainActivity.newMarker.getAvailableSeats())
                || !eventTypeString.equals(MainActivity.newMarker.getCategory())
                || !editDescription.getText().toString().equals(MainActivity.newMarker.getDescription()));
    }

    private void createParseObject(){
        progressDialog.showProgress("Sending data...");
        final ParseObject goEvent = new ParseObject("GoEvents");
        String title = editTitle.getText().toString();
        String category = eventTypeString;
        String duration = editTemporary.getText().toString();
        String availableSeats = editAvailableSits.getText().toString();
        String description = editDescription.getText().toString();
        String creatorAge = "";
        String creatorGender = "";
        String creatorName = "";
        String creatorNickName = "";
        String creatorAvatarUrl = "";
        LatLng location;
        Date startDate;

        Bundle bundle = getArguments();
        ParseGeoPoint point = new ParseGeoPoint(bundle.getDouble("LOCATION_LAT", 0), bundle.getDouble("LOCATION_LONG", 0));
        location = new LatLng(bundle.getDouble("LOCATION_LAT", 0), bundle.getDouble("LOCATION_LONG", 0));

        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            creatorAge = Integer.toString(MainActivity.getAge(currentUser.getString("birthday")));
            creatorGender = currentUser.getString("gender");
            creatorName = currentUser.getString("username");
            creatorNickName = currentUser.getString("nickname");
            creatorAvatarUrl = currentUser.getString("avatarURL");
            if (creatorAvatarUrl == null || creatorAvatarUrl.equals("")) {
                ParseFile photo = (ParseFile) currentUser.get("profilePic");
                creatorAvatarUrl = photo.getUrl();
            }
        }

        SimpleDateFormat mFormat;
        if (getResources().getConfiguration().locale != Locale.US)
            mFormat = new SimpleDateFormat("d MMMM, yyyy 'на' HH:mm");
        else
            mFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a");
        startDate = new Date();
        try {
            startDate = mFormat.parse(editStartDate.getText().toString());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        goEvent.put("title", title);
        goEvent.put("creatorAge", creatorAge);
        goEvent.put("creatorGender", creatorGender);
        goEvent.put("creatorName", creatorName);
        goEvent.put("creatorNickName", creatorNickName);
        goEvent.put("creatorAvatarUrl", creatorAvatarUrl);
        goEvent.put("category", category);
        goEvent.put("temporary", duration);
        goEvent.put("avaibleSeats", availableSeats);
        goEvent.put("description", description);
        goEvent.put("startDate", startDate);
        goEvent.put("location", point);

        MainActivity.newMarker = new MyMarker(title, creatorAge, creatorGender, creatorName, creatorNickName, creatorAvatarUrl,
                category, duration, availableSeats, description, startDate, location);

        goEvent.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), "CREATE_EVENT_ERROR: " + e, Toast.LENGTH_LONG).show();
                    progressDialog.hideProgress();
                }
                else {
                    if (currentUser != null) {
                        ParseRelation<ParseObject> relation = currentUser.getRelation("goEvent");
                        relation.add(goEvent);
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Toast.makeText(getActivity(), "CREATE_EVENT_ERROR: " + e, Toast.LENGTH_LONG).show();
                                    MainActivity.EVENT_FRAGMENT_RESULT = "cancel";
                                }
                                else {
                                    Toast.makeText(getActivity(), "Event was successfully created!", Toast.LENGTH_SHORT).show();
                                    MainActivity.EVENT_FRAGMENT_RESULT = "done";
                                }
                                progressDialog.hideProgress();
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
                    }
                    else {
                        MainActivity.EVENT_FRAGMENT_RESULT = "cancel";
                        progressDialog.hideProgress();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
            }
        });
    }

    private void updateParseObject(){
        if (MainActivity.newMarker != null) {
            progressDialog.showProgress("Updating data...");

            final String title = editTitle.getText().toString();
            final String category = eventTypeString;
            final String duration = editTemporary.getText().toString();
            final String availableSeats = editAvailableSits.getText().toString();
            final String description = editDescription.getText().toString();
            final String startDate = editStartDate.getText().toString();

            final ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                ParseRelation<ParseObject> relation = currentUser.getRelation("goEvent");
                relation.getQuery().getInBackground(getArguments().getString("MARKER_ID", ""), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e != null) {
                            Toast.makeText(getActivity(), "UPDATE_EVENT_ERROR: " + e, Toast.LENGTH_LONG).show();
                            progressDialog.hideProgress();
                            MainActivity.EVENT_FRAGMENT_RESULT = "cancel";
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                        else {
                            if (!title.equals(MainActivity.newMarker.getTitle()))
                                parseObject.put("title", title);
                            if (!category.equals(MainActivity.newMarker.getCategory()))
                                parseObject.put("category", category);
                            if (!duration.equals(MainActivity.newMarker.getDuration()))
                                parseObject.put("temporary", duration);
                            if (!availableSeats.equals(MainActivity.newMarker.getAvailableSeats()))
                                parseObject.put("avaibleSeats", availableSeats);
                            if (!description.equals(MainActivity.newMarker.getDescription()))
                                parseObject.put("description", description);
                            final SimpleDateFormat format;
                            if (getResources().getConfiguration().locale != Locale.US)
                                format = new SimpleDateFormat("d MMMM, yyyy 'на' HH:mm");
                            else
                                format = new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a");
                            if (!startDate.equals(format.format(MainActivity.newMarker.getStartDate())))
                                try {
                                    parseObject.put("startDate", format.parse(startDate));
                                } catch (java.text.ParseException e1) {
                                    e1.printStackTrace();
                                }
                            parseObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Toast.makeText(getActivity(), "UPDATE_EVENT_ERROR: " + e, Toast.LENGTH_LONG).show();
                                        MainActivity.EVENT_FRAGMENT_RESULT = "cancel";
                                    }
                                    else {
                                        if (!title.equals(MainActivity.newMarker.getTitle()))
                                            MainActivity.newMarker.setTitle(title);
                                        if (!category.equals(MainActivity.newMarker.getCategory()))
                                            MainActivity.newMarker.setCategory(category);
                                        if (!duration.equals(MainActivity.newMarker.getDuration()))
                                            MainActivity.newMarker.setDuration(duration);
                                        if (!availableSeats.equals(MainActivity.newMarker.getAvailableSeats()))
                                            MainActivity.newMarker.setAvailableSeats(availableSeats);
                                        if (!description.equals(MainActivity.newMarker.getDescription()))
                                            MainActivity.newMarker.setDescription(description);
                                        if (!startDate.equals(format.format(MainActivity.newMarker.getStartDate())))
                                            try {
                                                MainActivity.newMarker.setStartDate(format.parse(startDate));
                                            } catch (java.text.ParseException e1) {
                                                e1.printStackTrace();
                                            }
                                        MainActivity.EVENT_FRAGMENT_RESULT = "done";
                                    }
                                    progressDialog.hideProgress();
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            });
                        }
                    }
                });
            }
            else  {
                MainActivity.EVENT_FRAGMENT_RESULT = "cancel";
                progressDialog.hideProgress();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }
}