package vn.com.vng.zalopay.ui.adapter.model;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.airbnb.epoxy.SimpleEpoxyModel;
import com.zalopay.ui.widget.slider.BaseSliderView;
import com.zalopay.ui.widget.slider.SliderLayout;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.adapter.HomeAdapter;
import vn.com.vng.zalopay.ui.adapter.view.DefaultSliderView;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;

/**
 * Created by hieuvm on 3/21/17.
 * BannerModel
 */

public class BannerModel extends SimpleEpoxyModel {

    private static final String KEY_BANNER = "banner";

    private List<DBanner> banners;
    private final Context context;
    private SliderLayout mSliderLayout;

    private HomeAdapter.OnClickAppItemListener clickListener;

    public BannerModel(Context context) {
        super(R.layout.row_banner_layout);
        this.banners = new ArrayList<>();
        this.context = context;
    }

    @Override
    public void bind(View itemView) {
        super.bind(itemView);
        mSliderLayout = (SliderLayout) itemView;
        SliderLayout sliderLayout = (SliderLayout) itemView;
        List<DefaultSliderView> list = new ArrayList<>();
        for (DBanner banner : banners) {
            DefaultSliderView slider = new DefaultSliderView(context);
            slider.mUrl = banner.logourl;
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_BANNER, banner);

            slider.mBundle = bundle;

            slider.setOnSliderClickListener(sliderClickListener);
            list.add(slider);
        }
        sliderLayout.setSliders(list);
    }

    @Override
    public void unbind(View view) {
        super.unbind(view);
        ((SliderLayout) view).stopAutoCycle();
        mSliderLayout = null;
    }

    private final BaseSliderView.OnSliderClickListener sliderClickListener = slider -> {
        if (clickListener == null || slider.mBundle == null) {
            return;
        }

        DBanner banner = slider.mBundle.getParcelable(KEY_BANNER);
        if (banner == null) {
            return;
        }
        clickListener.onClickBanner(banner, banners.indexOf(banner));
    };

    public BannerModel setClickListener(HomeAdapter.OnClickAppItemListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    @Override
    public int getSpanSize(int totalSpanCount, int position, int itemCount) {
        return totalSpanCount;
    }

    public void setData(List<DBanner> banners) {
        if (banners == null) {
            return;
        }

        this.banners = banners;
    }


    public void pause() {
        if (mSliderLayout != null) {
            mSliderLayout.stopAutoCycle();
        }
    }

    public void resume() {
        if (mSliderLayout != null) {
            mSliderLayout.startAutoCycle();
        }
    }

    @Override
    public void onViewDetachedFromWindow(View view) {
        super.onViewDetachedFromWindow(view);
    }

    @Override
    public void onViewAttachedToWindow(View view) {
        super.onViewAttachedToWindow(view);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + banners.hashCode();
        return result;
    }
}
