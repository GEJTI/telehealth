package com.example.telehealth.patient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.telehealth.DoctorDetailsActivity;
import com.example.telehealth.DoctorListActivity;
import com.example.telehealth.LoginActivity;
import com.example.telehealth.PatientProfileActivity;
import com.example.telehealth.PatientProfileSettingActivity;
import com.example.telehealth.R;
import com.example.telehealth.bottom_sheets.FeedbackBottomDialogFragment;
import com.example.telehealth.customfonts.MyTextView_Poppins_Medium;
import com.example.telehealth.doctor.DoctorSettingActivity;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.models.Doctors;
import com.example.telehealth.models.Feedback;
import com.example.telehealth.models.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class DoctorProfileActivity extends AppCompatActivity {

    FirebaseDatabase mDatabase;
    DatabaseReference dbRef, feedbackRef, doctorRef, userRef;

    RecyclerView displayFeedback;

    FirebaseRecyclerAdapter<Feedback, FeedbacksViewHolder> displayAdapter;
   // FirebaseRecyclerPagingAdapter<Feedback, FeedbacksViewHolder> allAdapter;

    TextView txtNoRecord;
    EditText editFeedback;
    Button btnSendFeedback;

    String doctorId;

    //Firebase
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile_patient_view);

        txtNoRecord = (TextView) findViewById(R.id.txtNoRecord);
        editFeedback = (EditText) findViewById(R.id.editFeedback);
        btnSendFeedback = findViewById(R.id.btnSendFeedback);

        Intent i = getIntent();
        doctorId = i.getStringExtra("doctor_id");

        mDatabase = FirebaseDatabase.getInstance();
        dbRef = mDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

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
        getSupportActionBar().setTitle("Doctor Profile");

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        feedbackRef = dbRef.child("Feedbacks").child(doctorId);
        doctorRef = dbRef.child("Doctors").child(doctorId);
        userRef = dbRef.child("Users");

        WrapContentLinearLayoutManager wrapContentLinearLayoutManager = new WrapContentLinearLayoutManager(this);
        wrapContentLinearLayoutManager.setReverseLayout(true);
        wrapContentLinearLayoutManager.setStackFromEnd(true);

        displayFeedback = (RecyclerView) findViewById(R.id.displayFeedback);
        displayFeedback.setHasFixedSize(true);
        displayFeedback.setLayoutManager(wrapContentLinearLayoutManager);

        loadDoctorInfo();
        fetchDisplayFeedback();

        btnSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedback = editFeedback.getText().toString().trim();
                if(feedback.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "No feedback entered", Toast.LENGTH_SHORT).show();
                    return;
                }
                long time = new Date().getTime();
                Map<String, Object> data = new HashMap<>();
                data.put("patient_id", currentUser.getUid());
                data.put("feedback", feedback);
                data.put("is_hidden", false);
                data.put("time_sent", time);
                data.put("inverse_timestamp", (-1 * time));
                feedbackRef.push().setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String message = !task.isSuccessful()? "Sending failed" : "Feedback sent!";
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        if(task.isSuccessful())
                        {
                            editFeedback.setText(null);
                        }
                    }
                });
            }
        });
    }

    public static class FeedbacksViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout root;
        public TextView patientName;
        public TextView feedback;
        public CircleImageView patientPhoto;

        public FeedbacksViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.list_root);
            patientName = (TextView) itemView.findViewById(R.id.patient);
            feedback = (TextView) itemView.findViewById(R.id.feedback);
            patientPhoto = (CircleImageView) itemView.findViewById(R.id.patient_photo);
        }

        public void setPatientName(String name){
            patientName.setText(name);
        }

        public void setFeedback(String sFeedback) {
            feedback.setText(sFeedback);
        }

        public void setPatientPhoto(String url, Context con, String uid){
            if(url.equals("Default")){
                patientPhoto.setImageResource(R.drawable.ic_person_black_24dp);
            }
            else {
                FirebaseStorageHelper.loadChatProfilePhoto(con, patientPhoto, uid, url, false);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        displayAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
//        if(allAdapter != null)
//        {
//
//            allAdapter.stopListening();
//        }
        displayAdapter.stopListening();
    }

    private void loadDoctorInfo()
    {
        CircleImageView doctorImg = findViewById(R.id.profile_image);
        TextView doctorName = findViewById(R.id.txtPatientName);
        TextView doctorSpecialization = findViewById(R.id.txtSpecialization);

        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Doctors doctor = snapshot.getValue(Doctors.class);
                String imageUrl = doctor.getImageUrl();
                if(imageUrl.equals("Default")){
                    doctorImg.setImageResource(R.drawable.ic_person_black_24dp);
                }
                else {
                    FirebaseStorageHelper.loadProfilePhoto(DoctorProfileActivity.this, doctorImg, doctorId, imageUrl, false);
                }
                doctorName.setText(doctor.getName());
                doctorSpecialization.setText(doctor.getSpecialization());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

//    private void showAllFeedbackDialog(){
//
//        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DoctorProfileActivity.this);
//        View mView = getLayoutInflater().inflate(R.layout.dialog_feedback,null);
//
//        ImageView imgClose = mView.findViewById(R.id.close);
//        SwipeRefreshLayout swipeRefreshLayout = mView.findViewById(R.id.swipe_refresh_layout);
//        RecyclerView feedbackList = mView.findViewById(R.id.feedbackList);
//
//        feedbackList.setHasFixedSize(true);
//
//        fetchAllFeedback();
//
//        allAdapter.addLoadStateListener(new Function1<CombinedLoadStates, Unit>() {
//            @Override
//            public Unit invoke(CombinedLoadStates states) {
//                LoadState refresh = states.getRefresh();
//                LoadState append = states.getAppend();
//
//                if (refresh instanceof LoadState.Error || append instanceof LoadState.Error) {
//                    // The previous load (either initial or additional) failed. Call
//                    // the retry() method in order to retry the load operation.
//                    // ...
//                    swipeRefreshLayout.setRefreshing(false);
//                    allAdapter.retry();
//                }
//
//                if (refresh instanceof LoadState.Loading) {
//                    // The initial Load has begun
//                    // ...
//                    Toast.makeText(getApplicationContext(), "initialLoad", Toast.LENGTH_SHORT).show();
//                    swipeRefreshLayout.setRefreshing(true);
//                }
//
//                if (append instanceof LoadState.Loading) {
//                    // The adapter has started to load an additional page
//                    // ...
//                    Toast.makeText(getApplicationContext(), "append", Toast.LENGTH_SHORT).show();
//
//                    swipeRefreshLayout.setRefreshing(true);
//                }
//
//                if (append instanceof LoadState.NotLoading) {
//                    LoadState.NotLoading notLoading = (LoadState.NotLoading) append;
//                    if (notLoading.getEndOfPaginationReached()) {
//                        swipeRefreshLayout.setRefreshing(false);
//                        //feedbackList.scrollToPosition(0);
//                        Toast.makeText(getApplicationContext(), "loading all completed", Toast.LENGTH_SHORT).show();
//
//                        // The adapter has finished loading all of the data set
//                        // ...
//                        return null;
//                    }
//
//                    if (refresh instanceof LoadState.NotLoading) {
//                        swipeRefreshLayout.setRefreshing(false);
//                        Toast.makeText(getApplicationContext(), "initinal or additional load completed", Toast.LENGTH_SHORT).show();
//
//                        //feedbackList.scrollToPosition(0);
//                        // The previous load (either initial or additional) completed
//                        // ...
//                        return null;
//                    }
//                }
//                return null;
//            }
//        });
//
//        feedbackList.setAdapter(allAdapter);
//
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
//
//
//        feedbackList.setLayoutManager(linearLayoutManager);
//
//
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                allAdapter.refresh();
//
//            }
//        });
//
//        mBuilder.setView(mView);
//        final AlertDialog dialog = mBuilder.create();
//        dialog.setCanceledOnTouchOutside(true);
//
//
//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialogInterface) {
//                Toast.makeText(getApplicationContext(), "show", Toast.LENGTH_SHORT).show();
//
//                allAdapter.startListening();
//            }
//        });
//
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                Toast.makeText(getApplicationContext(), "dismiss", Toast.LENGTH_SHORT).show();
//
//                if(allAdapter != null)
//                {
//                    allAdapter.stopListening();
//                }
//            }
//        });
//
//        imgClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//    }

    private void fetchDisplayFeedback()
    {
        Query query = feedbackRef.orderByChild("is_hidden").equalTo(false);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtNoRecord.setVisibility(snapshot.exists() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseRecyclerOptions<Feedback> options =
                new FirebaseRecyclerOptions.Builder<Feedback>()
                        .setQuery(query, Feedback.class)
                        .build();
        displayAdapter =
                new FirebaseRecyclerAdapter<Feedback, FeedbacksViewHolder>(options) {
                    @NonNull
                    @Override
                    public FeedbacksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        // Create the ItemViewHolder
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.patient_feeback_item, parent, false);

                        return new FeedbacksViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull FeedbacksViewHolder holder,
                                                    int position,
                                                    @NonNull Feedback feedback) {
                        // Bind the item to the view holder
                        userRef.child(feedback.getPatientId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    Users patient = snapshot.getValue(Users.class);
                                    holder.setPatientName(patient.getName());
                                    holder.setPatientPhoto(patient.getImage(), DoctorProfileActivity.this, feedback.getPatientId());
                                    holder.setFeedback(feedback.getFeedback());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                };
        displayFeedback.setAdapter(displayAdapter);
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
            finish(); // close this activity and return to preview activity (if there is any)
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_profile){
            startActivity(new Intent(DoctorProfileActivity.this, PatientProfileActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(DoctorProfileActivity.this, PatientProfileSettingActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_inbox){
            startActivity(new Intent(DoctorProfileActivity.this, PatientInboxActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            mAuth.signOut();
            startActivity(new Intent(DoctorProfileActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}