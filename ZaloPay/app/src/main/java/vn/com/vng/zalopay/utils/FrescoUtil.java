package vn.com.vng.zalopay.utils;

import android.graphics.drawable.Animatable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by hieuvm on 7/10/17.
 * *
 */

public class FrescoUtil {

    public static void loadWrapContent(SimpleDraweeView draweeView, String filePath) {

        if (TextUtils.isEmpty(filePath)) {
            draweeView.setImageURI("");
            return;
        }

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(UriUtil.getUriForFile(new File(filePath)))
                .setControllerListener(new ControllerListener(draweeView))
                .build();
        draweeView.setController(controller);
    }

    private static class ControllerListener extends BaseControllerListener<ImageInfo> {
        private WeakReference<SimpleDraweeView> draweeView;

        ControllerListener(SimpleDraweeView pImageView) {
            draweeView = new WeakReference<>(pImageView);
        }

        @Override
        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
            if (draweeView.get() != null) {
                updateViewSize(draweeView.get(), imageInfo);
            }
        }

        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            if (draweeView.get() != null) {
                updateViewSize(draweeView.get(), imageInfo);
            }
        }

        @Override
        public void onIntermediateImageFailed(String id, Throwable throwable) {
            super.onIntermediateImageFailed(id, throwable);
        }

        private void updateViewSize(SimpleDraweeView draweeView, @Nullable ImageInfo imageInfo) {
            if (imageInfo != null) {
                draweeView.getLayoutParams().width = imageInfo.getWidth();
                draweeView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                draweeView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
            }
        }
    }

}
