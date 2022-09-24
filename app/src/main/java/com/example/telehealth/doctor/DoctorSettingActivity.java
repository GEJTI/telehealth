package com.example.telehealth.doctor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTargetFactory;
import com.example.telehealth.DoctorSignUpActivity;
import com.example.telehealth.LoginActivity;
import com.example.telehealth.PatientProfileSettingActivity;
import com.example.telehealth.R;
import com.example.telehealth.adapters.CustomSpinnerAdapter;
import com.example.telehealth.customfonts.MyTextView_Poppins_Medium;
import com.example.telehealth.customfonts.MyTextView_Poppins_Regular;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.helpers.GlideHelper;
import com.example.telehealth.models.Doctors;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.constant.ImageProvider;
import com.github.dhaval2404.imagepicker.listener.DismissListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class DoctorSettingActivity extends AppCompatActivity {

    public static final String TAG="DoctorSetting";

    private static final int PROFILE_IMAGE_REQ_CODE = 101;
    public static final String PROFILE_IMAGE = "profileImg";
    public static final String LICENSE_IMAGE = "licenseImg";
    public static final String PROFILE_BTN_ADD_TEXT = "CHANGE PICTURE";
    public static final String PROFILE_SAVE_PIC_TEXT = "SAVE PICTURE";

    private Toolbar mToolbar;
    String uid;
    FirebaseUser currentUser;
    //Firbase storage
    private StorageReference mImageStorage;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // database
    private DatabaseReference mUserRef,mDatabase;
    private Query query;

    public String doctorId;
    Uri pictureURI, licenseURI;
    //CircularImageView
    CircleImageView profilePicture;
    ImageView imgLicense;
    MyTextView_Poppins_Medium btnSavePic, btnSaveInfo, btnSaveLicense, btnChangeLicense, btnSavePassword;
    EditText editName, editMobileNo, editCurrentPword, editNewPword, editConfirmPword;
    Spinner spinnerSpecialization;
    MyTextView_Poppins_Regular txtLicenseValidity;

    MaterialButtonToggleGroup toggleAvailability;

    ProgressDialog progressDialog;

    String uploadingRequest;

    private CustomSpinnerAdapter specializationAdapter;

    DatePickerDialog picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_setting);

        //Firebase storage
        mImageStorage = FirebaseStorage.getInstance().getReference();
        //Firebase init
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();

        //Firebase init
        //Firebase Auth init
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        doctorId = mAuth.getCurrentUser().getUid();
        //mProfilePicture imageview init
        currentUser = mAuth.getInstance().getCurrentUser();
        uid = currentUser.getUid();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Doctors").child(doctorId);

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
        imgLicense = (ImageView) findViewById(R.id.imgLicense);

        editName = (EditText) findViewById(R.id.profile_name);
        editMobileNo = (EditText) findViewById(R.id.profile_mobile_no);
        editCurrentPword = (EditText) findViewById(R.id.current_password);
        editNewPword = (EditText) findViewById(R.id.new_password);
        editConfirmPword = (EditText) findViewById(R.id.confirm_password);

        spinnerSpecialization = (Spinner) findViewById(R.id.doctor_specialization);

        specializationAdapter =
                new CustomSpinnerAdapter(this, R.layout.spinner_item, Arrays.asList(getResources().getStringArray(R.array.doctor_specializations_array)));
        spinnerSpecialization.setAdapter(specializationAdapter);

        btnSavePic = findViewById(R.id.btnSavePic);
        btnSaveInfo = findViewById(R.id.btnSaveInfo);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        btnChangeLicense = findViewById(R.id.btnChangeLicense);
        btnSaveLicense = findViewById(R.id.btnSaveLicense);

        txtLicenseValidity = findViewById(R.id.profile_date_license);

        toggleAvailability = findViewById(R.id.toggleAvailability);

        toggleAvailability.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if(isChecked)
                {
                    setAvailability(checkedId == R.id.btnAvailable? "Available" : "Busy");
                }
            }
        });

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

        btnChangeLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLicenseImage();
            }
        });

        btnSaveLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLicense();
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

        //Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Doctors").child(uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Doctors doctor = snapshot.getValue(Doctors.class);
                String imageUrl = doctor.getImageUrl();
                editName.setText(doctor.getName());
                editMobileNo.setText(doctor.getMobile());
                spinnerSpecialization.setSelection(specializationAdapter.getPosition(doctor.getSpecialization()));
                toggleAvailability.check(doctor.getAvailability().equals("Available")? R.id.btnAvailable : R.id.btnBusy);

                if(imageUrl.equals("Default")){
                    profilePicture.setImageResource(R.drawable.ic_person_black_24dp);
                }
                else {
                    FirebaseStorageHelper.loadProfilePhoto(DoctorSettingActivity.this, profilePicture, uid, imageUrl, false);
//                    StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profile_image").child(uid).child(imageUrl);
//                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            // Got the download URL for 'users/me/profile.png'
//                            GlideHelper.loadThumbnail(DoctorSettingActivity.this, profilePicture, uri);
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception exception) {
//                            // Handle any errors
//                        }
//                    });
                    //Picasso.with(getApplicationContext()).load(imageUrl).into(profilePicture);
                }

                FirebaseStorageHelper.asyncLicensePhoto(DoctorSettingActivity.this, imgLicense, uid, false);

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                String license_validity = doctor.getLicenseValidity();
                String[] validityArr = license_validity.split("-");
                txtLicenseValidity.setText(license_validity);
                txtLicenseValidity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DoctorSettingActivity.this);
                        View mView = getLayoutInflater().inflate(R.layout.date_dialog,null);

                        TextView start_date = mView.findViewById(R.id.start_date);
                        TextView end_date = mView.findViewById(R.id.expiry_date);
                        TextView ok_date = mView.findViewById(R.id.ok_date);

                        String sDate = validityArr[0].trim();
                        String eDate = validityArr[1].trim();

                        start_date.setText(sDate);
                        end_date.setText(eDate);

                        start_date.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Calendar cldr = Calendar.getInstance();
                                try {
                                    cldr.setTime(sdf.parse(sDate));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                int day = cldr.get(Calendar.DAY_OF_MONTH);
                                int month = cldr.get(Calendar.MONTH);
                                int year = cldr.get(Calendar.YEAR);

                                picker = new DatePickerDialog(DoctorSettingActivity.this, new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        start_date.setText((month+1) + "/" + dayOfMonth + "/" + year);
                                    }
                                },year,month,day);
                                picker.show();
                            }
                        });
                        end_date.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Calendar cldr = Calendar.getInstance();
                                try {
                                    cldr.setTime(sdf.parse(eDate));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                int day = cldr.get(Calendar.DAY_OF_MONTH);
                                int month = cldr.get(Calendar.MONTH);
                                int year = cldr.get(Calendar.YEAR);

                                picker = new DatePickerDialog(DoctorSettingActivity.this, new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        end_date.setText((month+1) + "/" + dayOfMonth + "/" + year);
                                    }
                                },year,month,day);
                                picker.show();
                            }
                        });

                        mBuilder.setView(mView);
                        final AlertDialog dialog = mBuilder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();

                        ok_date.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String start = start_date.getText().toString();
                                String end = end_date.getText().toString();
                                if (start.matches("")) {
                                    Toast.makeText(DoctorSettingActivity.this, "Please Select Start Date", Toast.LENGTH_SHORT).show();
                                    return;
                                }else if (end.matches("")){
                                    Toast.makeText(DoctorSettingActivity.this, "Please Select End Date", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                txtLicenseValidity.setText(start_date.getText() + " - " +end_date.getText());


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
//        mDatabase.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                String imageUrl = dataSnapshot.child("imageUrl").getValue().toString();
//
//                if(imageUrl.equals("Default")){
//                    profilePicture.setImageResource(R.drawable.ic_person_black_24dp);
//                }
//                else {
//                    StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profile_image").child(uid).child(imageUrl);
//                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            // Got the download URL for 'users/me/profile.png'
//                            Glide.with(getApplicationContext()).load(uri).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(profilePicture);
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception exception) {
//                            // Handle any errors
//                        }
//                    });
//                    //Picasso.with(getApplicationContext()).load(imageUrl).into(profilePicture);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

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

    private void changeLicenseImage()
    {
        showImagePickerDialog();
        uploadingRequest = LICENSE_IMAGE;
    }

    private void showImagePickerDialog()
    {
        ImagePicker.with(DoctorSettingActivity.this)
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
                    //    setDefSavePicBtn();
                    }
                })
                .start(PROFILE_IMAGE_REQ_CODE);;
    }

    private void savePicture()
    {
        progressDialog.show();
        String imgName = uid + ".jpg";
        final StorageReference imageRef = mImageStorage.child("profile_image").child(uid).child(imgName);
        imageRef.putFile(pictureURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful()){
                    //@SuppressWarnings("VisibleForTests") String imageUrl = imageRef.getDownloadUrl().toString();
                    mDatabase.child("imageUrl").setValue(imgName).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Uri object will not b;e null for RESULT_OK
            Uri uri = data.getData();
            if(uploadingRequest.equals(PROFILE_IMAGE))
            {
                pictureURI = uri;
                profilePicture.setImageURI(pictureURI);
                btnSavePic.setText(PROFILE_SAVE_PIC_TEXT);
            }else{
                licenseURI = uri;
                imgLicense.setImageURI(licenseURI);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            setDefSavePicBtn();
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            setDefSavePicBtn();
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAvailability(String availability)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("availability", availability);

        mDatabase.updateChildren(data, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    String message = "Updating availability failed";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveInfo()
    {
        String name  = editName.getText().toString().trim();
        String mobile = editMobileNo.getText().toString().trim();
        String specialization = spinnerSpecialization.getSelectedItem().toString();

        if(name.isEmpty() || mobile.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Please fill all requirements.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();

        data.put("name", name);
        data.put("mobile", mobile);
        data.put("specialization", specialization);

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

    private void saveLicense()
    {
        String license_validity = txtLicenseValidity.getText().toString().trim();

        if(license_validity.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Please fill all requirements.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        if(licenseURI == null)
        {
            saveLicenseValidity(license_validity, "");
        }else{
            String imgName = uid + ".jpg";
            final StorageReference imageRef = mImageStorage.child("license_image").child(uid).child(imgName);
            imageRef.putFile(licenseURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                        saveLicenseValidity(license_validity, imgName);
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }

    private void saveLicenseValidity(String license_validity, String license_url)
    {
        Map<String, Object> data = new HashMap<>();

        data.put("licenseValidity", license_validity);

        if(!license_url.isEmpty())
        {
            data.put("licenseUrl", license_url);
        }

        mDatabase.updateChildren(data, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                progressDialog.dismiss();
                licenseURI = null;
                String message = (error != null)? "Updating failed" : "License Info updated successfully";
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

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_bar_menu,menu);

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
            startActivity(new Intent(DoctorSettingActivity.this, DoctorProfileActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(DoctorSettingActivity.this, DoctorSettingActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_history){
            startActivity(new Intent(DoctorSettingActivity.this, CheckUpHistoryActivity.class));
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            FirebaseDatabase.getInstance().goOffline();
            mAuth.signOut();
            startActivity(new Intent(DoctorSettingActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }
}