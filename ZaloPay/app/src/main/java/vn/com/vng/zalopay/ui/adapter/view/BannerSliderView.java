package vn.com.vng.zalopay.ui.adapter.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.slider.BaseSliderView;

import java.lang.ref.WeakReference;

import butterknife.internal.DebouncingOnClickListener;
import vn.com.vng.zalopay.R;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;

/**
 * Created by hieuvm on 3/20/17.
 * *
 */

public class BannerSliderView implements BaseSliderView {

    public interface OnSliderClickListener {
        void onSliderClick(DBanner banner);
    }

    private OnSliderClickListener mOnSliderClickListener;

    protected final WeakReference<Context> mContext;

    private final DBanner mBanner;

    public BannerSliderView(@NonNull Context context,@NonNull DBanner banner) {
        mContext = new WeakReference<>(context);
        mBanner = banner;
    }

    @Override
    public View getView() {
        SimpleDraweeView target = (SimpleDraweeView) LayoutInflater.from(mContext.get()).inflate(R.layout.banner_item, null);
        target.setImageURI(mBanner.logourl);
        target.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                if (mOnSliderClickListener != null) {
                    mOnSliderClickListener.onSliderClick(mBanner);
                }
            }
        });
        return target;
    }


    public void setOnSliderClickListener(OnSliderClickListener l) {
        mOnSliderClickListener = l;
    }
}