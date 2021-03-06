package com.android.socialnetworks.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.android.socialnetworks.R;
import com.android.socialnetworks.activities.SignUpActivity;
import com.makeramen.RoundedTransformationBuilder;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class RegistrationFragment extends Fragment {

    private ImageView avatar;
    private ImageButton avatarCamera;
    private MaterialEditText editUserName;
    private MaterialEditText editPassword;
    private MaterialEditText editConfirmPassword;
    private MaterialEditText editEmail;
    private EditText editBirthday;
    private EditText editGender;
    private Button signupButton;
    private SmoothProgressBar progressBar;
    private boolean isUserNameValid;
    private boolean isEmailValid;
    private boolean isPasswordValid;
    private boolean isPasswordsMatches;

    private DatePickerDialog datePickerDialog;
    private Uri outputFileUri;
    private Uri generalUri;
    private int incrementUserPhoto = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.registration_fragment, container, false);

        avatar = (ImageView)rootView.findViewById(R.id.avatar_signup);
        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.WHITE)
                .borderWidthDp(1)
                .cornerRadiusDp(200)
                .oval(false)
                .build();
        Picasso.with(getActivity())
                .load(R.drawable.user)
                .transform(transformation)
                .resize(400, 400)
                .centerCrop()
                .into(avatar);

        avatarCamera = (ImageButton)rootView.findViewById(R.id.avatar_camera);
        avatarCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        editUserName = (MaterialEditText)rootView.findViewById(R.id.username_signup);
        editUserName.setBackgroundResource(R.drawable.background_niagara);
        userNameListener();
        editEmail = (MaterialEditText)rootView.findViewById(R.id.email_signup);
        editEmail.setBackgroundResource(R.drawable.background_normal);
        emailListener();
        editPassword = (MaterialEditText) rootView.findViewById(R.id.password_signup);
        editPassword.setBackgroundResource(R.drawable.background_normal);
        passwordListener();
        editConfirmPassword = (MaterialEditText) rootView.findViewById(R.id.confirm_password_signup);
        editConfirmPassword.setBackgroundResource(R.drawable.background_normal);
        confirmPasswordListener();

        editBirthday = (EditText) rootView.findViewById(R.id.birthday_signup);
        editBirthday.setBackgroundResource(R.drawable.background_normal);
        editBirthday.setInputType(InputType.TYPE_NULL);
        editGender = (EditText) rootView.findViewById(R.id.gender_signup);
        editGender.setBackgroundResource(R.drawable.background_normal);
        editGender.setInputType(InputType.TYPE_NULL);

        signupButton = (Button)rootView.findViewById(R.id.signup_main);
        signupButton.setOnClickListener(buttonsClick);

        progressBar = (SmoothProgressBar) rootView.findViewById(R.id.registration_progress_bar);
        progressBar.setVisibility(View.GONE);

        Button backButton = (Button) rootView.findViewById(R.id.back_button);
        backButton.setOnClickListener(buttonsClick);

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (!Character.isLetterOrDigit(currentChar) && currentChar != '_') {
                            sourceAsSpannableBuilder.delete(i, i+1);
                        }
                    }
                    return source;
                } else {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char currentChar = source.charAt(i);
                        if (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
                            filteredStringBuilder.append(currentChar);
                        }
                    }
                    return filteredStringBuilder.toString();
                }
            }
        };
        editPassword.setFilters(new InputFilter[]{filter});
        editUserName.setFilters(new InputFilter[]{filter});

        setBirthdayField();
        setGenderField();

        signupButton.setEnabled(false);

        return rootView;
    }

    private void disableAllViews(){
        RelativeLayout layout = (RelativeLayout) getActivity().findViewById(R.id.registration_fragment);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }
    }

    private void enableAllViews(){
        RelativeLayout layout = (RelativeLayout) getActivity().findViewById(R.id.registration_fragment);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }
    }

    private void passwordListener(){
        editPassword.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                if (isPasswordValid) {
                    editPassword.setBackgroundResource(R.drawable.background_normal);
                    editPassword.setFloatingLabelText("");
                }
                else {
                    editPassword.setBackgroundResource(R.drawable.background_error);
                    editPassword.setFloatingLabelText("     " + getString(R.string.password_edit_text_sign_up_hint));
                }
                updateLoginButtonState();
            }
        });
    }

    private void confirmPasswordListener(){
        editConfirmPassword.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword(s.toString());
                if (isPasswordsMatches) {
                    editConfirmPassword.setBackgroundResource(R.drawable.background_normal);
                    editConfirmPassword.setFloatingLabelText("");
                }
                else {
                    editConfirmPassword.setBackgroundResource(R.drawable.background_error);
                    editConfirmPassword.setFloatingLabelText("     " + getString(R.string.confirm_password_edit_text_hint));
                }
                updateLoginButtonState();
            }
        });
    }

    private void emailListener() {
        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
                if (isEmailValid) {
                    editEmail.setBackgroundResource(R.drawable.background_normal);
                    editEmail.setFloatingLabelText("");
                }
                else {
                    editEmail.setBackgroundResource(R.drawable.background_error);
                    editEmail.setFloatingLabelText("     " + getString(R.string.email_edit_text_hint));
                }
                updateLoginButtonState();
            }
        });
    }

    private void userNameListener() {
        editUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUserName(s.toString());
                if (isUserNameValid) {
                    editUserName.setBackgroundResource(R.drawable.background_niagara);
                    editUserName.setFloatingLabelText("");
                }
                else {
                    editUserName.setBackgroundResource(R.drawable.background_error);
                    if (s.length()<=2) editUserName.setFloatingLabelText("     " + getString(R.string.user_name_hint_1));
                    else editUserName.setFloatingLabelText("     " + getString(R.string.user_name_hint_2));
                }
                updateLoginButtonState();
            }
        });
    }

    private void validateEmail(String text) { isEmailValid = Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    private void validateUserName(String text) { isUserNameValid = text.length()>=2 && text.length()<=25;
    }

    private void validatePassword(String text) {
        boolean upperFound = false;
        boolean digitFound = false;
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) {
                upperFound = true;
                break;
            }
        }
        for (char c : text.toCharArray()) {
            if (Character.isDigit(c)) {
                digitFound = true;
                break;
            }
        }
        isPasswordValid = text.length()>=6 && upperFound && digitFound;
    }

    private void validateConfirmPassword(String text) { isPasswordsMatches =
            text.equals(editPassword.getText().toString());
    }

    private void updateLoginButtonState() {
        signupButton.setText(getString(R.string.sign_up_main_button));
        if(isEmailValid && isPasswordsMatches && isPasswordValid && isUserNameValid) { signupButton.setEnabled(true);
        } else { signupButton.setEnabled(false);
        }
    }

    private View.OnClickListener buttonsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.signup_main:
                    if(!SignUpActivity.isNetworkOn(getActivity())) {
                        Toast.makeText(getActivity(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
                    } else {
                        // do login
                        signupButton.setText(getString(R.string.progress_bar_loading));
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.progressiveStart();
                        disableAllViews();
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        if (currentUser == null) {
                            // show the signup or login screen
                            prepareNewUser();
                        }
                    }
                    break;
                case R.id.back_button:
                    getFragmentManager().popBackStackImmediate();
                    break;
            }
        }
    };

    private void setBirthdayField() {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                editBirthday.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(newCalendar.getTimeInMillis());

        editBirthday.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v == editBirthday) {
                    datePickerDialog.show();
                }
                return false;
            }
        });
        editBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    datePickerDialog.show();
                }
            }
        });
    }

    private void setGenderField(){
        final NumberPicker  aNumberPicker;
        AlertDialog.Builder alertBw;
        final AlertDialog alertDw;

        RelativeLayout linearLayout=new RelativeLayout(getActivity());
        aNumberPicker=new NumberPicker(getActivity());
        final String[] values=new String[2];
        values[0]=getString(R.string.gender_dialog_male_value);
        values[1]=getString(R.string.gender_dialog_female_value);
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
        alertBw.setTitle(getString(R.string.gender_dialog_title));
        alertBw.setView(linearLayout);
        alertBw.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editGender.setText(values[aNumberPicker.getValue()]);
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
        editGender.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v == editGender)  alertDw.show();
                return false;
            }
        });
        editGender.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alertDw.show();
                }
            }
        });
    }

    private void selectImage() {
        final CharSequence[] items = {
                getString(R.string.photo_picker_dialog_take_photo),
                getString(R.string.photo_picker_dialog_choose_from_gallery),
                getString(R.string.cancel_button) };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.photo_picker_dialog_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(items[0])) {
                    deletePhoto();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(Environment.getExternalStorageDirectory(),
                           Integer.toString(incrementUserPhoto) + "CityGO_user_photo.jpg");
                    incrementUserPhoto++;
                    outputFileUri = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    startActivityForResult(intent, 0);
                } else if (items[item].equals(items[1])) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
                } else if (items[item].equals(items[2])) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void deletePhoto(){
        if (outputFileUri != null) {
            File fdelete = new File(outputFileUri.getPath());
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    if(fdelete.exists())
                        try {
                            if (fdelete.getCanonicalFile().delete()) {
                                if (fdelete.exists()) {
                                    getActivity().deleteFile(fdelete.getName());
                                }
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = null;
            if (requestCode == 0) {
                if (data != null) {
                    if (data.hasExtra("data"))
                        imageUri = data.getParcelableExtra("data");
                }
                else imageUri = outputFileUri;
            }
            else imageUri = data.getData();
            if (imageUri != null) {
                generalUri = imageUri;
                Transformation transformation = new RoundedTransformationBuilder()
                        .borderColor(Color.WHITE)
                        .borderWidthDp(1)
                        .cornerRadiusDp(200)
                        .oval(false)
                        .build();
                Picasso.with(getActivity())
                        .load(imageUri)
                        .transform(transformation)
                        .resize(400, 400)
                        .centerCrop()
                        .into(avatar, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                deletePhoto();
                            }

                            @Override
                            public void onError() {
                            }
                        });
            }
        }
    }

    public void prepareNewUser(){
        if (generalUri != null)
            Picasso.with(getActivity())
                    .load(generalUri)
                    .resize(400, 400)
                    .centerCrop()
                    .into(target);
        else {
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(editEmail.getText());
            TextDrawable.IBuilder builder = TextDrawable.builder()
                    .beginConfig()
                    .width(400)
                    .height(400)
                    .endConfig()
                    .rect();
            TextDrawable drawable = builder.build(String.valueOf(editUserName.getText().toString().trim().charAt(0)), color);
            createNewUser(drawableToBitmap(drawable));
        }
    }

    public static Bitmap drawableToBitmap (TextDrawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void createNewUser(Bitmap bitmap) {
        final ParseUser user = new ParseUser();
        final ParseFile file = uploadImageToParse(bitmap);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    user.put("profilePic", file);
                    user.setUsername(editUserName.getText().toString());
                    user.setPassword(editPassword.getText().toString());
                    user.setEmail(editEmail.getText().toString());
                    user.put("nickname", editUserName.getText().toString());
                    if (editGender.getText().toString().equals(getString(R.string.gender_dialog_male_value)))
                        user.put("gender", "male");
                    else
                        user.put("gender", "female");
                    user.put("birthday", editBirthday.getText().toString());
                    user.signUpInBackground(new SignUpCallback() {
                        public void done(ParseException e) {
                            progressBar.progressiveStop();
                            if (e != null) {
                                Log.d("SIGNUP_ERROR", "signUpInBackground" + e);
                                Toast.makeText(getActivity(), "SIGNUP_ERROR: " + e, Toast.LENGTH_LONG).show();
                                enableAllViews();
                                updateLoginButtonState();
                            } else {
                                signupButton.setText(getString(R.string.progress_bar_success));
                                signupButton.setBackgroundResource(R.drawable.background_success);
                                SignUpActivity.startMainActivity();
                            }
                        }
                    });
                }
                else {
                    enableAllViews();
                    updateLoginButtonState();
                    progressBar.progressiveStop();
                    Toast.makeText(getActivity(), "PHOTO_UPLOADING_ERROR: " + e, Toast.LENGTH_LONG).show();
                    Log.d("PHOTO_UPLOADING_ERROR", "signUpInBackground" + e);
                }
            }
        });
    }

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    createNewUser(bitmap);
                }
            }).start();
        }
        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            enableAllViews();
            updateLoginButtonState();
            progressBar.progressiveStop();
            Toast.makeText(getActivity(), "PHOTO_UPLOADING_ERROR", Toast.LENGTH_LONG).show();
        }
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    private ParseFile uploadImageToParse(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        return new ParseFile("photo.png", imageInByte);
    }
}