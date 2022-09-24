package com.example.telehealth.adapters.doctor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehealth.DoctorSignUpActivity;
import com.example.telehealth.Helper;
import com.example.telehealth.R;
import com.example.telehealth.doctor.DoctorChatActivity;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.models.Message;
import com.example.telehealth.models.Patient;
import com.example.telehealth.models.Users;
import com.example.telehealth.models.doctor.MedicalCheckup;
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

public class CheckUpHistoryAdapter extends RecyclerView.Adapter<CheckUpHistoryAdapter.CheckUpHistoryViewHolder> {

    private List<MedicalCheckup> medicalCheckupList;
    private FirebaseAuth mAuth;
    private String checkupDate, condition, patientName;
    private Context context;

    public CheckUpHistoryAdapter(Context context, List<MedicalCheckup> medicalCheckupList){
        this.context = context;
        this.medicalCheckupList = medicalCheckupList;
    }

    @Override
    public CheckUpHistoryAdapter.CheckUpHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.check_up_history_item,parent,false);
        return new CheckUpHistoryAdapter.CheckUpHistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CheckUpHistoryViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();

        final MedicalCheckup medicalCheckup = medicalCheckupList.get(position);

        final DatabaseReference senderInfo = FirebaseDatabase.getInstance().getReference().child("Users");
        final String dId = mAuth.getCurrentUser().getUid();
        String patient_id = medicalCheckup.getPatient_id();

        senderInfo.child(patient_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Patient patient = snapshot.getValue(Patient.class);
                patientName = patient.getName();

                checkupDate = medicalCheckup.getDate_of_checkup();
                condition = medicalCheckup.getCondition();

                holder.mPatientName.setText(patientName);
                holder.mCheckupDate.setText("Date of Check up: " + checkupDate);
                holder.mCondition.setText(condition);
                FirebaseStorageHelper.loadChatProfilePhoto(context, holder.patientProfileImage, patient_id,patient.getImage(), true);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                        View mView = LayoutInflater.from(context).inflate(R.layout.dialog_patient_checkup_details,null);

                        TextView txtDateOfCheckup = mView.findViewById(R.id.txt_date_of_checkup);
                        TextView txtPatientName = mView.findViewById(R.id.txt_patient_name);
                        TextView txtPatientBday = mView.findViewById(R.id.txt_patient_bday);
                        TextView txtPatientGender = mView.findViewById(R.id.txt_patient_gender);
                        TextView txtPatientCondition = mView.findViewById(R.id.txt_condition);
                        TextView txtClose = mView.findViewById(R.id.txt_close);

                        txtDateOfCheckup.setText(checkupDate);
                        txtPatientName.setText(patientName);
                        txtPatientBday.setText(patient.getBirthday());
                        txtPatientGender.setText(patient.getGender());
                        txtPatientCondition.setText(condition);

                        mBuilder.setView(mView);

                        AlertDialog dialog = mBuilder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();

                        txtClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    @Override
    public int getItemCount() {
        return medicalCheckupList.size();
    }

    public class CheckUpHistoryViewHolder extends RecyclerView.ViewHolder{

        CircleImageView patientProfileImage;
        TextView mPatientName, mCheckupDate, mCondition;

        public CheckUpHistoryViewHolder(View itemView) {
            super(itemView);

            patientProfileImage = (CircleImageView) itemView.findViewById(R.id.patient_photo);
            mPatientName = (TextView) itemView.findViewById(R.id.patient_name);
            mCheckupDate = (TextView) itemView.findViewById(R.id.patient_check_up_date);
            mCondition = (TextView) itemView.findViewById(R.id.condition);
        }
    }
}

