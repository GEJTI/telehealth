package com.example.telehealth;

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
import android.widget.Toast;

import com.example.telehealth.adapters.doctor.DoctorMessageListAdapter;
import com.example.telehealth.doctor.CheckUpHistoryActivity;
import com.example.telehealth.doctor.DoctorPrescriptionListActivity;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DoctorHomeActivity extends AppCompatActivity {

    public static final String TAG="DoctorHome";
    private Toolbar mToolbar;

    //RecyclerView
    RecyclerView mMessageView;
    ArrayList<Message> mMessageList = new ArrayList<>();
    public LinearLayoutManager mLinearLayout;
    public DoctorMessageListAdapter mAdapter;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // database
    private DatabaseReference mUserRef,mDatabase;
    private Query query;

    public String doctorId;

    HashMap<String, Integer> addedPatientId;

    ChildEventListener childEventListener, childEventListener1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_home);

        //Firebase init
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();

        //Firebase init
        //Firebase Auth init
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        doctorId = mAuth.getCurrentUser().getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Doctors").child(doctorId);


//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        final DatabaseReference connectedRef = database.getReference(".info/connected");
//        DatabaseReference presenceRef = FirebaseDatabase.getInstance().getReference("presence/" + doctorId);
//        connectedRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists())
//                {
//                    presenceRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
//                    presenceRef.setValue(true);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });


//        mUserRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Toast.makeText(getApplicationContext(), "tests", Toast.LENGTH_SHORT).show();
//                if(dataSnapshot != null) {
//                    //For checking isOnline
//                    mUserRef.child("online").onDisconnect().setValue(false);
//                    mUserRef.child("online").setValue(true);
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

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
        getSupportActionBar().setTitle("Home");

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //RecyclerView

        addedPatientId = new HashMap<>();

        mAdapter = new DoctorMessageListAdapter(this,mMessageList);
        mMessageView = (RecyclerView) findViewById(R.id.doctor_message_list_recycler);
        mLinearLayout = new LinearLayoutManager(this);
        mMessageView.setHasFixedSize(true);
        mMessageView.setLayoutManager(mLinearLayout);
        msgInfo();
        mMessageView.setAdapter(mAdapter);

        setPresence();

    }

    public void msgInfo(){

        mAuth = FirebaseAuth.getInstance();
        final List<String> ptId = new ArrayList<>();
        final String dId = mAuth.getCurrentUser().getUid();

        final DatabaseReference retriveId = FirebaseDatabase.getInstance().getReference().child("Chat");
        retriveId.child(dId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("key:",dataSnapshot.getKey());
                String ss = String.valueOf(dataSnapshot.getKey());
                final String dId = mAuth.getCurrentUser().getUid();

                DatabaseReference retriveMsg = FirebaseDatabase.getInstance().getReference()
                        .child("Message").child(dId).child(ss);
                query = retriveMsg.limitToLast(1);
                query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Message message = dataSnapshot.getValue(Message.class);

                        String from = message.getFrom();
                        String patientId = (from.equals(dId))? message.getTo() : from;

                        if(!addedPatientId.containsKey(patientId))
                        {
                            mMessageList.add(message);
                            int index = mMessageList.size() - 1;
                            addedPatientId.put(patientId, index);
                            mAdapter.notifyItemInserted(index);
                        }else{
                            int index = addedPatientId.get(patientId);
                            mMessageList.set(index, message);
                            mAdapter.notifyItemChanged(index);
                        }

//                        startActivity(new Intent(getApplicationContext(),DoctorHome.class));
//                        finish();
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

    private void setPresence()
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = database.getReference("/Doctors/"+ doctorId +"/lastOnline");
        final DatabaseReference onlineRef = database.getReference("/Doctors/"+ doctorId +"/online");

        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {

                    // When I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    onlineRef.onDisconnect().setValue(false);

                    onlineRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Listener was cancelled at .info/connected");
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

            //finish(); // close this activity and return to preview activity (if there is any)
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_profile){
            startActivity(new Intent(DoctorHomeActivity.this, DoctorProfileActivity.class));
           // finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(DoctorHomeActivity.this, DoctorSettingActivity.class));
           // finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_history){
            startActivity(new Intent(DoctorHomeActivity.this, CheckUpHistoryActivity.class));
            //finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            FirebaseDatabase.getInstance().goOffline();
            mAuth.signOut();
            startActivity(new Intent(DoctorHomeActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}