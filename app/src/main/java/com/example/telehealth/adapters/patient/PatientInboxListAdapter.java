package com.example.telehealth.adapters.patient;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.telehealth.ChatActivity;
import com.example.telehealth.Helper;
import com.example.telehealth.R;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.models.Doctors;
import com.example.telehealth.models.Message;
import com.example.telehealth.models.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientInboxListAdapter extends RecyclerView.Adapter<PatientInboxListAdapter.PatientInboxListViewHolder> {

    private List<Message> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userInfo;
    private Context context;

    public PatientInboxListAdapter(Context context,List<Message> mMessageList){
        this.context = context;
        this.mMessageList = mMessageList;
    }


    @Override
    public PatientInboxListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_home_message_list,parent,false);
        return new PatientInboxListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final PatientInboxListViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        final List<Patient> pt = new ArrayList<>();


        final Message msg = mMessageList.get(position);

        final DatabaseReference senderInfo = FirebaseDatabase.getInstance().getReference().child("Doctors");
        final String pId = mAuth.getCurrentUser().getUid();

        String sender_id = (!pId.equals(msg.getFrom()))? msg.getFrom() : msg.getTo();
        senderInfo.child(sender_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Doctors pp = dataSnapshot.getValue(Doctors.class);
                //  Toast.makeText(DoctorHome.this, message.getMsg(), Toast.LENGTH_SHORT).show();
                holder.mMessageSender.setText(Helper.subStringH(pp.getName(), 14));
                holder.mMessageText.setText(Helper.subStringH(msg.getMsg(), 19));
                holder.mTime.setText(new SimpleDateFormat("EEE, MMM d, ''yy hh:mm a").format(new Date(msg.getTime())));
                //holder.mTime.setText(Helper.getTimeAgo(msg.getTime(), context));
                FirebaseStorageHelper.loadChatProfilePhoto(context, holder.patientProfileImage, sender_id, pp.getImageUrl(), true);
                //Picasso.with(context).load(pp.getImageUrl()).placeholder(R.drawable.ic_person_black_24dp).into(holder.patientProfileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, ChatActivity.class);
                        i.putExtra("doctor_id", sender_id);
                        i.putExtra("doctor_name",pp.getName());
                        i.putExtra("from_activity", "PatientInboxActivity");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                        //mMessageList.clear();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class PatientInboxListViewHolder extends RecyclerView.ViewHolder{

        CircleImageView patientProfileImage;
        TextView mMessageSender,mMessageText,mTime;

        public PatientInboxListViewHolder(View itemView) {
            super(itemView);

            patientProfileImage = (CircleImageView) itemView.findViewById(R.id.msg_list_profile_img);
            mMessageSender = (TextView) itemView.findViewById(R.id.message_sender);
            mMessageText = (TextView) itemView.findViewById(R.id.msg_from_patient);
            mTime = (TextView) itemView.findViewById(R.id.msg_send_time);
        }
    }
}

