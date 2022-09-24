package com.example.telehealth.helpers;

import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.telehealth.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirebaseStorageHelper {

    public static void loadProfilePhoto(Context context, View view, String uid, String filename, boolean hasErrorPhoto)
    {
        asyncProfilePhoto(context, view, uid, filename, hasErrorPhoto, "thumb_nail");
    }

    public static void loadChatProfilePhoto(Context context, View view, String uid, String filename, boolean hasErrorPhoto)
    {
        asyncProfilePhoto(context, view, uid, filename, hasErrorPhoto, "chat_profile_photo");
    }

    public static void loadChatMessagePhoto(Context context, View view, String filename, boolean hasErrorPhoto)
    {
        asyncMessagePhoto(context, view, filename, hasErrorPhoto, "chat_msg_photo");
    }

    public static void loadFullChatMessagePhoto(Context context, View view, String filename, boolean hasErrorPhoto)
    {
        asyncMessagePhoto(context, view, filename, hasErrorPhoto, "full_photo");
    }

    private static void asyncProfilePhoto(Context context, View view, String uid, String filename, boolean hasErrorPhoto, String type)
    {
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("profile_image").child(uid).child(filename);
        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                switch (type)
                {
                    case "chat_profile_photo":
                        GlideHelper.loadChatProfileImage(context, (CircleImageView) view, uri);
                        break;
                    case "full_photo":
                        GlideHelper.loadFullPhoto(context, view, uri);
                        break;
                    case "thumb_nail":
                        GlideHelper.loadThumbnail(context, view, uri);
                        break;
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                if(! hasErrorPhoto)
                {
                    return;
                }
                ((view instanceof ImageView)? (ImageView) view : (CircleImageView) view).setImageResource(R.drawable.ic_person_black_24dp);
//                if (exception instanceof StorageException &&
//                        ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
//                    ((view instanceof ImageView)? (ImageView) view : (CircleImageView) view).setImageResource(R.drawable.ic_person_black_24dp);
//                    Log.d("OnFailure", "File not exist");
//                }
            }
        });
    }

    private static void asyncMessagePhoto(Context context, View view, String filename, boolean hasErrorPhoto, String type)
    {
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("message_attach").child("images").child(filename);
        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                switch (type)
                {
                    case "chat_msg_photo":
                        GlideHelper.loadChatMessageImage(context, (ImageView) view, uri);
                        break;
                    case "full_photo":
                        GlideHelper.loadFullPhoto(context, view, uri);
                        break;
                    case "thumb_nail":
                        GlideHelper.loadThumbnail(context, view, uri);
                        break;
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                if(! hasErrorPhoto)
                {
                    return;
                }
                if (exception instanceof StorageException &&
                        ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    ((view instanceof ImageView)? (ImageView) view : (CircleImageView) view).setImageResource(R.drawable.ic_person_black_24dp);
                    Log.d("OnFailure", "File not exist");
                }
            }
        });
    }

    public static void asyncLicensePhoto(Context context, ImageView view, String uid, boolean hasErrorPhoto)
    {
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("license_image").child(uid).child(uid + ".jpg");
        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                GlideHelper.loadThumbnail(context, view, uri);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                if(! hasErrorPhoto)
                {
                    return;
                }
                if (exception instanceof StorageException &&
                        ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    view.setImageResource(R.drawable.ic_person_black_24dp);
                    Log.d("OnFailure", "File not exist");
                }
            }
        });
    }

    public static void asyncValidIdPhoto(Context context, ImageView view, String uid, boolean hasErrorPhoto)
    {
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("valid_id_image").child(uid).child(uid + ".jpg");
        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                GlideHelper.loadThumbnail(context, view, uri);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                if(! hasErrorPhoto)
                {
                    return;
                }
                if (exception instanceof StorageException &&
                        ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    view.setImageResource(R.drawable.ic_person_black_24dp);
                    Log.d("OnFailure", "File not exist");
                }
            }
        });
    }
}
