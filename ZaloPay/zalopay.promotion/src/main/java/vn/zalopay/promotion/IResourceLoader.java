package vn.zalopay.promotion;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.ImageView;

public interface IResourceLoader {
    void loadImage(Context mContext, ImageView pImageView, @StringRes int pResourceName);
}
