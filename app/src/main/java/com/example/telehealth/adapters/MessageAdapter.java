package com.example.telehealth.adapters;

import static android.content.Context.WINDOW_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.telehealth.Helper;
import com.example.telehealth.R;
import com.example.telehealth.helpers.FileHelper;
import com.example.telehealth.helpers.FirebaseStorageHelper;
import com.example.telehealth.helpers.GlideHelper;
import com.example.telehealth.interfaces.BitMapSetter;
import com.example.telehealth.models.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> mMessageList;
    private FirebaseAuth mAuth;
    private Context context;

    Bitmap bitmap;

    BitMapSetter bitMapSetter;

    public MessageAdapter(Context context,List<Message> mMessageList, BitMapSetter bitMapSetter) {
        this.context = context;
        this.mMessageList = mMessageList;
        this.bitMapSetter = bitMapSetter;
    }


    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        String current_user = mAuth.getCurrentUser().getUid();

        Message msg = mMessageList.get(position);

        String from_user = msg.getFrom();

        CircleImageView profileImg = from_user.equals(current_user)? holder.sProfileImage : holder.rProfileImage;

        FirebaseStorageHelper.loadChatProfilePhoto(context, profileImg, from_user, (from_user + ".jpg"), true);

//        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("profile_image").child(from_user).child(from_user + ".jpg");
//        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                GlideHelper.loadChatProfileImage(context, profileImg, uri);
//            }
//        })
//        .addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle any errors
//                if (exception instanceof StorageException &&
//                        ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
//                    profileImg.setImageResource(R.drawable.ic_person_black_24dp);
//                    Log.d("OnFailure", "File not exist");
//                }
//            }
//        });


        if(from_user.equals(current_user)){
            holder.viewSender.setVisibility(View.VISIBLE);
            holder.viewReceiver.setVisibility(View.GONE);
//            holder.sTextMessage.setBackgroundColor(Color.WHITE);
//            holder.sTextMessage.setTextColor(Color.BLACK);
//            holder.rTextMessage.setBackgroundColor(Color.WHITE);
//            holder.rTextMessage.setTextColor(Color.BLACK);
        }else {
            holder.viewSender.setVisibility(View.GONE);
            holder.viewReceiver.setVisibility(View.VISIBLE);
//            holder.sTextMessage.setBackgroundResource(R.drawable.message_text_background);
//            holder.sTextMessage.setTextColor(Color.WHITE);
//            holder.rTextMessage.setBackgroundResource(R.drawable.message_text_background);
//            holder.rTextMessage.setTextColor(Color.WHITE);
        }

        String msg_type = msg.getType();

        if(msg_type.equals("text")){

            if(from_user.equals(current_user)) {
                holder.sTextMessage.setText(msg.getMsg());
                holder.sTextMessage.setVisibility(View.VISIBLE);
                holder.sSendImage.setVisibility(View.GONE);
            }else{
                holder.rTextMessage.setText(msg.getMsg());
                holder.rTextMessage.setVisibility(View.VISIBLE);
                holder.rSendImage.setVisibility(View.GONE);
            }
        }
        else{

            if(from_user.equals(current_user)) {
                holder.sSendImage.setVisibility(View.VISIBLE);
                holder.sTextMessage.setVisibility(View.GONE);
            }else {
                holder.rSendImage.setVisibility(View.VISIBLE);
                holder.rTextMessage.setVisibility(View.GONE);
            }

            ImageView sendMsgImg = from_user.equals(current_user) ? holder.sSendImage : holder.rSendImage;

            if(msg_type.equals("image"))
            {
                FirebaseStorageHelper.loadChatMessagePhoto(context, sendMsgImg, msg.getMsg(), false);
            }else
            {
                Button btnQr = from_user.equals(current_user) ? holder.sDLQr : holder.rDLQr;
                StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("message_attach").child("images").child(msg.getMsg());
                profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        WindowManager manager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

                        // initializing a variable for default display.
                        Display display = manager.getDefaultDisplay();

                        // creating a variable for point which
                        // is to be displayed in QR Code.
                        Point point = new Point();
                        display.getSize(point);

                        // getting width and
                        // height of a point
                        int width = point.x;
                        int height = point.y;

                        // generating dimension from width and height.
                        int dimen = width < height ? width : height;
                        dimen = dimen * 3 / 4;
                        QRGEncoder qrgEncoder = new QRGEncoder(uri.toString(), null, QRGContents.Type.TEXT, dimen);

                        // Getting QR-Code as Bitmap
                        bitmap = qrgEncoder.getBitmap();
                        // Setting Bitmap to ImageView
                        sendMsgImg.setImageBitmap(bitmap);

                        bitMapSetter.onSetValues(bitmap, msg.getMsg());

                        btnQr.setVisibility(View.VISIBLE);
                        btnQr.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                FileHelper.saveBitmapToFile(bitmap, context, msg.getMsg());

//                                String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
//                                if (hasPermissions(context, PERMISSIONS)) {
//                                //if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
////
////                                    ContentResolver cr = context.getContentResolver();
////                                    String title = "myBitmap";
////                                    String description = "My bitmap created by Android-er";
////                                    String savedURL = MediaStore.Images.Media
////                                            .insertImage(cr, bitmap, title, description);
////
////                                    Toast.makeText(context,
////                                            savedURL,
////                                            Toast.LENGTH_LONG).show();
//                                    String fileName = msg.getMsg();
//
//                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
//
//                                    File ExternalStorageDirectory = Environment.getExternalStorageDirectory();
//                                    File file = new File(ExternalStorageDirectory + File.separator + fileName);
//
//                                    FileOutputStream fileOutputStream = null;
//                                    try {
//                                        file.createNewFile();
//                                        fileOutputStream = new FileOutputStream(file);
//                                        fileOutputStream.write(bytes.toByteArray());
//
//                                        ContentResolver cr = context.getContentResolver();
//                                        String imagePath = file.getAbsolutePath();
//                                        String name = file.getName();
//                                        String description = "My bitmap created by Android-er";
//                                        String savedURL = MediaStore.Images.Media
//                                                .insertImage(cr, imagePath, name, description);
//
//                                        Toast.makeText(context,
//                                                savedURL,
//                                                Toast.LENGTH_LONG).show();
//
//                                    } catch (IOException e) {
//                                        // TODO Auto-generated catch block
//                                        e.printStackTrace();
//                                    } finally {
//                                        if(fileOutputStream != null){
//                                            try {
//                                                fileOutputStream.close();
//                                            } catch (IOException e) {
//                                                // TODO Auto-generated catch block
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    }
//
//                                    // FileHelper.saveToInternalStorage(bitmap, context);
//                                    //                                    try {
////                                        Toast.makeText(context, savePath, Toast.LENGTH_LONG).show();
////                                        boolean save = new QRGSaver().save(savePath, msg.getMsg(), bitmap, QRGContents.ImageType.IMAGE_JPEG);
////                                        String result = save ? "Image Saved" : "Image Not Saved";
////                                        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
////                                    } catch (Exception e) {
////                                        e.printStackTrace();
////                                    }
//                                } else {
//                                    final int REQUEST = 112;
//                                    ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, REQUEST );
//                                  //  ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
//                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        if (exception instanceof StorageException &&
                                ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            sendMsgImg.setImageResource(R.drawable.ic_person_black_24dp);
                            Log.d("OnFailure", "File not exist");
                        }
                    }
                });
            }
//            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("message_image").child(msg.getMsg());
//            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri uri) {
//                    // Got the download URL for 'users/me/profile.png'
//
//                    GlideHelper.loadChatMessageImage(context, sendMsgImg, uri);
//                }
//            })
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // Handle any errors
//                }
//            });
//            final long ONE_MEGABYTE = 2 * 1024 * 1024;
//            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                @Override
//                public void onSuccess(byte[] bytes) {
//                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                    holder.sProfileImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, holder.sProfileImage.getWidth(), holder.sProfileImage.getHeight(), false));
//                    // Data for "images/island.jpg" is returns, use this as needed
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // Handle any errors
//                    Log.d("exception", exception.getMessage());
//                }
//            });
            //Log.d("test", msg.getMsg());
           // Glide.with(context).load(msg.getMsg()).thumbnail(0.1f).into(holder.sProfileImage);
            //Picasso.with(context).load(msg.getMsg()).into(holder.sProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView sTextMessage;
        public CircleImageView sProfileImage;
        public ImageView sSendImage;
        public Button sDLQr;

        public TextView rTextMessage;
        public CircleImageView rProfileImage;
        public ImageView rSendImage;
        public Button rDLQr;

        public  RelativeLayout viewSender, viewReceiver;

        public MessageViewHolder(View itemView) {
            super(itemView);

            viewSender = (RelativeLayout) itemView.findViewById(R.id.viewSender);
            viewReceiver = (RelativeLayout) itemView.findViewById(R.id.viewReceiver);

            sTextMessage = (TextView) itemView.findViewById(R.id.sender_txt_msg);
            sProfileImage = (CircleImageView) itemView.findViewById(R.id.sender_message_sender_profile_image);
            sSendImage = (ImageView) itemView.findViewById(R.id.sender_msg_image);
            sDLQr = (Button) itemView.findViewById(R.id.sender_dl_qr);

            rTextMessage = (TextView) itemView.findViewById(R.id.receiver_txt_msg);
            rProfileImage = (CircleImageView) itemView.findViewById(R.id.receiver_message_sender_profile_image);
            rSendImage = (ImageView) itemView.findViewById(R.id.receiver_msg_image);
            rDLQr = (Button) itemView.findViewById(R.id.receiver_dl_qr);

        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}

