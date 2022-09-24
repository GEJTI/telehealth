package com.example.telehealth.helpers;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.telehealth.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class GlideHelper {

    public static void loadChatMessageImage(Context context, ImageView imageView, Uri uri)
    {
        loadThumbnail(context, imageView, uri, 0.9f);
    }

    public static void loadChatProfileImage(Context context, CircleImageView imageView, Uri uri)
    {
        loadThumbnail(context, imageView, uri, 1f);
    }

    public static void loadThumbnail(Context context, View view, Uri uri){
        loadThumbnail(context, view, uri, 0.5f);
    }

    public static void loadThumbnail(Context context, View view, Uri uri, float thumbnail)
    {
        if(isValidContextForGlide(context))
        {
            Glide.with(context).load(uri).placeholder(R.drawable.loading_placeholder).thumbnail(thumbnail).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(((view instanceof ImageView)? (ImageView) view : (CircleImageView) view));
        }
    }

    public static void loadFullPhoto(Context context, View view, Uri uri)
    {
        if(isValidContextForGlide(context))
        {
            Glide.with(context).load(uri).placeholder(R.drawable.loading_placeholder).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(((view instanceof ImageView)? (ImageView) view : (CircleImageView) view));
        }
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }
}
