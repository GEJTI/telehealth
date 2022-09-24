package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.telehealth.bottom_sheets.FeedbackBottomDialogFragment;
import com.example.telehealth.doctor.DoctorSettingActivity;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.helpers.GlideHelper;
import com.example.telehealth.patient.PatientInboxActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorDetailsActivity extends AppCompatActivity {

    public static final String TAG="DoctorDeatails";
    private Toolbar mToolbar;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // database
    private DatabaseReference mDatabase;
    private Query query;

    public String ctg;
    public String name;
    public String imageUrl;
    public String doctorId;

    //Widget
    CircleImageView profileImage;
    TextView doctorName,doctorCategory;
    Button  showDoctorLicense, showFeedbacks;
    ImageView chatSymbol;

    private final String MyPREFERENCES = "CheckUpData";

    SharedPreferences sharedpreferences;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_details);

        Intent i = getIntent();
        name = i.getStringExtra("doctor_name");
        ctg = i.getStringExtra("doctor_specialization");
        imageUrl = i.getStringExtra("doctor_image");
        doctorId = i.getStringExtra("doctor_id");

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        //Widget init
        profileImage = (CircleImageView) findViewById(R.id.doctor_details_profile_image);
        doctorName = (TextView) findViewById(R.id.doctor_details_name);
        doctorCategory = (TextView) findViewById(R.id.doctor_details_category);
        chatSymbol = (ImageView) findViewById(R.id.doctor_details_message);
        showDoctorLicense = (Button) findViewById(R.id.show_doctor_license);
        showFeedbacks = (Button) findViewById(R.id.show_feedbacks);


        if(imageUrl.equals("Default")){
            profileImage.setImageResource(R.drawable.ic_person_black_24dp);
        }
        else {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profile_image").child(doctorId).child(imageUrl);
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    GlideHelper.loadThumbnail(DoctorDetailsActivity.this, profileImage, uri);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }

        //Picasso.with(getApplicationContext()).load(imageUrl).placeholder(R.drawable.ic_person_black_24dp).into(profileImage);
        doctorName.setText(name);
        doctorCategory.setText(ctg);

        //mProfilePicture imageview init
        final FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        //Firebase init
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("TAG", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("TAG", "onAuthStateChanged:signed_out");
                }
            }
        };

        //Toolbar initialization
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Doctor info");

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        chatSymbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map sharePrefData = sharedpreferences.getAll();

                if(sharePrefData.size() <= 0)
                {
                    gotoChat();
                    return;
                }

                final FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
                String uid = currentUser.getUid();

                DatabaseReference medical_checkup_push = mDatabase.child("MedicalCheckups")
                        .child(uid).push();
                String pushKey = medical_checkup_push.getKey();

                String current_user_ref = "MedicalCheckups/" + uid;
                String doctor_ref = "MedicalCheckups/" + doctorId;

                Map patientValMap = new HashMap();
                patientValMap.put("doctor_id", doctorId);
                patientValMap.put("condition", sharePrefData.get("condition"));
                patientValMap.put("date_of_checkup",sharePrefData.get("date_of_checkup"));
                patientValMap.put("time",ServerValue.TIMESTAMP);

                Map doctorValMap = new HashMap();
                doctorValMap.put("patient_id", uid);
                doctorValMap.put("condition", sharePrefData.get("condition"));
                doctorValMap.put("date_of_checkup",sharePrefData.get("date_of_checkup"));
                doctorValMap.put("time",ServerValue.TIMESTAMP);

                Map medicalCheckUpMap = new HashMap();
                medicalCheckUpMap.put(current_user_ref +"/" + pushKey, patientValMap);
                medicalCheckUpMap.put(doctor_ref + "/" + pushKey, doctorValMap);

                progressDialog.show();
                mDatabase.updateChildren(medicalCheckUpMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        progressDialog.dismiss();
                        if(databaseError != null){
                            Log.d(TAG,"Message sending failed for, database failure.");
                        }else{
                            sharedpreferences.edit().clear().apply();
                            gotoChat();
                        }
                    }
                });
            }
        });

        showDoctorLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLicense();
            }
        });

        showFeedbacks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFeedbacks();
            }
        });
    }

    private void showLicense()
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DoctorDetailsActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_doctor_license,null);

        ImageView imgLicense = mView.findViewById(R.id.imgLicense);
        FirebaseStorageHelper.asyncLicensePhoto(this, imgLicense, doctorId, false);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void showFeedbacks()
    {
        FeedbackBottomDialogFragment feedbackBottomDialogFragment = new FeedbackBottomDialogFragment(doctorId);
        feedbackBottomDialogFragment.show(getSupportFragmentManager(), feedbackBottomDialogFragment.getTag());
    }

    private void gotoChat()
    {
        Intent msgActivity = new Intent(getApplicationContext(), ChatActivity.class);
        msgActivity.putExtra("doctor_name",name);
        msgActivity.putExtra("doctor_id",doctorId);
        msgActivity.putExtra("from_activity", "DoctorDetailsActivity");
        msgActivity.putExtra("doctor_specialization", ctg);
        msgActivity.putExtra("doctor_image", imageUrl);
        startActivity(msgActivity);
        finish();
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
        } else if (item.getItemId() == R.id.main_menu_profile) {
            startActivity(new Intent(DoctorDetailsActivity.this, PatientProfileActivity.class));
           // finish();
            return true;
        } else if (item.getItemId() == R.id.main_menu_setting) {
            startActivity(new Intent(DoctorDetailsActivity.this, PatientProfileSettingActivity.class));
          //  finish();
            return true;
        }else if(item.getItemId()==R.id.main_menu_inbox){
            startActivity(new Intent(DoctorDetailsActivity.this, PatientInboxActivity.class));
            //finish();
            return true;
        } else if (item.getItemId() == R.id.main_menu_logout) {
            mAuth.signOut();
            startActivity(new Intent(DoctorDetailsActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}