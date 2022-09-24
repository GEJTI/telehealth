package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.telehealth.customfonts.MyTextView_Poppins_Medium;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.models.Patient;
import com.example.telehealth.patient.PatientInboxActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientProfileActivity extends AppCompatActivity {

    public static final String TAG = "PatientProfile";
    private Toolbar mToolbar;
    String userName;
    String imageUrl;
    String mobile;
    String email;

    //Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Firebase Database
    // Write a message to the database
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

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

        //mProfilePicture imageview init
        final FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Patient patient = dataSnapshot.getValue(Patient.class);

                imageUrl = dataSnapshot.child("image").getValue().toString();
                email = currentUser.getEmail();

                MyTextView_Poppins_Medium mUserName = findViewById(R.id.profile_name);
                EditText mEmail = findViewById(R.id.profile_email);
                EditText mPhone = findViewById(R.id.profile_phone);
                EditText mBirthday = findViewById(R.id.profile_bday);
                EditText mAge = findViewById(R.id.profile_age);
                EditText mGender = findViewById(R.id.profile_gender);

                ImageView mValidId = findViewById(R.id.profile_valid_id);
                mUserName.setText(patient.getName());
                mEmail.setText(email);
                mBirthday.setText(patient.getBirthday());
                mAge.setText(patient.getAge());
                mGender.setText(patient.getGender());
                mPhone.setText(patient.getMobile());

                CircleImageView mProfileImage = (CircleImageView) findViewById(R.id.profile_image);

                if(imageUrl.equals("Default")){
                    mProfileImage.setImageResource(R.drawable.ic_person_black_24dp);
                }
                else {
                    FirebaseStorageHelper.loadProfilePhoto(PatientProfileActivity.this, mProfileImage, uid, imageUrl, false);
                }

                FirebaseStorageHelper.asyncValidIdPhoto(PatientProfileActivity.this, mValidId, uid, false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.patient_menu_bar,menu);

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
//            startActivity(new Intent(PatientProfileActivity.this, PatientProfileActivity.class));
//            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(PatientProfileActivity.this, PatientProfileSettingActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_inbox){
            startActivity(new Intent(PatientProfileActivity.this, PatientInboxActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            mAuth.signOut();
            startActivity(new Intent(PatientProfileActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }

}
