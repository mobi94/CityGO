package com.android.socialnetworks;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditProfileFragment extends Fragment {

    private MyProgressDialog progressDialog;
    private MaterialEditText editNickname;
    private EditText editBirthday;
    private EditText editGender;
    private Button okButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.change_profile_fragment, container, false);
        progressDialog = new MyProgressDialog(getActivity());

        editNickname = (MaterialEditText)rootView.findViewById(R.id.nickname_profile);
        editNickname.setBackgroundResource(R.drawable.background_normal);
        userNameListener();
        editBirthday = (EditText) rootView.findViewById(R.id.birthday_profile);
        editBirthday.setBackgroundResource(R.drawable.background_normal);
        editBirthday.setInputType(InputType.TYPE_NULL);
        editGender = (EditText) rootView.findViewById(R.id.gender_profile);
        editGender.setBackgroundResource(R.drawable.background_normal);
        editGender.setInputType(InputType.TYPE_NULL);
        okButton = (Button)rootView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(buttonsClick);
        Button cancelButton = (Button) rootView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(buttonsClick);

        Bundle bundle = getArguments();
        if(bundle != null){
            editNickname.setText(bundle.getString("NICKNAME", ""));
            editGender.setText(bundle.getString("GENDER", ""));
            editBirthday.setText(bundle.getString("BIRTHDAY", ""));
        }
        else getProfileData();

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (!Character.isLetterOrDigit(currentChar)/* && !Character.isSpaceChar(currentChar)*/) {
                            sourceAsSpannableBuilder.delete(i, i+1);
                        }
                    }
                    return source;
                } else {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char currentChar = source.charAt(i);
                        if (Character.isLetterOrDigit(currentChar) /*|| Character.isSpaceChar(currentChar)*/) {
                            filteredStringBuilder.append(currentChar);
                        }
                    }
                    return filteredStringBuilder.toString();
                }
            }
        };
        editNickname.setFilters(new InputFilter[]{filter});
        setBirthdayField();
        setGenderField();

        return rootView;
    }

    private void getProfileData(){
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            editNickname.setText(currentUser.getString("nickname"));
            editGender.setText(currentUser.getString("gender"));
            editBirthday.setText(currentUser.getString("birthday"));
        }
    }

    private View.OnClickListener buttonsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.ok_button:
                    if(!SignUpActivity.isNetworkOn(getActivity().getBaseContext())) {
                        Toast.makeText(getActivity().getBaseContext(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
                    } else {
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        if (currentUser != null) {
                            updateUserData(currentUser);
                        }
                        getFragmentManager().popBackStackImmediate();
                    }
                    break;
                case R.id.cancel_button:
                    getFragmentManager().popBackStackImmediate();
                    break;
            }
        }
    };

    private void updateUserData(ParseUser currentUser){
        currentUser.put("nickname", editNickname.getText().toString());
        currentUser.put("birthday", editBirthday.getText().toString());
        currentUser.put("gender", editGender.getText().toString());
        currentUser.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Log.d("UPDATE_ERROR", "updateUserData" + e);
                    Toast.makeText(getActivity(), "UPDATE_ERROR: " + e, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void userNameListener() {
        editNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((s.length()==0 || s.length()>=2) &&  s.length()<=60) {
                    editNickname.setBackgroundResource(R.drawable.background_normal);
                    editNickname.setFloatingLabelText("   " + "New nickname");
                    okButton.setEnabled(true);
                }
                else {
                    editNickname.setBackgroundResource(R.drawable.background_error);
                    editNickname.setFloatingLabelText("   " + getString(R.string.user_name_hint));
                    okButton.setEnabled(false);
                }
            }
        });
    }

    private void setBirthdayField() {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                editBirthday.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(newCalendar.getTimeInMillis());
        Date date = null;
        try {
            date = dateFormatter.parse(editBirthday.getText().toString());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Calendar birthDay = Calendar.getInstance();
        if (date != null) {
            birthDay.setTimeInMillis(date.getTime());
        }
        datePickerDialog.getDatePicker().init(birthDay.getTime().getYear()+1900, birthDay.getTime().getMonth(),
                birthDay.getTime().getDay(), datePickerDialog);
        editBirthday.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v == editBirthday) {
                    datePickerDialog.show();
                }
                return false;
            }
        });
    }

    private void setGenderField(){
        final NumberPicker aNumberPicker;
        AlertDialog.Builder alertBw;
        final AlertDialog alertDw;

        RelativeLayout linearLayout=new RelativeLayout(getActivity());
        aNumberPicker=new NumberPicker(getActivity());
        final String[] values=new String[2];
        values[0]=getString(R.string.gender_dialog_man_value);
        values[1]=getString(R.string.gender_dialog_female_value);
        aNumberPicker.setMaxValue(values.length-1);
        aNumberPicker.setMinValue(0);
        aNumberPicker.setDisplayedValues(values);
        aNumberPicker.setWrapSelectorWheel(false);
        aNumberPicker.setClickable(false);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250,100);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker,numPicerParams);

        alertBw=new AlertDialog.Builder(getActivity());
        alertBw.setTitle(getString(R.string.gender_dialog_title));
        alertBw.setView(linearLayout);
        alertBw.setPositiveButton(getString(R.string.gender_dialog_ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editGender.setText(values[aNumberPicker.getValue()]);
                dialog.dismiss();
            }
        });
        alertBw.setNeutralButton(getString(R.string.gender_dialog_cancel_button), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        alertDw=alertBw.create();

        editGender.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v == editGender)  alertDw.show();
                return false;
            }
        });
    }

}
