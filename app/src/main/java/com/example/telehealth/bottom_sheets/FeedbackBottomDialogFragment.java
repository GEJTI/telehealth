package com.example.telehealth.bottom_sheets;

import android.content.Context;
import android.os.Bundle;
import android.text.BoringLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehealth.DoctorListActivity;
import com.example.telehealth.R;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.models.Feedback;
import com.example.telehealth.models.Users;
import com.example.telehealth.patient.DoctorProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedbackBottomDialogFragment extends BottomSheetDialogFragment {

    private String doctorId;

    public FeedbackBottomDialogFragment(String doctorId) {
        this.doctorId = doctorId;
    }

    private FirebaseDatabase mDatabase;
    private DatabaseReference dbRef, feedbackRef, userRef;
    private FirebaseRecyclerAdapter adapter;

    private FirebaseUser currentUser;
    private  FirebaseAuth mAuth;

    RecyclerView feedbackList;
    TextView txtNoRecord;

    Boolean isDoctor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance();
        dbRef = mDatabase.getReference();

        feedbackRef = dbRef.child("Feedbacks").child(doctorId);
        userRef = dbRef.child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        isDoctor = doctorId == currentUser.getUid();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_feedback, container, false);

        feedbackList = view.findViewById(R.id.feedbackList);
        txtNoRecord = view.findViewById(R.id.txtNoRecord);
        ImageView close = view.findViewById(R.id.close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        setAdapter();

        return view;
    }

    public static class FeedbacksViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout root;
        public TextView patientName;
        public TextView feedback;
        public CircleImageView patientPhoto;
        public TextView txtHiddenMsg;
        public MaterialButton toggleVisibility;

        public FeedbacksViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.list_root);
            patientName = (TextView) itemView.findViewById(R.id.patient);
            feedback = (TextView) itemView.findViewById(R.id.feedback);
            patientPhoto = (CircleImageView) itemView.findViewById(R.id.patient_photo);
            txtHiddenMsg = (TextView) itemView.findViewById(R.id.txtHiddenMsg);
            toggleVisibility = (MaterialButton) itemView.findViewById(R.id.toggleVisibility);
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

    private void setAdapter()
    {
        WrapContentLinearLayoutManager wrapContentLinearLayoutManager = new WrapContentLinearLayoutManager(getContext());
        if(!isDoctor)
        {
            wrapContentLinearLayoutManager.setReverseLayout(true);
            wrapContentLinearLayoutManager.setStackFromEnd(true);
        }

        feedbackList.setHasFixedSize(true);
        feedbackList.setLayoutManager(wrapContentLinearLayoutManager);

        Query query = isDoctor? feedbackRef.orderByChild("inverse_timestamp") : feedbackRef.orderByChild("is_hidden").equalTo(false);

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
        adapter =
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
                        DatabaseReference feedbackRefByPos = getRef(position);
                        // Bind the item to the view holder
                        userRef.child(feedback.getPatientId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    Users patient = snapshot.getValue(Users.class);
                                    holder.setPatientName(patient.getName());
                                    holder.setPatientPhoto(patient.getImage(), getContext(), feedback.getPatientId());
                                    holder.setFeedback(feedback.getFeedback());
                                    holder.toggleVisibility.setVisibility(isDoctor? View.VISIBLE : View.GONE);
                                    if(isDoctor)
                                    {
                                        boolean isHidden = feedback.getIsHidden();
                                        holder.toggleVisibility.setIconResource(isHidden? R.drawable.eye_open : R.drawable.eye_close);
                                        holder.toggleVisibility.setText(isHidden? "SHOW" : "HIDE");
                                        holder.txtHiddenMsg.setVisibility(isHidden? View.VISIBLE : View.GONE);
                                        holder.toggleVisibility.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                boolean hiddenVal = !isHidden;
                                                feedbackRefByPos.child("is_hidden").setValue(hiddenVal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(getContext(), task.isSuccessful()? (hiddenVal? "Feedback hidden" : "Feedback unhidden.") : "Failed.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
//                                                feedbackRefByPos.addListenerForSingleValueEvent(new ValueEventListener() {
//                                                    @Override
//                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                        Toast.makeText(getContext(), snapshot.getKey(), Toast.LENGTH_SHORT).show();
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                                    }
//                                                });
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                };

        feedbackList.setAdapter(adapter);
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
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
