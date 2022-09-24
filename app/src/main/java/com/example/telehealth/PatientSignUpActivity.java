package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.telehealth.adapters.CustomSpinnerAdapter;
import com.example.telehealth.customfonts.MyTextView_Poppins_Medium;
import com.example.telehealth.customfonts.MyTextView_Poppins_Regular;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.constant.ImageProvider;
import com.github.dhaval2404.imagepicker.listener.DismissListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class PatientSignUpActivity extends AppCompatActivity {

    private static final int PROFILE_IMAGE_REQ_CODE = 101;

    Uri validIdUri;

    DatePickerDialog picker;
    public static final String TAG = "PatientSignUp";
    //Button
    MyTextView_Poppins_Medium signUp, signIn, uploadId;

    MyTextView_Poppins_Regular mBirthday;

    //EditText
    EditText mEmail, mPassword, mConfirmPasswor, mUserName, mMobile, mAge;

    Spinner gender;

    //ProgressBar
    private ProgressBar mProgressBar;
    //Progress dialog
    ProgressDialog signUpProgress;
    //Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Firebase Database
    // Write a message to the database
    private DatabaseReference mDatabase;
    //Firbase storage
    private StorageReference mImageStorage;

    private CustomSpinnerAdapter customSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_sign_up);

        //Firebase storage
        mImageStorage = FirebaseStorage.getInstance().getReference();
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        //Button initialization
        signUp = findViewById(R.id.sign_up_button_reg_page);
        signIn = findViewById(R.id.sign_in_button);
        uploadId = findViewById(R.id.upload_id);

        //EditText initialization
        mEmail = findViewById(R.id.email_for_sign_up);
        mPassword = (EditText)findViewById(R.id.pass_for_sign_up);
        mConfirmPasswor = (EditText)findViewById(R.id.confirm_pass_for_sign_up);
        mUserName = (EditText)findViewById(R.id.user_name_for__sign_up);
        mMobile = (EditText)findViewById(R.id.phone_for__sign_up);
        mBirthday = findViewById(R.id.birthday);
        mAge = findViewById(R.id.age);

        //Spinner
        gender = findViewById(R.id.gender);


        customSpinnerAdapter =
                new CustomSpinnerAdapter(this, R.layout.spinner_item, Arrays.asList(getResources().getStringArray(R.array.gender_array)));
        gender.setAdapter(customSpinnerAdapter);

        signUpProgress = new ProgressDialog(this);
        signUpProgress.setTitle("Sign Up process");
        signUpProgress.setMessage("Please wait for a while");
        signUpProgress.setCanceledOnTouchOutside(false);

        //Birthday
        mBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                picker = new DatePickerDialog(PatientSignUpActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String bDay = (month + 1) + "/" + dayOfMonth + "/" + year;
                        mBirthday.setText(bDay);
                        mAge.setText(String.valueOf(getAge(bDay)));
                    }
                },year,month,day);
                picker.show();
            }
        });

        //Sign Up Button onClick method
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String pass = mPassword.getText().toString().trim();
                String conPass = mConfirmPasswor.getText().toString().trim();
                final String name = mUserName.getText().toString().trim();
                final String mobile = mMobile.getText().toString().trim();
                String birthday = mBirthday.getText().toString().trim();
                String age = mAge.getText().toString().trim();
                String mGender = gender.getSelectedItem().toString().trim();

                if(!pass.equals(conPass)){
                    Toast.makeText(getApplicationContext(),"Passwords are not same",Toast.LENGTH_SHORT).show();
                }
                else if(email.isEmpty() || pass.isEmpty() || conPass.isEmpty() || name.isEmpty() || mobile.isEmpty() || birthday.isEmpty() || age.isEmpty() || validIdUri == null){
                    Toast.makeText(getApplicationContext(),"Please fill all requirements.",Toast.LENGTH_SHORT).show();
                }
                else{
                    signUpProgress.show();
                    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
                                String uid = currentUser.getUid();
                                String imgName = uid + ".jpg";
                                final StorageReference imageRef = mImageStorage.child("valid_id_image").child(uid).child(imgName);
                                imageRef.putFile(validIdUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if(task.isSuccessful())
                                        {
                                            //Insert into Database
                                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                                            HashMap<String, String> userMap = new HashMap<String, String>();
                                            userMap.put("name",name);
                                            userMap.put("mobile",mobile);
                                            userMap.put("image","Default");
                                            userMap.put("status","Patient");
                                            userMap.put("birthday", birthday);
                                            userMap.put("age", age);
                                            userMap.put("validIdUrl", imgName);
                                            userMap.put("gender", mGender);
                                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    signUpProgress.dismiss();
                                                    if(task.isSuccessful()){
                                                        startActivity(new Intent(PatientSignUpActivity.this, PatientCheckUpActivity.class));
                                                        finish();
                                                    }
                                                    else{
                                                        Toast.makeText(getApplicationContext(),"Registration failed.",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }else
                                        {
                                            signUpProgress.dismiss();
                                            Toast.makeText(getApplicationContext(),"Registration failed.",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else {
                                signUpProgress.dismiss();
                                Toast.makeText(getApplicationContext(),"Registration failed.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PatientSignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

        uploadId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setValidID();
            }
        });
    }

    private void setValidID()
    {
        ImagePicker
            .with(PatientSignUpActivity.this)
            .compress(500)
            .maxResultSize(620, 620)
            .setImageProviderInterceptor(new Function1<ImageProvider, Unit>() {
                @Override
                public Unit invoke(ImageProvider imageProvider) {
                    Log.d("ImagePicker", "Selected ImageProvider: " + imageProvider.toString());
                    return null;
                }
            })
            .setDismissListener(new DismissListener() {
                @Override
                public void onDismiss() {

                }
            })
            .start(PROFILE_IMAGE_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Uri object will not be null for RESULT_OK
            validIdUri = data.getData();
            uploadId.setText("CHANGE  VALID ID");
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private int getAge(String dobString){

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null) return 0;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int year = dob.get(Calendar.YEAR);
        int month = dob.get(Calendar.MONTH);
        int day = dob.get(Calendar.DAY_OF_MONTH);

        dob.set(year, month+1, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        return age;
    }
}