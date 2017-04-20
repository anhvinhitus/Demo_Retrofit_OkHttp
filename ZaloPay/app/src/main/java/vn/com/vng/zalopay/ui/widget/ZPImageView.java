package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;

import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;

/**
 * Created by longlv on 9/30/16.
 * Show image from local png
 */

public class ZPImageView extends SimpleDraweeView {

    public ZPImageView(Context context) {
        super(context);
    }

    public ZPImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public ZPImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ZPImageView, 0, 0);
        String mFileName;
        try {
            mFileName = typedArray.getString(R.styleable.ZPImageView_fileName);
        } finally {
            typedArray.recycle();
        }
        setDrawable(mFileName);
    }

    private void setDrawable(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Timber.d("setDrawable fail, fileName null or empty.");
            return;
        }

        /*String filePath = "file://" +
                ResourceHelper.getResource(getContext(), BuildConfig.ZALOPAY_APP_ID, fileName + ".png");
        Timber.d("setDrawable filePath [%s]", filePath);
        Uri imageUri = Uri.parse(filePath);
        setImageURI(imageUri);*/

        if (!fileName.endsWith(Constants.FILE_PNG)) {
            fileName += Constants.FILE_PNG;
        }

        //setImageBitmap(ResourceHelper.getBitmap(getContext(), BuildConfig.ZALOPAY_APP_ID, fileName));
        String filePath = ResourceHelper.getResource(getContext(), BuildConfig.ZALOPAY_APP_ID, fileName);
        Timber.d("setDrawable filePath 2 [%s]", filePath);
        setImageURI(UriUtil.getUriForFile(new File(filePath)));
    }
}