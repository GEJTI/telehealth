package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.telehealth.adapters.CustomSpinnerAdapter;
import com.example.telehealth.customfonts.EditText_Poppins_Regular;
import com.example.telehealth.customfonts.MyTextView_Poppins_Medium;
import com.example.telehealth.customfonts.MyTextView_Poppins_Regular;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.constant.ImageProvider;
import com.github.dhaval2404.imagepicker.listener.DismissListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class DoctorSignUpActivity extends AppCompatActivity {

    public static final String TAG = "DoctorSignUp";

    private static final int PROFILE_IMAGE_REQ_CODE = 101;

    Uri licenseUri;

    DatePickerDialog picker;
    MyTextView_Poppins_Medium ok_date, upload_license, sign_in_button, sign_up_button;
    MyTextView_Poppins_Regular date_license, start_date,end_date;

    EditText editEmail, editPassword, editCPassword, editName, editMobileNo;

    Spinner doctor_specialization;

    ProgressDialog signUpProgress;

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
        setContentView(R.layout.activity_doctor_sign_up);

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

        editEmail = findViewById(R.id.email_for_sign_up);
        editPassword = findViewById(R.id.pass_for_sign_up);
        editCPassword = findViewById(R.id.confirm_pass_for_sign_up);
        editName = findViewById(R.id.name_for_sign_up);
        doctor_specialization = findViewById(R.id.doctor_specialization);
        editMobileNo = findViewById(R.id.phone_for_sign_up);
        upload_license = findViewById(R.id.upload_license);
        date_license = findViewById(R.id.date_license_for_sign_up);
        sign_up_button = findViewById(R.id.sign_up_button_reg_page);
        sign_in_button = findViewById(R.id.sign_in_button);

        customSpinnerAdapter =
                new CustomSpinnerAdapter(this, R.layout.spinner_item, Arrays.asList(getResources().getStringArray(R.array.doctor_specializations_array)));
        doctor_specialization.setAdapter(customSpinnerAdapter);

        signUpProgress = new ProgressDialog(this);
        signUpProgress.setTitle("Sign Up process");
        signUpProgress.setMessage("Please wait for a while");
        signUpProgress.setCanceledOnTouchOutside(false);

        upload_license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLicenseImage();
            }
        });

        date_license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(DoctorSignUpActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.date_dialog,null);

                start_date = mView.findViewById(R.id.start_date);
                end_date = mView.findViewById(R.id.expiry_date);
                ok_date = mView.findViewById(R.id.ok_date);

                start_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar cldr = Calendar.getInstance();
                        int day = cldr.get(Calendar.DAY_OF_MONTH);
                        int month = cldr.get(Calendar.MONTH);
                        int year = cldr.get(Calendar.YEAR);

                        picker = new DatePickerDialog(DoctorSignUpActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                start_date.setText((month+1) + "/" + dayOfMonth + "/" + year);
                            }
                        },year,month,day);
                        picker.show();
                    }
                });
                end_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar cldr = Calendar.getInstance();
                        int day = cldr.get(Calendar.DAY_OF_MONTH);
                        int month = cldr.get(Calendar.MONTH);
                        int year = cldr.get(Calendar.YEAR);

                        picker = new DatePickerDialog(DoctorSignUpActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                end_date.setText((month+1) + "/" + dayOfMonth + "/" + year);
                            }
                        },year,month,day);
                        picker.show();
                    }
                });

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                ok_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       String start = start_date.getText().toString();
                       String end = end_date.getText().toString();
                        if (start.matches("")) {
                            Toast.makeText(DoctorSignUpActivity.this, "Please Select Start Date", Toast.LENGTH_SHORT).show();
                            return;
                        }else if (end.matches("")){
                            Toast.makeText(DoctorSignUpActivity.this, "Please Select End Date", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        date_license.setText(start_date.getText() + " - " +end_date.getText());


                        dialog.dismiss();

                    }
                });


            }
        });

        sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DoctorSignUpActivity.this, LoginActivity.class));
            }
        });
    }

    private void setLicenseImage()
    {
        ImagePicker
            .with(DoctorSignUpActivity.this)
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
            .start(PROFILE_IMAGE_REQ_CODE);;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Uri object will not be null for RESULT_OK
            licenseUri = data.getData();
            upload_license.setText("CHANGE LICENSE");
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void signUp()
    {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String cpassword = editCPassword.getText().toString().trim();
        String name = editName.getText().toString().trim();
        String specialization = doctor_specialization.getSelectedItem().toString();
        String mobile = editMobileNo.getText().toString().trim();
        String license_validity = date_license.getText().toString().trim();

        if(!password.equals(cpassword))
        {
            Toast.makeText(getApplicationContext(), "Passwords are not same.", Toast.LENGTH_SHORT).show();
        }else if(email.isEmpty() || password.isEmpty() || cpassword.isEmpty() || name.isEmpty() || mobile.isEmpty() || license_validity.isEmpty() || licenseUri == null)
        {
            Toast.makeText(getApplicationContext(), "Please fill all requirements.", Toast.LENGTH_SHORT).show();
        }else
        {
            signUpProgress.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
                        String uid = currentUser.getUid();
                        String imgName = uid + ".jpg";
                        final StorageReference imageRef = mImageStorage.child("license_image").child(uid).child(imgName);
                        imageRef.putFile(licenseUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    //Insert into Database
                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Doctors").child(uid);

                                    HashMap<String, Object> doctorMap = new HashMap<String, Object>();
                                    doctorMap.put("id", uid);
                                    doctorMap.put("name", name);
                                    doctorMap.put("specialization", specialization);
                                    doctorMap.put("mobile",mobile);
                                    doctorMap.put("imageUrl","Default");
                                    doctorMap.put("licenseUrl", imgName);
                                    doctorMap.put("licenseValidity", license_validity);
                                    doctorMap.put("online", false);
                                    mDatabase.setValue(doctorMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            signUpProgress.dismiss();
                                            if(task.isSuccessful())
                                            {
                                                startActivity(new Intent(DoctorSignUpActivity.this, DoctorHomeActivity.class));
                                                finish();
                                            }else
                                            {
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
                    }else
                    {
                        signUpProgress.dismiss();
                        Toast.makeText(getApplicationContext(),"Registration failed.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}