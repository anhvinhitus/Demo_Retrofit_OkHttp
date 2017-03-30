package vn.com.vng.zalopay.ui.adapter.view;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.slider.BaseSliderView;

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

    private final DBanner mBanner;

    public BannerSliderView(@NonNull DBanner banner) {
        mBanner = banner;
    }

    @Override
    public View getView(ViewGroup container) {
        SimpleDraweeView target = (SimpleDraweeView) LayoutInflater.from(container.getContext()).inflate(R.layout.banner_item, null);
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

    @Override
    public void destroyItem(View view) {

    }

    public void setOnSliderClickListener(OnSliderClickListener l) {
        mOnSliderClickListener = l;
    }
}