package com.example.telehealth.adapters.doctor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.telehealth.Helper;
import com.example.telehealth.R;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.models.Patient;
import com.example.telehealth.models.Users;
import com.example.telehealth.doctor.DoctorChatActivity;
import com.example.telehealth.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorMessageListAdapter extends RecyclerView.Adapter<DoctorMessageListAdapter.DoctorMessageListViewHolder> {

    private List<Message> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userInfo;
    private Context context;

    public DoctorMessageListAdapter(Context context,List<Message> mMessageList){
        this.context = context;
        this.mMessageList = mMessageList;
    }

    @Override
    public DoctorMessageListAdapter.DoctorMessageListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_home_message_list,parent,false);
        return new DoctorMessageListAdapter.DoctorMessageListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final DoctorMessageListViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        final List<Patient> pt = new ArrayList<>();


        final Message msg = mMessageList.get(position);

        final DatabaseReference senderInfo = FirebaseDatabase.getInstance().getReference().child("Users");
        final String dId = mAuth.getCurrentUser().getUid();

        String sender_id = (!dId.equals(msg.getFrom()))? msg.getFrom() : msg.getTo();
        senderInfo.child(sender_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Users pp = dataSnapshot.getValue(Users.class);
                //  Toast.makeText(DoctorHome.this, message.getMsg(), Toast.LENGTH_SHORT).show();
                holder.mMessageSender.setText(Helper.subStringH(pp.getName(), 14));
                holder.mMessageText.setText(Helper.subStringH(msg.getMsg(), 19));
                holder.mTime.setText(new SimpleDateFormat("EEE, MMM d, ''yy hh:mm a").format(new Date(msg.getTime())));
                //holder.mTime.setText(Helper.getTimeAgo(msg.getTime(), context));

                FirebaseStorageHelper.loadChatProfilePhoto(context, holder.patientProfileImage, sender_id, pp.getImage(), true);
                //Picasso.with(context).load(pp.getImage()).placeholder(R.drawable.ic_person_black_24dp).into(holder.patientProfileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, DoctorChatActivity.class);
                        i.putExtra("sender_id", sender_id);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                        // mMessageList.clear();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        if(!dId.equals(msg.getFrom())) {
//            senderInfo.child(msg.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    final Users pp = dataSnapshot.getValue(Users.class);
//                    //  Toast.makeText(DoctorHome.this, message.getMsg(), Toast.LENGTH_SHORT).show();
//                    holder.mMessageSender.setText(Helper.subStringH(pp.getName(), 14));
//                    holder.mMessageText.setText(Helper.subStringH(msg.getMsg(), 19));
//                    holder.mTime.setText(new SimpleDateFormat("EEE, MMM d, ''yy hh:mm a").format(new Date(msg.getTime())));
//                    //holder.mTime.setText(Helper.getTimeAgo(msg.getTime(), context));
//
//                    FirebaseStorageHelper.loadChatProfilePhoto(context, holder.patientProfileImage, msg.getFrom(), pp.getImage(), true);
//                    //Picasso.with(context).load(pp.getImage()).placeholder(R.drawable.ic_person_black_24dp).into(holder.patientProfileImage);
//
//                    holder.itemView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Intent i = new Intent(context, DoctorChatActivity.class);
//                            i.putExtra("sender_id", msg.getFrom());
//                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            context.startActivity(i);
//                           // mMessageList.clear();
//                        }
//                    });
//
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
//        else {
//            senderInfo.child(msg.getTo()).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    final Users pp = dataSnapshot.getValue(Users.class);
//
//                    holder.mMessageSender.setText(Helper.subStringH(pp.getName(), 14));
//                    holder.mMessageText.setText(Helper.subStringH(msg.getMsg(), 19));
//                    holder.mTime.setText(new SimpleDateFormat("EEE, MMM d, ''yy hh:mm a").format(new Date(msg.getTime())));
//                    //holder.mTime.setText(Helper.getTimeAgo(msg.getTime(), context));
//                    FirebaseStorageHelper.loadChatProfilePhoto(context, holder.patientProfileImage, msg.getTo(), pp.getImage(), true);
//                    //Picasso.with(context).load(pp.getImage()).placeholder(R.drawable.ic_person_black_24dp).into(holder.patientProfileImage);
//
//                    holder.itemView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Intent i = new Intent(context, DoctorChatActivity.class);
//                            i.putExtra("sender_id", msg.getTo());
//                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            context.startActivity(i);
//                            //mMessageList.clear();
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }



    }



    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class DoctorMessageListViewHolder extends RecyclerView.ViewHolder{

        CircleImageView patientProfileImage;
        TextView mMessageSender,mMessageText,mTime;

        public DoctorMessageListViewHolder(View itemView) {
            super(itemView);

            patientProfileImage = (CircleImageView) itemView.findViewById(R.id.msg_list_profile_img);
            mMessageSender = (TextView) itemView.findViewById(R.id.message_sender);
            mMessageText = (TextView) itemView.findViewById(R.id.msg_from_patient);
            mTime = (TextView) itemView.findViewById(R.id.msg_send_time);
        }
    }
}
