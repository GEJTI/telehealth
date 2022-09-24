package com.example.telehealth.doctor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;

import com.example.telehealth.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;

public class PrescriptionActivity extends AppCompatActivity {

    RadioButton a,b,c;
    EditText mSubject;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Firbase storage
    private StorageReference mImageStorage;
    // database
    private DatabaseReference mUserRef,mDatabase;
    private Query query;

    private String doctorId,senderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        mSubject = (EditText) findViewById(R.id.sub);

        Intent i = getIntent();
        senderId = i.getStringExtra("sender_id");

        //Firebase init
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();

        //Firebase init
        //Firebase Auth init
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        doctorId = mAuth.getCurrentUser().getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Doctors").child(doctorId);

//        try{
//
//            // instantiate database handler
//            databaseH = new DatabaseHandler(PrescriptionActivity.this);
//
//            // put sample data to database
//            insertSampleData();
//
//            // autocompletetextview is in activity_main.xml
//            myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.myautocomplete);
//
//            // add the listener so it will tries to suggest while the user types
//            myAutoComplete.addTextChangedListener(new CustomAutoCompleteTextChangedListener(this));
//
//            // set our adapter
//            myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
//            myAutoComplete.setAdapter(myAdapter);
//
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        //RecyclerView
//        recyclerView = (RecyclerView) findViewById(R.id.medicine_list);
//
//        mAdapter = new MedicineListAdapter(this,list);
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setAdapter(mAdapter);
    }

//    public void insertSampleData(){
//
//        // CREATE
//        databaseH.create( new Medicine("Napa") );
//        databaseH.create( new Medicine("Bronkolax") );
//        databaseH.create( new Medicine("Provair") );
//        databaseH.create( new Medicine("Antacid") );
//        databaseH.create( new Medicine("Renidin") );
//        databaseH.create( new Medicine("Deltason") );
//        databaseH.create( new Medicine("July") );
//        databaseH.create( new Medicine("August") );
//        databaseH.create( new Medicine("September") );
//        databaseH.create( new Medicine("October") );
//        databaseH.create( new Medicine("November") );
//        databaseH.create( new Medicine("December") );
//        databaseH.create( new Medicine("New Caledonia") );
//        databaseH.create( new Medicine("New Zealand") );
//        databaseH.create( new Medicine("Papua New Guinea") );
//        databaseH.create( new Medicine("COFFEE-1K") );
//        databaseH.create( new Medicine("coffee raw") );
//        databaseH.create( new Medicine("authentic COFFEE") );
//        databaseH.create( new Medicine("k12-coffee") );
//        databaseH.create( new Medicine("view coffee") );
//        databaseH.create( new Medicine("Indian-coffee-two") );
//
//    }

//    // this function is used in CustomAutoCompleteTextChangedListener.java
//    public String[] getItemsFromDb(String searchTerm){
//
//        // add items on the array dynamically
//        List<Medicine> products = databaseH.read(searchTerm);
//        int rowCount = products.size();
//
//        String[] item = new String[rowCount];
//        int x = 0;
//
//        for (Medicine record : products) {
//
//            item[x] = record.objectName;
//            x++;
//        }
//
//        return item;
//    }

//
//    public void sendPrescribe(View view){
//        String medicineName = "";
//        String subject = mSubject.getText().toString();
//
//        for(int i=0;i<list.size();i++){
//            medicineName = medicineName + list.get(i).objectName + " \n ";
//        }
//
//        // String message = messageTextBox.getText().toString();
//
//        if(!medicineName.isEmpty()){
//
//            //Insert into Database
//            mDatabase = FirebaseDatabase.getInstance().getReference().child("Prescription").child(senderId).push();
//
//            HashMap<String, String> userMap = new HashMap<String, String>();
//            userMap.put("subject",subject);
//            userMap.put("medicine",medicineName);
//            userMap.put("from",doctorId);
//            userMap.put("to",senderId);
//
//            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if(task.isSuccessful()){
//                        startActivity(new Intent(PrescriptionActivity.this, DoctorHomeActivity.class));
//                        finish();
//                    }
//                    else {
//                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
//
//        if(!medicineName.isEmpty()){
//
//            //Insert into Database
//            mDatabase = FirebaseDatabase.getInstance().getReference().child("Prescription").child(doctorId).push();
//
//            HashMap<String, String> userMap = new HashMap<String, String>();
//            userMap.put("subject",subject);
//            userMap.put("medicine",medicineName);
//            userMap.put("from",doctorId);
//            userMap.put("to",senderId);
//
//            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if(task.isSuccessful()){
//                        startActivity(new Intent(PrescriptionActivity.this,DoctorHomeActivity.class));
//                        finish();
//                    }
//                    else {
//                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
//
//        //  Toast.makeText(getApplicationContext(),medicineName,Toast.LENGTH_SHORT).show();
//    }
}
