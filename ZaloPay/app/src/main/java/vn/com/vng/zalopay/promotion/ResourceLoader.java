package vn.com.vng.zalopay.promotion;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.WrapContentController;

import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.zalopay.promotion.IResourceLoader;

public class ResourceLoader implements IResourceLoader {

    public ResourceLoader() {
        Timber.d("create resource loader for promotion");
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
}
