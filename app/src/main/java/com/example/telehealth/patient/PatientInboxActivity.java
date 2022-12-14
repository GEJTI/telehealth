package com.example.telehealth.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.telehealth.LoginActivity;
import com.example.telehealth.PatientHomeActivity;
import com.example.telehealth.PatientProfileActivity;
import com.example.telehealth.PatientProfileSettingActivity;
import com.example.telehealth.R;
import com.example.telehealth.adapters.patient.PatientInboxListAdapter;
import com.example.telehealth.doctor.DoctorProfileActivity;
import com.example.telehealth.doctor.DoctorSettingActivity;
import com.example.telehealth.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatientInboxActivity extends AppCompatActivity {

    public static final String TAG="PatientInbox";
    private Toolbar mToolbar;

    //RecyclerView
    RecyclerView mMessageView;
    ArrayList<Message> mMessageList = new ArrayList<>();
    public LinearLayoutManager mLinearLayout;
    public PatientInboxListAdapter mAdapter;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // database
    private DatabaseReference mUserRef,mDatabase;
    private Query query;

    public String patientId;

    HashMap<String, Integer> addedDoctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_inbox);

        //Firebase init
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();

        //Firebase init
        //Firebase Auth init
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        patientId = mAuth.getCurrentUser().getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(patientId);

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

        //Toolbar initialization
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Inbox");

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        addedDoctorId = new HashMap<>();
        //RecyclerView
        mAdapter = new PatientInboxListAdapter(this,mMessageList);
        mMessageView = (RecyclerView) findViewById(R.id.patient_message_list_recycler);
        mLinearLayout = new LinearLayoutManager(this);
        mMessageView.setHasFixedSize(true);
        mMessageView.setLayoutManager(mLinearLayout);
        msgInfo();
        mMessageView.setAdapter(mAdapter);
    }

    public void msgInfo(){

        mAuth = FirebaseAuth.getInstance();
        final List<String> ptId = new ArrayList<>();
        final String pId = mAuth.getCurrentUser().getUid();

        final DatabaseReference retriveId = FirebaseDatabase.getInstance().getReference().child("Chat");
        retriveId.child(pId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("key:",dataSnapshot.getKey());
                String ss = String.valueOf(dataSnapshot.getKey());
                final String pId = mAuth.getCurrentUser().getUid();

                DatabaseReference retriveMsg = FirebaseDatabase.getInstance().getReference()
                        .child("Message").child(pId).child(ss);
                query = retriveMsg.limitToLast(1);
                query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Message message = dataSnapshot.getValue(Message.class);

                        String from = message.getFrom();
                        String doctorId = (from.equals(pId))? message.getTo() : from;

                        if(!addedDoctorId.containsKey(doctorId))
                        {
                            mMessageList.add(message);
                            int index = mMessageList.size() - 1;
                            addedDoctorId.put(doctorId, index);
                            mAdapter.notifyItemInserted(index);
                        }else{
                            int index = addedDoctorId.get(doctorId);
                            mMessageList.set(index, message);
                            mAdapter.notifyItemChanged(index);
                        }

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
            startActivity(new Intent(PatientInboxActivity.this, PatientProfileActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(PatientInboxActivity.this, PatientProfileSettingActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_inbox){
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            mAuth.signOut();
            startActivity(new Intent(PatientInboxActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}