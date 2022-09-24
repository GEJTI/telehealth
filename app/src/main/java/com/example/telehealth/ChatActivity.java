package com.example.telehealth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.arch.core.util.Function;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;
import com.example.telehealth.adapters.MessageAdapter;
import com.example.telehealth.helpers.FileHelper;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.interfaces.BitMapSetter;
import com.example.telehealth.models.Doctors;
import com.example.telehealth.models.Message;
import com.example.telehealth.patient.DoctorProfileActivity;
import com.example.telehealth.patient.PatientInboxActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.constant.ImageProvider;
import com.github.dhaval2404.imagepicker.listener.DismissListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ChatActivity extends AppCompatActivity implements BitMapSetter {

    public static final String TAG="ChatActivity";

    private static final int PROFILE_IMAGE_REQ_CODE = 101;

    private Toolbar mToolbar;

    //RecyclerView
    RecyclerView mMessageView;
    List<Message> mMessageList = new ArrayList<>();
    public LinearLayoutManager mLinearLayout;
    public MessageAdapter mAdapter;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // database
    public DatabaseReference mDatabase;
    public Query query;

    //Firbase storage
    private StorageReference mImageStorage;

    public String doctorName, doctorId, doctorSpecialization, doctorImgUrl;
    public String currentUserId;
    public String fromActivity;

    //Widget
    private ImageView sendImageButton,sendMessageButton;
    private EditText messageTextBox;

    //For loading message

    private static final int TOTAL_MSG_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey="";

    ProgressDialog progressDialog;

    Bitmap qrBitMap;
    String qrFileName;

    boolean isFromCheckUp, isFromDoctorDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent i = getIntent();
        doctorName = i.getStringExtra("doctor_name");
        doctorId = i.getStringExtra("doctor_id");
        fromActivity = i.getStringExtra("from_activity");

        isFromCheckUp = fromActivity.equals("PatientCheckUpActivity");
        isFromDoctorDetails = fromActivity.equals("DoctorDetailsActivity");

        if(isFromDoctorDetails)
        {
            doctorSpecialization = i.getStringExtra("doctor_specialization");
            doctorImgUrl = i.getStringExtra("doctor_image");
        }

        //Firebase storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //Widget initialization
        sendImageButton = (ImageView) findViewById(R.id.send_image);
        sendMessageButton = (ImageView) findViewById(R.id.message_send_button);

        messageTextBox = (EditText) findViewById(R.id.send_text_message);

        //RecyclerView

        mAdapter = new MessageAdapter(this, mMessageList, this);
        mMessageView = (RecyclerView) findViewById(R.id.message_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessageView.setLayoutManager(mLinearLayout);
        loadMessage();
        mMessageView.setAdapter(mAdapter);

        progressDialog = new ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Toolbar initialization
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setToolbar();
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle(doctorName);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        //mProfilePicture imageview init
        final FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();

        //Firebase init
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

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

        //Chating Activity
        mDatabase.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(doctorId)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+currentUserId+"/"+doctorId,chatAddMap);
                    chatUserMap.put("Chat/"+doctorId+"/"+currentUserId,chatAddMap);

                    mDatabase.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Log.d(TAG,"Message sending failed for, database failure.");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Onclick for send button
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(getPushKey(), "text", messageTextBox.getText().toString().trim());
            }
        });

    }

    private void setToolbar()
    {
        CircleImageView doctorImg = (CircleImageView) mToolbar.findViewById(R.id.imgDoctorProfile);
        TextView txtDoctorName = (TextView) mToolbar.findViewById(R.id.txtDoctorName);
        ImageView imgDoctorPresence = (ImageView) mToolbar.findViewById(R.id.imgDoctorPresence);
        TextView txtDoctorStatus = (TextView) mToolbar.findViewById(R.id.txtDoctorStatus);

        DatabaseReference doctorRef = mDatabase.child("Doctors").child(doctorId);

        doctorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Doctors doctor = snapshot.getValue(Doctors.class);
                if(doctor.getImageUrl().equals("Default")){
                    doctorImg.setImageResource(R.drawable.ic_person_black_24dp);
                }
                else {
                    FirebaseStorageHelper.loadProfilePhoto(ChatActivity.this, doctorImg, doctorId, doctor.getImageUrl(), false);
                }
                txtDoctorName.setText(doctor.getName());
                if(doctor.getOnline())
                {
                    String availability = doctor.getAvailability();
                    boolean isAvailable = availability.equals("Available");
                    imgDoctorPresence.setImageResource(isAvailable? R.drawable.online : R.drawable.busy);
                    txtDoctorStatus.setText(availability);
                    TSnackbar snackbar = TSnackbar.make(findViewById(R.id.view), "Doctor is currently " + (isAvailable? "available. Feel free to ask for your medical concerns." : "busy.") , TSnackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.setMaxWidth(3000);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor(isAvailable? "#6bbe66" : "#ff4141"));
                    TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snackbar.show();
                }else{
                    imgDoctorPresence.setImageResource(R.drawable.offline_gray);
                    txtDoctorStatus.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getPushKey()
    {
        DatabaseReference user_message_push = mDatabase.child("Message")
                .child(currentUserId).child(doctorId).push();
        return user_message_push.getKey();
    }

    //For sending message patient to doctor
    public void sendMessage(String push_id, String type, String message){

        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "Message/"+currentUserId+"/"+doctorId;
            String doctor_ref = "Message/"+doctorId+"/"+currentUserId;

            Map messageMap = new HashMap();
            messageMap.put("msg",message);
            messageMap.put("seen",false);
            messageMap.put("type",type);
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserId);
            messageMap.put("to",doctorId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageUserMap.put(doctor_ref+"/"+push_id,messageMap);

            messageTextBox.setText("");

            mDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d(TAG,"Message sending failed for, database failure.");
                    }
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    //For getting message from database
    public void loadMessage(){

        mAuth = FirebaseAuth.getInstance();

        String patientId = mAuth.getCurrentUser().getUid();

        DatabaseReference retriveMessae = FirebaseDatabase.getInstance().getReference().child("Message").child(patientId)
                .child(doctorId);
        Query msgQuery = retriveMessae;

        //  Toast.makeText(getApplicationContext(),patientId,Toast.LENGTH_SHORT).show();

        msgQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);

                itemPos++;
                if(itemPos == 1){
                    mLastKey = dataSnapshot.getKey();
                }

                mMessageList.add(message);
                //mAdapter.notifyDataSetChanged();
                mAdapter.notifyItemInserted(mMessageList.size() - 1);
                //For showing the last message in view
                mMessageView.scrollToPosition(mMessageList.size()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    //Send Image code
    public void sendImage(View view){
        ImagePicker.with(ChatActivity.this)
                .compress(1024)
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
                .start(PROFILE_IMAGE_REQ_CODE);
//        FileHelper.browsePhoto(ChatActivity.this);
        //PickImageDialog.build(new PickSetup()).show(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            switch(requestCode)
            {
                case 0:
                    FileHelper.saveBitmapToFile(qrBitMap, ChatActivity.this, qrFileName);
                    break;
                case 1:
                   // FileHelper.browsePhoto(ChatActivity.this);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            // Uri object will not be null for RESULT_OK
            final String push_id = getPushKey();

            String imgName = push_id+".jpg";
            StorageReference imageRef = mImageStorage.child("message_attach").child("images").child(imgName);

            progressDialog.setMessage("Sending...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            imageRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                        //@SuppressWarnings("VisibleForTests") final String imageUrl = task.getResult().toString();
                        mDatabase.child("image").setValue(imgName).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    sendMessage(push_id, "image", imgName);
                                }
                                else {
                                    progressDialog.dismiss();
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

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            // Uri object will not be null for RESULT_OK
//            ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
//            Uri uri = files.get(0).getUri();
//
//            final String push_id = getPushKey();
//
//            String imgName = push_id+".jpg";
//            StorageReference imageRef = mImageStorage.child("message_attach").child("images").child(imgName);
//
//            progressDialog.setMessage("Sending...");
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.show();
//
//            imageRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                    if(task.isSuccessful()){
//                        //@SuppressWarnings("VisibleForTests") final String imageUrl = task.getResult().toString();
//                        mDatabase.child("image").setValue(imgName).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()) {
//                                    sendMessage(push_id, "image", imgName);
//                                }
//                                else {
//                                    progressDialog.dismiss();
//                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        });
//                    }
//                    else {
//                        progressDialog.dismiss();
//                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
//                    }
//
//                }
//            });
//       }
        //else if (resultCode == ImagePicker.RESULT_ERROR) {
//            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
//            // Uri object will not be null for RESULT_OK
//            Uri uri = data.getData();
//            final String current_user_ref = "Message/"+currentUserId+"/"+doctorId;
//            final String doctor_ref = "Message/"+doctorId+"/"+currentUserId;
//
//            DatabaseReference user_message_push = mDatabase.child("Message")
//                    .child(currentUserId).child(doctorId).push();
//
//            final String push_id = user_message_push.getKey();
//
//            String imgName = push_id+".jpg";
//            StorageReference imageRef = mImageStorage.child("message_image").child(imgName);
//            imageRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                    if(task.isSuccessful()){
//                        //@SuppressWarnings("VisibleForTests") final String imageUrl = task.getResult().toString();
//                        mDatabase.child("image").setValue(imgName).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()) {
//                                    Map messageMap = new HashMap();
//                                    messageMap.put("msg",imgName);
//                                    messageMap.put("seen",false);
//                                    messageMap.put("type","image");
//                                    messageMap.put("time",ServerValue.TIMESTAMP);
//                                    messageMap.put("from",currentUserId);
//                                    messageMap.put("to",doctorId);
//
//                                    Map messageUserMap = new HashMap();
//                                    messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
//                                    messageUserMap.put(doctor_ref+"/"+push_id,messageMap);
//
//                                    messageTextBox.setText("");
//
//                                    mDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
//                                        @Override
//                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                            if(databaseError != null){
//                                                Log.d(TAG,"Message sending failed for, database failure.");
//                                            }
//                                        }
//                                    });
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
//        } else if (resultCode == ImagePicker.RESULT_ERROR) {
//            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
//        }
//    }

//    @Override
//    public void onPickResult(PickResult pickResult) {
//        if (pickResult.getError() == null) {
//
//            Uri image = pickResult.getUri();
//
//            final String current_user_ref = "Message/"+currentUserId+"/"+doctorId;
//            final String doctor_ref = "Message/"+doctorId+"/"+currentUserId;
//
//            DatabaseReference user_message_push = mDatabase.child("Message")
//                    .child(currentUserId).child(doctorId).push();
//
//            final String push_id = user_message_push.getKey();
//
//            StorageReference imageRef = mImageStorage.child("message_image").child(push_id+".jpg");
//            imageRef.putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                    if(task.isSuccessful()){
//                        //@SuppressWarnings("VisibleForTests") final String imageUrl = task.getResult().toString();
//                        mDatabase.child("image").setValue(push_id+".jpg").addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()) {
//                                    Map messageMap = new HashMap();
//                                    messageMap.put("msg",push_id+".jpg");
//                                    messageMap.put("seen",false);
//                                    messageMap.put("type","image");
//                                    messageMap.put("time",ServerValue.TIMESTAMP);
//                                    messageMap.put("from",currentUserId);
//                                    messageMap.put("to",doctorId);
//
//                                    Map messageUserMap = new HashMap();
//                                    messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
//                                    messageUserMap.put(doctor_ref+"/"+push_id,messageMap);
//
//                                    messageTextBox.setText("");
//
//                                    mDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
//                                        @Override
//                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                            if(databaseError != null){
//                                                Log.d(TAG,"Message sending failed for, database failure.");
//                                            }
//                                        }
//                                    });
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
        getMenuInflater().inflate(R.menu.chat_menu,menu);

        return true;
    }

    private void backToActivity() {
        Class gotoClass;
        switch (fromActivity) {
            case "DoctorDetailsActivity":
                gotoClass = DoctorDetailsActivity.class;
                break;
            case "PatientCheckUpActivity":
                gotoClass = PatientCheckUpActivity.class;
                break;
            default:
                gotoClass = null;
                break;
        }
        if (gotoClass != null) {
            Intent intent = new Intent(ChatActivity.this, gotoClass);
            if (isFromDoctorDetails) {
                intent.putExtra("doctor_name", doctorName);
                intent.putExtra("doctor_id", doctorId);
                intent.putExtra("doctor_specialization", doctorSpecialization);
                intent.putExtra("doctor_image", doctorImgUrl);
            }
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        backToActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            backToActivity(); // close this activity and return to preview activity (if there is any)
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_profile){
            startActivity(new Intent(ChatActivity.this, DoctorProfileActivity.class).putExtra("doctor_id", doctorId));
           // finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(ChatActivity.this, PatientProfileSettingActivity.class));
            //finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_inbox){
            startActivity(new Intent(ChatActivity.this, PatientInboxActivity.class));
            //finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            mAuth.signOut();
            startActivity(new Intent(ChatActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return true;
    }

    @Override
    public void onSetValues(Bitmap bitmap, String title) {
        qrBitMap = bitmap;
        qrFileName = title;
    }
}
