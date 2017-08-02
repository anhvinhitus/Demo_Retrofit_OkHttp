package vn.com.vng.zalopay.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.GridLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.R;

/**
 * Created by hieuvm on 7/21/17.
 * *
 */

public class ImageGroupView extends GridLayout {

    @BindView(R.id.image)
    SimpleDraweeView mDraweeView;
    @BindView(R.id.image1)
    SimpleDraweeView mDraweeView1;
    @BindView(R.id.image2)
    SimpleDraweeView mDraweeView2;
    @BindView(R.id.image3)
    SimpleDraweeView mDraweeView3;

    public ImageGroupView(@NonNull Context context) {
        this(context, null);
    }

    public ImageGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageGroupView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            return;
        }

        inflate(context, R.layout.image_group_view, this);
        setColumnCount(2);
        ButterKnife.bind(this);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        super.onMeasure(MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
    }

    public void setImageURI(String uriString, String uriString1, String uriString2, String uriString3) {
//        Timber.d("setImageURI: %s %s %s", uriString, uriString1, uriString2);
        mDraweeView.setImageURI(uriString);
        mDraweeView1.setImageURI(uriString1);
        mDraweeView2.setImageURI(uriString2);
        //  mDraweeView3.setImageURI(uriString3);
    }
}
