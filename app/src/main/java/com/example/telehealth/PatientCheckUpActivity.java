package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.telehealth.models.Doctors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientCheckUpActivity extends AppCompatActivity {

    TextView new_check, old_check_up;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_check_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        uid = mAuth.getCurrentUser().getUid();

        new_check = findViewById(R.id.new_check_up);
        old_check_up = findViewById(R.id.old_check_up);

        new_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PatientCheckUpActivity.this, PatientCheckUpQuestion.class));
                finish();
            }
        });

        old_check_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("MedicalCheckups").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {

                            for (DataSnapshot children : snapshot.getChildren()){
                                String doctor_id =  children.child("doctor_id").getValue().toString();
                                mDatabase.child("Doctors").child(doctor_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Doctors doctor = snapshot.getValue(Doctors.class);
                                        Intent msgActivity = new Intent(getApplicationContext(), ChatActivity.class);
                                        msgActivity.putExtra("doctor_name", doctor.getName());
                                        msgActivity.putExtra("doctor_id", doctor.getId());
                                        msgActivity.putExtra("from_activity", "PatientCheckUpActivity");
                                        startActivity(msgActivity);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                break;
                            }
                        }else
                        {
                            Toast.makeText(getApplicationContext(), "No existing medical record.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}