package com.example.telehealth.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.telehealth.LoginActivity;
import com.example.telehealth.PatientHomeActivity;
import com.example.telehealth.R;
import com.example.telehealth.adapters.MessageAdapter;
import com.example.telehealth.adapters.doctor.CheckUpHistoryAdapter;
import com.example.telehealth.models.Message;
import com.example.telehealth.models.doctor.MedicalCheckup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class CheckUpHistoryActivity extends AppCompatActivity {

    public static final String TAG="CheckUpHistoryActivity";
    private Toolbar mToolbar;

    //RecyclerView
    RecyclerView recyclerView;
    List<MedicalCheckup> medicalCheckupList = new ArrayList<>();
    public LinearLayoutManager mLinearLayout;
    public CheckUpHistoryAdapter mAdapter;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // database
    public DatabaseReference mDatabase, mUserRef;
    public Query query;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_up_history);

        recyclerView = (RecyclerView) findViewById(R.id.checkup_list_recycler);

        //Firebase init
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        //RecyclerView

        mAdapter = new CheckUpHistoryAdapter(this, medicalCheckupList);
        mLinearLayout = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLinearLayout);
        loadHistory();
        recyclerView.setAdapter(mAdapter);

        //Toolbar initialization
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Patient Checkup History");

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Doctors").child(currentUserId);

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
    }

    //For getting message from database
    public void loadHistory(){

        mAuth = FirebaseAuth.getInstance();
        query = mDatabase.child("MedicalCheckups").child(currentUserId);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MedicalCheckup medicalCheckup = dataSnapshot.getValue(MedicalCheckup.class);

                medicalCheckupList.add(medicalCheckup);
                mAdapter.notifyDataSetChanged();
//                mAdapter.notifyItemInserted(mMessageList.size() - 1);
//                //For showing the last message in view
//                mMessageView.scrollToPosition(mMessageList.size()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
            startActivity(new Intent(CheckUpHistoryActivity.this, DoctorProfileActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(CheckUpHistoryActivity.this, DoctorSettingActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_history){
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            FirebaseDatabase.getInstance().goOffline();
            mAuth.signOut();
            startActivity(new Intent(CheckUpHistoryActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}