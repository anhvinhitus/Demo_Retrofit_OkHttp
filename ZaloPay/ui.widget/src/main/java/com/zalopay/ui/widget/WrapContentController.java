package com.zalopay.ui.widget;

import android.graphics.drawable.Animatable;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.lang.ref.WeakReference;

/*
 * Created by chucvv on 8/3/17.
 */

public class WrapContentController extends BaseControllerListener {
    private WeakReference<ImageView> imageViewWeakReference;

    public WrapContentController(ImageView pImageView) {
        imageViewWeakReference = new WeakReference<>(pImageView);
    }

    @Override
    public void onFinalImageSet(String id, Object imageInfo, Animatable animatable) {
        super.onFinalImageSet(id, imageInfo, animatable);
        if (imageViewWeakReference.get() != null) {
            updateViewSize(imageViewWeakReference.get(), (ImageInfo) imageInfo);
        }
    }

    @Override
    public void onIntermediateImageSet(String id, Object imageInfo) {
        super.onIntermediateImageSet(id, imageInfo);
        if (imageViewWeakReference.get() != null) {
            updateViewSize(imageViewWeakReference.get(), (ImageInfo) imageInfo);
        }
    }

    private void updateViewSize(ImageView imageView, @Nullable ImageInfo imageInfo) {
        if (imageInfo != null && imageView instanceof SimpleDraweeView) {
            imageView.getLayoutParams().width = imageInfo.getWidth();
            imageView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            ((SimpleDraweeView) imageView).setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
        }
    }
}
