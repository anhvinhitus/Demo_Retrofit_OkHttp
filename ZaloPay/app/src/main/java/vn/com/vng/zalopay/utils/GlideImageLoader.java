package vn.com.vng.zalopay.utils;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by AnhHieu on 9/15/16.
 * *
 */

public class GlideImageLoader implements ImageLoader<ImageView> {

    private Context context;

    public GlideImageLoader(Context context) {
        this.context = context;
    }

    @Override
    public void loadImage(ImageView target, String url, @DrawableRes int placeHolder, @DrawableRes int error, ScaleType scaleType) {
        DrawableTypeRequest<String> request = Glide.with(context).load(url);

        request.placeholder(placeHolder)
                .error(error);

        if (scaleType == ScaleType.CENTER) {
            request.centerCrop();
        } else if (scaleType == ScaleType.FIT_CENTER) {
            request.fitCenter();
        }

        request.into(target);
    }

    @Override
    public void loadImage(ImageView target, @DrawableRes int resourceId) {

    }

    @Override
    public void loadImage(ImageView target, String url) {
        Glide.with(context)
                .load(url)
                .centerCrop()
                .into(target);
    }
}
