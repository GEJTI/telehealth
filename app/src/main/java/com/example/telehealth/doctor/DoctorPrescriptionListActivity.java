package com.example.telehealth.doctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.telehealth.DoctorHomeActivity;
import com.example.telehealth.LoginActivity;
import com.example.telehealth.R;
import com.example.telehealth.adapters.PrescriptionListAdapter;
import com.example.telehealth.models.Prescription;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DoctorPrescriptionListActivity extends AppCompatActivity {

    public static final String TAG="DoctorPrescriptionList";
    private Toolbar mToolbar;

    private ArrayList<Prescription> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private PrescriptionListAdapter mAdapter;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Firbase storage
    private StorageReference mImageStorage;
    // database
    private DatabaseReference mUserRef,mDatabase;
    private Query query;

    String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_prescription_list);

        //Toolbar initialization
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Doctor's suggesion");

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
        uId = mAuth.getCurrentUser().getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);

        //RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.prescription_list);

        getData();

        mAdapter = new PrescriptionListAdapter(this,list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_bar_menu,menu);

        return true;
    }

    private void getData() {
        mAuth = FirebaseAuth.getInstance();

        String patientId = mAuth.getCurrentUser().getUid();

        DatabaseReference retriveMessae = FirebaseDatabase.getInstance().getReference().child("Prescription").child(patientId);
        Query msgQuery = retriveMessae;

        //  Toast.makeText(getApplicationContext(),patientId,Toast.LENGTH_SHORT).show();

        msgQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Prescription p = dataSnapshot.getValue(Prescription.class);

                list.add(p);
                mAdapter.notifyDataSetChanged();
                //For showing the last message in view
                // mMessageView.scrollToPosition(mMessageList.size()-1);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(DoctorPrescriptionListActivity.this, DoctorHomeActivity.class));
            finish(); // close this activity and return to preview activity (if there is any)
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_profile){
            //startActivity(new Intent(DoctorPrescriptionListActivity.this, DoctorProfile.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(DoctorPrescriptionListActivity.this, DoctorSettingActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if(currentUser == null){

            }
            else {
                mUserRef.child("online").setValue(false);
            }
            mAuth.signOut();
            startActivity(new Intent(DoctorPrescriptionListActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }

}
