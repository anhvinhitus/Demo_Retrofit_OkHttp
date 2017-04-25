package vn.com.vng.zalopay.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import javax.annotation.Nullable;

import timber.log.Timber;

/**
 * Created by longlv on 4/20/17.
 * Wrapper fresco function.
 */

public class FrescoHelper {

    public static void setImage(ImageView imageView, String filepath) {
        if (imageView == null || TextUtils.isEmpty(filepath)) {
            return;
        }
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(UriUtil.getUriForFile(new File(filepath)))
                .setProgressiveRenderingEnabled(true)
                .build();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, imageView.getContext());

        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(@Nullable Bitmap bitmap) {
                if (bitmap == null) {
                    Timber.d("Get bitmap from file successfully but bitmap null, filepath [%s]", filepath);
                    return;
                }
                Timber.d("Get bitmap from file successfully [%s]", filepath);
                AndroidUtils.runOnUIThread(() -> imageView.setImageBitmap(bitmap));
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                Timber.d("Get bitmap from file fail, filepath [%s] dataSource [%s]",
                        filepath, dataSource);
            }
        }, CallerThreadExecutor.getInstance());
    }
}
