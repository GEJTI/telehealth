package com.example.telehealth.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.telehealth.LoginActivity;
import com.example.telehealth.PatientHomeActivity;
import com.example.telehealth.PatientProfileActivity;
import com.example.telehealth.R;
import com.example.telehealth.bottom_sheets.FeedbackBottomDialogFragment;
import com.example.telehealth.customfonts.EditText_Poppins_Regular;
import com.example.telehealth.customfonts.MyTextView_Poppins_Medium;
import com.example.telehealth.customfonts.MyTextView_Poppins_Regular;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.models.Doctors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorProfileActivity extends AppCompatActivity {

    public static final String TAG = "DoctorProfile";
    private Toolbar mToolbar;
    String userName;
    String imageUrl;
    String specialization;
    String email;
    String licenseUrl;
    String licenseValidity;
    String mobile;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // database
    private DatabaseReference mUserRef,mDatabase;
    private Query query;

    public String doctorId;

    Button btnShowFeedbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        //Toolbar initialization
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Firebase init
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();

        //Firebase init
        //Firebase Auth init
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        doctorId = mAuth.getCurrentUser().getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Doctors").child(doctorId);

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

        btnShowFeedbacks = (Button) findViewById(R.id.show_feedbacks);
        btnShowFeedbacks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFeedbacks();
            }
        });

        //mProfilePicture imageview init
        final FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Doctors").child(uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Doctors doctor = dataSnapshot.getValue(Doctors.class);
                email = currentUser.getEmail();

                TextView mUserName = findViewById(R.id.doctor_details_name);
                EditText_Poppins_Regular mEmail = findViewById(R.id.profile_email);
                EditText_Poppins_Regular mSpecialization = findViewById(R.id.doctor_details_category);
                MyTextView_Poppins_Medium mLicenseValidity = findViewById(R.id.profile_license_validity);
                EditText_Poppins_Regular mPhone = findViewById(R.id.profile_phone);
                ImageView mLicense = findViewById(R.id.profile_license);

                mUserName.setText(doctor.getName());
                mEmail.setText(email);
                mSpecialization.setText(doctor.getSpecialization());
                mLicenseValidity.setText("License Validity\n " + doctor.getLicenseValidity());
                mPhone.setText(doctor.getMobile());

                CircleImageView mProfileImage = (CircleImageView) findViewById(R.id.profile_image);

                FirebaseStorageHelper.asyncLicensePhoto(DoctorProfileActivity.this, mLicense, uid, false);

                if(doctor.getImageUrl().equals("Default")){
                    mProfileImage.setImageResource(R.drawable.ic_person_black_24dp);
                }
                else {
                    FirebaseStorageHelper.loadProfilePhoto(DoctorProfileActivity.this, mProfileImage, uid, doctor.getImageUrl(), false);
                    //Picasso.with(getApplicationContext()).load(imageUrl).into(mProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showFeedbacks()
    {
        FeedbackBottomDialogFragment feedbackBottomDialogFragment = new FeedbackBottomDialogFragment(doctorId);
        feedbackBottomDialogFragment.show(getSupportFragmentManager(), feedbackBottomDialogFragment.getTag());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_bar_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_profile){
//            startActivity(new Intent(DoctorProfile.this, DoctorProfile.class));
//            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(DoctorProfileActivity.this, DoctorSettingActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_history){
           startActivity(new Intent(DoctorProfileActivity.this, CheckUpHistoryActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            FirebaseDatabase.getInstance().goOffline();
            mAuth.signOut();
            startActivity(new Intent(DoctorProfileActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}
