package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.telehealth.customfonts.MyTextView_Poppins_Medium;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    public static final String TAG="LogInActivity";
    //EditText
    EditText mEmail,mPassword;
    //Button
    MyTextView_Poppins_Medium mSignIn;
    TextView mSignUp,sign_up_doctor;

    //Progress dialog
    ProgressDialog signInprogress;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = (EditText) findViewById(R.id.email_for_sign_in);
        mPassword = (EditText) findViewById(R.id.pass_for_sign_in);
        sign_up_doctor = findViewById(R.id.sign_up_doctor);
        //Button iinit
        mSignIn = findViewById(R.id.sign_in_button);
        mSignUp =  findViewById(R.id.sign_up_button);
        signInprogress = new ProgressDialog(this);

        FirebaseDatabase.getInstance().goOnline();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Here we are checking log in sessio
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    // startActivity(new Intent(PatientSignInActivity.this,PatientHome.class));
                    // finish();
                    //Toast.makeText(getApplicationContext(), "Signed In", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //Sign In Button onClick method
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please fill all requirements",Toast.LENGTH_SHORT).show();
                }
                else {
                    signInprogress.setTitle("Sign In process");
                    signInprogress.setMessage("Please wait for a while");
                    signInprogress.setCanceledOnTouchOutside(false);
                    signInprogress.show();
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                signInprogress.dismiss();
                                String currentUserId = mAuth.getCurrentUser().getUid();
                                mDatabase.child("Doctors").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Class gotoClass = DoctorHomeActivity.class;
                                        if(!snapshot.exists())
                                        {
                                            gotoClass = PatientCheckUpActivity.class;
                                        }
                                        startActivity(new Intent(LoginActivity.this, gotoClass));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                //Log.d("sucess", "successLogin");
                                //startActivity(new Intent(PatientSignInActivity.this, PatientHomeActivity.class));
                                //finish();

                            }
                            else {
                                Toast.makeText(getApplicationContext(),"Please enter correct email and password",Toast.LENGTH_SHORT).show();
                                signInprogress.dismiss();
                            }
                        }
                    });
                }

            }
        });

        //Sign Up Button onClick method
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, PatientSignUpActivity.class));
                finish();
            }
        });

        sign_up_doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, DoctorSignUpActivity.class));
                finish();
            }
        });
    }

    public void gotoForgotPasswordActivity(View view)
    {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}