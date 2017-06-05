package vn.com.vng.zalopay.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.DrawableRes;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by AnhHieu on 9/15/16.
 * *
 */

public class FrescoImageLoader implements ImageLoader<SimpleDraweeView> {

    final Context context;

    public FrescoImageLoader(Context context) {
        this.context = context;
    }

    @Override
    public void loadImage(SimpleDraweeView target, String url, @DrawableRes int placeHolder, @DrawableRes int error, ScaleType scaleType) {
        GenericDraweeHierarchy hierarchy = target.getHierarchy();
        hierarchy.setPlaceholderImage(placeHolder);
        hierarchy.setFailureImage(error);

        target.setImageURI(url);
    }

    @Override
    public void loadImage(SimpleDraweeView target, @DrawableRes int resourceId) {
        Uri uri = new Uri.Builder()
                .scheme(com.facebook.common.util.UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(resourceId))
                .build();
        // target.getHierarchy().setPlaceholderImage(R.color.white);
        target.setImageURI(uri);
    }

    @Override
    public void loadImage(SimpleDraweeView target, String url, ControllerListener controllerListener) {
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(url)
                .setControllerListener(controllerListener)
                .build();
        target.setController(controller);
    }

    @Override
    public void loadImage(SimpleDraweeView target, String url) {
        target.setImageURI(url);
    }
}
