package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.telehealth.customfonts.MyTextView_Poppins_Medium;
import com.example.telehealth.models.Patient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientCheckUpQuestion extends AppCompatActivity {

    DatePickerDialog picker;
    TextView date_checkup;
    EditText patient_name, patient_bdy, patient_gender;
    RadioGroup patient_condition;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String uid, doctor_specialization, condition;

    MyTextView_Poppins_Medium btnStart;

    ProgressDialog progressDialog;

    private final String MyPREFERENCES = "CheckUpData";
    private final String NONE = "none";

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_check_up_question);

        patient_name = findViewById(R.id.patient_name);
        patient_bdy = findViewById(R.id.patient_bdy);
        patient_gender = findViewById(R.id.patient_gender);
        date_checkup = findViewById(R.id.date_of_checkup);
        patient_condition = findViewById(R.id.radCondition);

        btnStart = findViewById(R.id.btn_start);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        uid = mAuth.getCurrentUser().getUid();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Checkup");
        progressDialog.setMessage("Saving your data..");
        progressDialog.setCanceledOnTouchOutside(false);

        date_checkup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectAppointment();
            }
        });

        List specialization = Arrays.asList(getResources().getStringArray(R.array.doctor_specializations_array));

        patient_condition.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                int index = 0;
                if(id == R.id.oncologist) {
                    index = 1;
                }else if(id == R.id.pathologist)
                {
                    index = 2;
                }else if(id == R.id.hematologist)
                {
                    index = 3;
                }else if(id == R.id.dermatologist)
                {
                    index = 4;
                }
                RadioButton radioButton = findViewById(id);
                condition = radioButton.getText().toString().trim();
                doctor_specialization = (id == R.id.none)? NONE : specialization.get(index).toString();
               // Toast.makeText(getApplicationContext(), condition + " - " + doctor_specialization, Toast.LENGTH_SHORT).show();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMedicalCheckUp();
            }
        });

//        date_checkup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Calendar cldr = Calendar.getInstance();
//                int day = cldr.get(Calendar.DAY_OF_MONTH);
//                int month = cldr.get(Calendar.MONTH);
//                int year = cldr.get(Calendar.YEAR);
//
//                picker = new DatePickerDialog(PatientCheckUpQuestion.this, new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                        date_checkup.setText(dayOfMonth+"/"+(month+1)+"/"+ year);
//                    }
//                },year,month,day);
//                picker.show();
//            }
//        });
        getPatientInfo();
    }

    private void getPatientInfo()
    {
        mDatabase.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Patient patient = snapshot.getValue(Patient.class);
                patient_name.setText(patient.getName());
                patient_bdy.setText(patient.getBirthday());
                patient_gender.setText(patient.getGender());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectAppointment(){
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);

        picker = new DatePickerDialog(PatientCheckUpQuestion.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date_checkup.setText((month + 1) + "/" + dayOfMonth + "/" + year);
            }
        },year,month,day);
        picker.show();
    }

    private void createMedicalCheckUp()
    {
        String dateOfCheckup = date_checkup.getText().toString().trim();
        if(dateOfCheckup.isEmpty() || condition == null)
        {
            Toast.makeText(getApplicationContext(), "Please fill all requirements.", Toast.LENGTH_SHORT).show();
        }else
        {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("condition", condition);
            editor.putString("date_of_checkup", dateOfCheckup);
            editor.apply();

            boolean hasNoSpecialization = doctor_specialization.equals(NONE);

            Class gotoClass = hasNoSpecialization? PatientHomeActivity.class : DoctorListActivity.class;
            Intent intent = new Intent(PatientCheckUpQuestion.this, gotoClass);
            if(!hasNoSpecialization)
            {
                intent.putExtra("doctor_specialization", doctor_specialization);
                intent.putExtra("fromActivity", "PatientCheckupQuestion");
            }
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(PatientCheckUpQuestion.this, PatientCheckUpActivity.class));
    }
}