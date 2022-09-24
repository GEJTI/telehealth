package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.telehealth.adapters.CustomSpinnerAdapter;
import com.example.telehealth.customfonts.MyTextView_Poppins_Medium;
import com.example.telehealth.doctor.DoctorChatActivity;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.helpers.GlideHelper;
import com.example.telehealth.models.Patient;
import com.example.telehealth.models.Users;
import com.example.telehealth.patient.PatientInboxActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.constant.ImageProvider;
import com.github.dhaval2404.imagepicker.listener.DismissListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class PatientProfileSettingActivity extends AppCompatActivity {

    public static final String TAG="PatientProfileSetting";
    private static final int PROFILE_IMAGE_REQ_CODE = 101;
    public static final String PROFILE_IMAGE = "profileImg";
    public static final String VALID_ID_IMAGE = "validIDImg";
    public static final String PROFILE_BTN_ADD_TEXT = "CHANGE PICTURE";
    public static final String PROFILE_SAVE_PIC_TEXT = "SAVE PICTURE";
    public static final String VALID_BTN_ADD_TEXT = "CHANGE VALID ID";
    public static final String VALID_SAVE_PIC_TEXT = "SAVE VALID ID";

    private Toolbar mToolbar;
    //Firbase storage
    private StorageReference mImageStorage;
    //Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser currentUser;

    String uid;
    Uri pictureURI, validIdURI;

    //Firebase Database
    // Write a message to the database
    private DatabaseReference mDatabase;
    //CircularImageView
    CircleImageView profilePicture;
    ImageView imgValidID;
    MyTextView_Poppins_Medium btnSavePic, btnSaveInfo, btnChangeValidId, btnSavePassword;

    EditText editName, editAge, editMobileNo, editCurrentPword, editNewPword, editConfirmPword;
    TextView txtBday;
    Spinner spinnerGender;

    ProgressDialog progressDialog;

    String uploadingRequest;

    private CustomSpinnerAdapter customSpinnerAdapter;

    DatePickerDialog picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile_setting);

        //Firebase storage
        mImageStorage = FirebaseStorage.getInstance().getReference();
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
        //CircularImageView init
        profilePicture = (CircleImageView) findViewById(R.id.profile_image);
        imgValidID = (ImageView) findViewById(R.id.imgValidID);

        editName = (EditText) findViewById(R.id.profile_name);
        txtBday = (TextView) findViewById(R.id.profile_bday);
        editAge = (EditText) findViewById(R.id.profile_age);
        editMobileNo = (EditText) findViewById(R.id.profile_mobile_no);
        editCurrentPword = (EditText) findViewById(R.id.current_password);
        editNewPword = (EditText) findViewById(R.id.new_password);
        editConfirmPword = (EditText) findViewById(R.id.confirm_password);

        spinnerGender = (Spinner) findViewById(R.id.profile_gender);

        customSpinnerAdapter =
                new CustomSpinnerAdapter(this, R.layout.spinner_item, Arrays.asList(getResources().getStringArray(R.array.gender_array)));
        spinnerGender.setAdapter(customSpinnerAdapter);

        btnSavePic = findViewById(R.id.btnSavePic);
        btnSaveInfo = findViewById(R.id.btnSaveInfo);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        btnChangeValidId = findViewById(R.id.btnChangeValidId);

        btnSavePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProfileImage();
            }
        });

        btnSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInfo();
            }
        });

        btnChangeValidId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setValidIdImage();
            }
        });

        btnSavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePassword();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCanceledOnTouchOutside(false);

        //mProfilePicture imageview init
        currentUser = mAuth.getInstance().getCurrentUser();
        uid = currentUser.getUid();
        //Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Patient patient = dataSnapshot.getValue(Patient.class);
                String imageUrl = dataSnapshot.child("image").getValue().toString();
                String birthDay = patient.getBirthday();

                editName.setText(patient.getName());
                txtBday.setText(birthDay);
                editAge.setText(patient.getAge());
                editMobileNo.setText(patient.getMobile());
                spinnerGender.setSelection(customSpinnerAdapter.getPosition(patient.getGender()));

                txtBday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        final Calendar cldr = Calendar.getInstance();
                        try {
                            cldr.setTime(sdf.parse(birthDay));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        int day = cldr.get(Calendar.DAY_OF_MONTH);
                        int month = cldr.get(Calendar.MONTH);
                        int year = cldr.get(Calendar.YEAR);

                        picker = new DatePickerDialog(PatientProfileSettingActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String bDay = (month + 1) + "/" + dayOfMonth + "/" + year;
                                txtBday.setText(bDay);
                                editAge.setText(String.valueOf(getAge(bDay)));
                            }
                        },year,month,day);
                        picker.show();
                    }
                });

                if(imageUrl.equals("Default")){
                    profilePicture.setImageResource(R.drawable.ic_person_black_24dp);
                }
                else {
                    FirebaseStorageHelper.loadProfilePhoto(PatientProfileSettingActivity.this, profilePicture, uid, imageUrl, false);
//                    StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profile_image").child(uid).child(imageUrl);
//                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            // Got the download URL for 'users/me/profile.png'
//                            GlideHelper.loadThumbnail(PatientProfileSettingActivity.this, profilePicture, uri);
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception exception) {
//                            // Handle any errors
//                        }
//                    });
                   // Glide.with(getApplicationContext()).load(imageUrl).into(profilePicture);
                }

                FirebaseStorageHelper.asyncValidIdPhoto(PatientProfileSettingActivity.this, imgValidID, uid, false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Toolbar initialization
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile Setting");
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    //Profile picture upload
    //Profile picture upload
    private void setProfileImage(){
        String btnText = btnSavePic.getText().toString().trim();
        if(btnText.equals(PROFILE_BTN_ADD_TEXT))
        {
            showImagePickerDialog();
            uploadingRequest = PROFILE_IMAGE;
        }else{
            savePicture();
        }
    }

    private void setDefSavePicBtn()
    {
        if(uploadingRequest.equals(PROFILE_IMAGE)) {
            btnSavePic.setText(PROFILE_BTN_ADD_TEXT);
        }
    }

    private void setValidIdImage()
    {
        String btnText = btnChangeValidId.getText().toString().trim();
        if(btnText.equals(VALID_BTN_ADD_TEXT))
        {
            showImagePickerDialog();
            uploadingRequest = VALID_ID_IMAGE;
        }else{
            saveID();
        }
    }

    private void setDefSaveIDPicBtn()
    {
        if(uploadingRequest.equals(VALID_ID_IMAGE)) {
            btnChangeValidId.setText(VALID_BTN_ADD_TEXT);
        }
    }

    private void setDefaultSaveBtnText()
    {
        setDefSavePicBtn();
        setDefSaveIDPicBtn();
    }

    public void showImagePickerDialog(){
        ImagePicker.with(PatientProfileSettingActivity.this)
            .compress(500)
            .maxResultSize(620, 620)
            .setImageProviderInterceptor(new Function1<ImageProvider, Unit>() {
                @Override
                public Unit invoke(ImageProvider imageProvider) {
                    Log.d("ImagePicker", "Selected ImageProvider: " + imageProvider.toString());
                    return null;
                }
            })
            .setDismissListener(new DismissListener() {
                @Override
                public void onDismiss() {

                }
            })
            .start(PROFILE_IMAGE_REQ_CODE);;
        //PickImageDialog.build(new PickSetup()).show(this);
    }

    public void savePicture()
    {
        progressDialog.show();
        String imgName = uid + ".jpg";
        final StorageReference imageRef = mImageStorage.child("profile_image").child(uid).child(imgName);
        imageRef.putFile(pictureURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful()){
                    //@SuppressWarnings("VisibleForTests") String imageUrl = imageRef.getDownloadUrl().toString();
                    mDatabase.child("image").setValue(imgName).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if(task.isSuccessful()) {
                                setDefSavePicBtn();
                                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void saveID()
    {
        progressDialog.show();
        String imgName = uid + ".jpg";
        final StorageReference imageRef = mImageStorage.child("valid_id_image").child(uid).child(imgName);
        imageRef.putFile(validIdURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful()){
                    //@SuppressWarnings("VisibleForTests") String imageUrl = imageRef.getDownloadUrl().toString();
                    mDatabase.child("validIdUrl").setValue(imgName).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if(task.isSuccessful()) {
                                setDefSaveIDPicBtn();
                                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Uri object will not be null for RESULT_OK
            Uri uri = data.getData();
            if(uploadingRequest.equals(PROFILE_IMAGE))
            {
                pictureURI = uri;
                profilePicture.setImageURI(pictureURI);
                btnSavePic.setText(PROFILE_SAVE_PIC_TEXT);
            }else{
                validIdURI = uri;
                imgValidID.setImageURI(validIdURI);
                btnChangeValidId.setText(VALID_SAVE_PIC_TEXT);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            setDefaultSaveBtnText();
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            setDefaultSaveBtnText();
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private int getAge(String dobString){

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null) return 0;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int year = dob.get(Calendar.YEAR);
        int month = dob.get(Calendar.MONTH);
        int day = dob.get(Calendar.DAY_OF_MONTH);

        dob.set(year, month+1, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        return age;
    }

    private void saveInfo()
    {
        String name  = editName.getText().toString().trim();
        String birthday = txtBday.getText().toString().trim();
        String age = editAge.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String mobile = editMobileNo.getText().toString().trim();

        if(name.isEmpty() || birthday.isEmpty() || age.isEmpty() || gender.isEmpty() || mobile.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Please fill all requirements.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();

        data.put("name", name);
        data.put("birthday", birthday);
        data.put("age", age);
        data.put("gender", gender);
        data.put("mobile", mobile);

        progressDialog.show();
        mDatabase.updateChildren(data, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                progressDialog.dismiss();
                String message = (error != null)? "Updating failed" : "Info updated successfully";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePassword()
    {
        String current_pword = editCurrentPword.getText().toString().trim();
        String new_pword = editNewPword.getText().toString().trim();
        String confirm_pword = editConfirmPword.getText().toString().trim();

        if(current_pword.isEmpty() || new_pword.isEmpty() || confirm_pword.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Please fill all requirements.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!new_pword.equals(confirm_pword))
        {
            Toast.makeText(getApplicationContext(), "Passwords are not same", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider
                .getCredential(currentUser.getEmail(), current_pword);

        progressDialog.show();
        currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    currentUser.updatePassword(new_pword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            editCurrentPword.setText(null);
                            editNewPword.setText(null);
                            editConfirmPword.setText(null);
                            editCurrentPword.requestFocus();
                            String message = (task.isSuccessful())? "Password updated." : "Updating failed";
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }else
                {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Wrong current password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


//    @Override
//    public void onPickResult(PickResult pickResult) {
//        if (pickResult.getError() == null) {
//
//            Uri image = pickResult.getUri();
//            final StorageReference imageRef = mImageStorage.child("profile_image").child(uid+".jpg");
//            imageRef.putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                    if(task.isSuccessful()){
//                        @SuppressWarnings("VisibleForTests") String imageUrl = imageRef.getDownloadUrl().toString();
//                        mDatabase.child("image").setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()) {
//                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
//                                }
//                                else {
//                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        });
//                    }
//                    else {
//                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
//                    }
//
//                }
//            });
//
//
//        } else {
//            //Handle possible errors
//            //TODO: do what you have to do with r.getError();
//            Toast.makeText(this, pickResult.getError().getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }

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
            startActivity(new Intent(PatientProfileSettingActivity.this, PatientProfileActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
          //  startActivity(new Intent(PatientProfileSettingActivity.this, PatientProfileSettingActivity.class));
            //finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_inbox){
            startActivity(new Intent(PatientProfileSettingActivity.this, PatientInboxActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            mAuth.signOut();
            startActivity(new Intent(PatientProfileSettingActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}