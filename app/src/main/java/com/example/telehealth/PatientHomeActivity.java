package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.telehealth.adapters.PatientHomeViewAdapter;
import com.example.telehealth.models.PatientHomeData;
import com.example.telehealth.patient.PatientInboxActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class PatientHomeActivity extends AppCompatActivity {

    public static final String TAG="PatientHome";
    private Toolbar mToolbar;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        //init RecyclerView
        initRecyclerViewsForPatient();

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

        //Toolbar initialization
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
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

    private void backToQuestion()
    {
        startActivity(new Intent(PatientHomeActivity.this, PatientCheckUpQuestion.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        backToQuestion();
    }

    public void initRecyclerViewsForPatient(){
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.patient_home_recycler);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<PatientHomeData> list = new ArrayList<>();
        list.add(new PatientHomeData("Cardiologist",R.drawable.ekg_2069872_640));
        list.add(new PatientHomeData("Neurologist",R.drawable.brain_1710293_640));

        list.add(new PatientHomeData("Oncologist",R.drawable.cancer));
        list.add(new PatientHomeData("Pathologist",R.drawable.boy_1299626_640));
        list.add(new PatientHomeData("Hematologist",R.drawable.virus_1812092_640));
        list.add(new PatientHomeData("Dermatologist",R.drawable.skin));

        PatientHomeViewAdapter adapter = new PatientHomeViewAdapter(getApplicationContext(),list);
        recyclerView.setAdapter(adapter);

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
            backToQuestion();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_profile){
            startActivity(new Intent(PatientHomeActivity.this, PatientProfileActivity.class));
           // finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(PatientHomeActivity.this, PatientProfileSettingActivity.class));
           // finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_inbox){
            startActivity(new Intent(PatientHomeActivity.this, PatientInboxActivity.class));
           // finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            mAuth.signOut();
            startActivity(new Intent(PatientHomeActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}