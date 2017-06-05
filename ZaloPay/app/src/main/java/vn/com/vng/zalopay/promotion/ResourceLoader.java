package vn.com.vng.zalopay.promotion;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.zalopay.promotion.IResourceLoader;

public class ResourceLoader implements IResourceLoader {

    public ResourceLoader() {
        Timber.d("create resource loader for promotion");
    }

    /***
     * Emulate the support WRAP_CONTENT
     * @param imageView
     * @param imageInfo
     */
    protected static void updateViewSize(ImageView imageView, @Nullable ImageInfo imageInfo) {
        if (imageInfo != null && imageView instanceof SimpleDraweeView) {
            imageView.getLayoutParams().width = imageInfo.getWidth();
            imageView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            ((SimpleDraweeView) imageView).setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
        }
    }

    @Override
    public void loadImage(Context mContext, ImageView pImageView, @StringRes int pResourceName) {
        if (pImageView == null) {
            return;
        }
        if (pResourceName == 0) {
            pImageView.setImageDrawable(null);
        } else {
            String iconName = mContext.getString(pResourceName);
            String iconPath = ResourceHelper.getResource(mContext, BuildConfig.ZALOPAY_APP_ID, iconName);
            iconPath = String.format("file://%s", iconPath);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(iconPath)
                    .setControllerListener(new WrapContentController(pImageView))
                    .build();
            if (pImageView instanceof SimpleDraweeView) {
                ((SimpleDraweeView) pImageView).setController(controller);
            }
        }
    }

    public static class WrapContentController extends BaseControllerListener {
        private WeakReference<ImageView> imageViewWeakReference;

        public WrapContentController(ImageView pImageView) {
            imageViewWeakReference = new WeakReference<>(pImageView);
        }

        @Override
        public void onFinalImageSet(String id, @javax.annotation.Nullable Object imageInfo, @javax.annotation.Nullable Animatable animatable) {
            super.onFinalImageSet(id, imageInfo, animatable);
            if (imageViewWeakReference.get() != null) {
                updateViewSize(imageViewWeakReference.get(), (ImageInfo) imageInfo);
            }
        }

        @Override
        public void onIntermediateImageSet(String id, @javax.annotation.Nullable Object imageInfo) {
            super.onIntermediateImageSet(id, imageInfo);
            if (imageViewWeakReference.get() != null) {
                updateViewSize(imageViewWeakReference.get(), (ImageInfo) imageInfo);
            }
        }
    }
}
