package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.models.Doctors;
import com.example.telehealth.models.Users;
import com.example.telehealth.patient.PatientInboxActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorListActivity extends AppCompatActivity {

    public static final String TAG = "DoctorList";
    private Toolbar mToolbar;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // database
    private DatabaseReference mDatabase;
    private Query query;

    public String ctg, fromActivity;

    //RecyclerView
    private RecyclerView mDoctorList;
    private TextView txtNoRecord;

    private FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        txtNoRecord = (TextView) findViewById(R.id.txtNoRecord);

        Intent i = getIntent();
        ctg = i.getStringExtra("doctor_specialization");
        fromActivity = i.getStringExtra("fromActivity");

        //mProfilePicture imageview init
        final FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        //Retrive from database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Doctors");

        query = mDatabase.orderByChild("specialization").equalTo(ctg);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtNoRecord.setText("No " + ctg.toLowerCase() + " found.");
                txtNoRecord.setVisibility(snapshot.exists() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
        getSupportActionBar().setTitle(ctg + "s");

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Recycler view set up for doctor list
        mDoctorList = (RecyclerView) findViewById(R.id.doctor_list_recycler);
        mDoctorList.setHasFixedSize(true);
        mDoctorList.setLayoutManager(new WrapContentLinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        fetch();
    }

    private void backToActivity()
    {
        Class backToClass = fromActivity.equals("PatientCheckupQuestion")? PatientCheckUpQuestion.class : PatientHomeActivity.class;
        startActivity(new Intent(DoctorListActivity.this, backToClass));
        finish();
    }

    @Override
    public void onBackPressed() {
       backToActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
//
//        FirebaseRecyclerAdapter<Doctors,DoctorsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Doctors, DoctorsViewHolder>(
//                Doctors.class,
//                R.layout.doctor_online_list,
//                DoctorsViewHolder.class,
//                query
//        ) {
//            @Override
//            protected void populateViewHolder(DoctorsViewHolder viewHolder, final Doctors doctor, int position) {
//
//
//                viewHolder.setName(doctor.getName());
//                viewHolder.setCategory(doctor.getSpecialization());
//                viewHolder.setImage(doctor.getImageUrl(), DoctorListActivity.this, doctor.getId());
//                viewHolder.showOnline(doctor.getOnline(), doctor.getAvailability());
//
//                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent i = new Intent(DoctorListActivity.this, DoctorDetailsActivity.class);
//                        i.putExtra("doctor_name",doctor.getName());
//                        i.putExtra("doctor_specialization",doctor.getSpecialization());
//                        i.putExtra("doctor_image",doctor.getImageUrl());
//                        i.putExtra("doctor_id",doctor.getId());
//
//                        startActivity(i);
//                    }
//                });
//
//            }
//        };
//        mDoctorList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void fetch()
    {

        FirebaseRecyclerOptions<Doctors> options =
                new FirebaseRecyclerOptions.Builder<Doctors>()
                        .setQuery(query, Doctors.class)
//                        .setQuery(query, new SnapshotParser<Doctors>() {
//                            @NonNull
//                            @Override
//                            public Doctors parseSnapshot(@NonNull DataSnapshot snapshot) {
//                                final Doctors doctors = snapshot.getValue(Doctors.class);
//                                return doctors;
//                            }
//                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Doctors, DoctorsViewHolder>(options) {
            @Override
            public DoctorsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.doctor_online_list, parent, false);

                return new DoctorsViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(DoctorsViewHolder holder, final int position, Doctors doctor) {
                holder.setName(doctor.getName());
                holder.setCategory(doctor.getSpecialization());
                holder.setImage(doctor.getImageUrl(), DoctorListActivity.this, doctor.getId());
                holder.showOnline(doctor.getOnline(), doctor.getAvailability());
                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(DoctorListActivity.this, DoctorDetailsActivity.class);
                        i.putExtra("doctor_name",doctor.getName());
                        i.putExtra("doctor_specialization",doctor.getSpecialization());
                        i.putExtra("doctor_image",doctor.getImageUrl());
                        i.putExtra("doctor_id",doctor.getId());

                        startActivity(i);
                    }
                });

            }

        };
        mDoctorList.setAdapter(adapter);
    }

    public class DoctorsViewHolder extends RecyclerView.ViewHolder{

        public LinearLayout root;
        public  TextView mDoctorName;
        public TextView mCategory;
        public CircleImageView mDoctorImage;
        public  ImageView onlineImage;

        public DoctorsViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.list_root);
            mDoctorName = (TextView) itemView.findViewById(R.id.doctor_name);
            mCategory = (TextView) itemView.findViewById(R.id.doctor_category);
            mDoctorImage = (CircleImageView) itemView.findViewById(R.id.doctor_image);
            onlineImage = (ImageView) itemView.findViewById(R.id.online_image);
        }

        public void setName(String name){
            mDoctorName.setText(name);
        }

        public void setCategory(String ctg){
            mCategory.setText(ctg);
        }

        public void setImage(String url, Context con, String uid){
            if(url.equals("Default")){
                mDoctorImage.setImageResource(R.drawable.ic_person_black_24dp);
            }
            else {
                FirebaseStorageHelper.loadChatProfilePhoto(con, mDoctorImage, uid, url, false);
//                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profile_image").child(uid).child(url);
//                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        // Got the download URL for 'users/me/profile.png'
//                        GlideHelper.loadChatProfileImage(con, mDoctorImage, uri);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        // Handle any errors
//                    }
//                });
            }
            //Picasso.with(con).load(url).placeholder(R.drawable.ic_person_black_24dp).into(mDoctorImage);
        }

        public void showOnline(Boolean isOnline, String availability){
            if(isOnline){
                onlineImage.setImageResource(availability.equals("Available")? R.drawable.online : R.drawable.busy);
            }else{
                onlineImage.setImageResource(R.drawable.offline_gray);
            }
        }
    }

    public class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("TAG", "meet a IOOBE in RecyclerView");
            }
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
            backToActivity(); // close this activity and return to preview activity (if there is any)
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_profile){
            startActivity(new Intent(DoctorListActivity.this, PatientProfileActivity.class));
            //finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(DoctorListActivity.this, PatientProfileSettingActivity.class));
            //finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_inbox){
            startActivity(new Intent(DoctorListActivity.this, PatientInboxActivity.class));
            //finish();
            return true;
         }
        else if(item.getItemId()==R.id.main_menu_logout){
            mAuth.signOut();
            startActivity(new Intent(DoctorListActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}