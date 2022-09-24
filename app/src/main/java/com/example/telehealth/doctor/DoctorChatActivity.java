package com.example.telehealth.doctor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.telehealth.ChatActivity;
import com.example.telehealth.DoctorHomeActivity;
import com.example.telehealth.LoginActivity;
import com.example.telehealth.R;
import com.example.telehealth.adapters.MessageAdapter;
import com.example.telehealth.helpers.FileHelper;
import com.example.telehealth.interfaces.BitMapSetter;
import com.example.telehealth.models.Message;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.constant.ImageProvider;
import com.github.dhaval2404.imagepicker.listener.DismissListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class DoctorChatActivity extends AppCompatActivity implements BitMapSetter {

    public static final String TAG="DoctorChatActivity";
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
    //Firbase storage
    private StorageReference mImageStorage;
    // database
    private DatabaseReference mUserRef,mDatabase;
    private Query query;

    public String doctorName,doctorId;
    public String currentUserId,senderId;

    //Widget
    private ImageView sendImageButton,sendMessageButton;
    private EditText messageTextBox;

    //For loading message
    private static final int TOTAL_MSG_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey="";

    private Boolean isAttachedFileOnly = true;

    ProgressDialog progressDialog;

    Bitmap qrBitMap;
    String qrFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_chat);

        Intent i = getIntent();
        senderId = i.getStringExtra("sender_id");
        //    Toast.makeText(getApplicationContext(),senderId,Toast.LENGTH_SHORT).show();

        //Firebase storage
        mImageStorage = FirebaseStorage.getInstance().getReference();


        //Widget initialization
        sendImageButton = (ImageView) findViewById(R.id.send_image);
        sendMessageButton = (ImageView) findViewById(R.id.message_send_button);
        messageTextBox = (EditText) findViewById(R.id.send_text_message);

        progressDialog = new ProgressDialog(this);

        //RecyclerView

        mAdapter = new MessageAdapter(this, mMessageList, this);
        mMessageView = (RecyclerView) findViewById(R.id.message_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessageView.setLayoutManager(mLinearLayout);
        loadMessage();
        mMessageView.setAdapter(mAdapter);

        //Firebase init
        //Firebase Auth init
        mAuth = FirebaseAuth.getInstance();

        //Firebase init
        //Firebase Auth init
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        doctorId = mAuth.getCurrentUser().getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Doctors").child(doctorId);

        //Toolbar initialization
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);


        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        DatabaseReference userInfo = FirebaseDatabase.getInstance().getReference().child("Users");
        userInfo.child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setTitle(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

        //Chating Activity
        mDatabase.child("Chat").child(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(senderId)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+doctorId+"/"+senderId,chatAddMap);
                    chatUserMap.put("Chat/"+senderId+"/"+doctorId,chatAddMap);


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

    private String getPushKey() {
        DatabaseReference user_message_push = mDatabase.child("Message")
                .child(doctorId).child(senderId).push();

        return user_message_push.getKey();
    }

    //For sending message patient to doctor
    public void sendMessage(String push_id, String type, String message){

        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "Message/"+doctorId+"/"+senderId;
            String doctor_ref = "Message/"+senderId+"/"+doctorId;

            Map messageMap = new HashMap();
            messageMap.put("msg",message);
            messageMap.put("seen",false);
            messageMap.put("type",type);
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",doctorId);
            messageMap.put("to",senderId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(doctor_ref+"/"+push_id,messageMap);
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);


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

        String dId = mAuth.getCurrentUser().getUid();

        DatabaseReference retriveMessae = FirebaseDatabase.getInstance().getReference().child("Message").child(dId).child(senderId);
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

    //Send Image code
    public void sendAttachFile(View view){
        showDialog();
        //FileHelper.browsePhoto(this);
        isAttachedFileOnly = true;
    }

    public void sendQR(View view)
    {
        showDialog();
        //FileHelper.browsePhoto(this);
        isAttachedFileOnly = false;
    }

    private void showDialog()
    {
        ImagePicker.with(DoctorChatActivity.this)
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
    }

//    private void showDialog()
//    {
//        FragmentManager fm = getSupportFragmentManager();
//        SelectAttachFileFragment editNameDialogFragment = SelectAttachFileFragment.newInstance("Some Title");
//        editNameDialogFragment.show(fm, "fragment_edit_name");
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            switch(requestCode)
            {
                case 0:
                    FileHelper.saveBitmapToFile(qrBitMap, DoctorChatActivity.this, qrFileName);
                    break;
                case 1:
                   // FileHelper.browsePhoto(DoctorChatActivity.this);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Uri object will not be null for RESULT_OK
//            ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
//            MediaFile mediaFile = files.get(0);
//            Uri uri = mediaFile.getUri();
            Uri uri = data.getData();

            final String push_id = getPushKey();

            String fileName = push_id + ".jpg";

           // boolean isImage = requestCode == 101;
//            String fileName = isImage? (push_id + ".jpg") : files.get;
//            String dir = isImage? "images" : "documents";
            StorageReference imageRef = mImageStorage.child("message_attach").child("images").child(fileName);

            progressDialog.setMessage("Sending...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            imageRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                        mDatabase.child("image").setValue(fileName).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    sendMessage(push_id, isAttachedFileOnly? "image" : "qr_code", fileName);
                                }
                                else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
//
                        //@SuppressWarnings("VisibleForTests") final String imageUrl = task.getResult().toString();
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
//    public void onPickResult(PickResult pickResult) {
//        if (pickResult.getError() == null) {
//
//            Uri image = pickResult.getUri();
//
//            final String current_user_ref = "Message/"+doctorId+"/"+senderId;
//            final String doctor_ref = "Message/"+senderId+"/"+doctorId;
//
//            DatabaseReference user_message_push = mDatabase.child("Message")
//                    .child(doctorId).child(senderId).push();
//
//            final String push_id = user_message_push.getKey();
//
//            StorageReference imageRef = mImageStorage.child("message_image").child(push_id+".jpg");
//            imageRef.putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                    if(task.isSuccessful()){
//                        mDatabase.child("image").setValue(push_id+".jpg").addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()) {
//                                    Map messageMap = new HashMap();
//                                    messageMap.put("msg",push_id+".jpg");
//                                    messageMap.put("seen",false);
//                                    messageMap.put("type","image");
//                                    messageMap.put("time",ServerValue.TIMESTAMP);
//                                    messageMap.put("from",doctorId);
//                                    messageMap.put("to",senderId);
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
////                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
////                            @Override
////                            public void onSuccess(Uri uri) {
////                                String imageUrl = uri.toString();
////                            }
////                        });
//                        //@SuppressWarnings("VisibleForTests") final String imageUrl = task.getResult().toString();
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
        getMenuInflater().inflate(R.menu.prescribe_menu_bar,menu);

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
            startActivity(new Intent(DoctorChatActivity.this, DoctorProfileActivity.class));
            //finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_setting){
            startActivity(new Intent(DoctorChatActivity.this, DoctorSettingActivity.class));
            //finish();
            return true;
        }
        else if(item.getItemId()==R.id.main_menu_prescription){
//            Intent i = new Intent(getApplicationContext(), Prescription.class);
//            i.putExtra("sender_id", senderId);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(i);

            return true;
        }
        else if(item.getItemId()==R.id.main_menu_logout){
            FirebaseDatabase.getInstance().goOffline();
            mAuth.signOut();
            startActivity(new Intent(DoctorChatActivity.this, LoginActivity.class));
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
