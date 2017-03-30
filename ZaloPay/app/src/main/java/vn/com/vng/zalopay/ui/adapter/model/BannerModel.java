package vn.com.vng.zalopay.ui.adapter.model;

import android.view.View;

import com.airbnb.epoxy.SimpleEpoxyModel;
import com.zalopay.ui.widget.slider.SliderLayout;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.adapter.HomeAdapter;
import vn.com.vng.zalopay.ui.adapter.view.BannerSliderView;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;

/**
 * Created by hieuvm on 3/21/17.
 * BannerModel
 */

public class BannerModel extends SimpleEpoxyModel {

    private List<DBanner> banners;
    private SliderLayout mSliderLayout;

    private HomeAdapter.OnClickAppItemListener clickListener;

    public BannerModel() {
        super(R.layout.row_banner_layout);
        this.banners = new ArrayList<>();
    }

    @Override
    public void bind(View itemView) {
        super.bind(itemView);
        mSliderLayout = (SliderLayout) itemView;
        SliderLayout sliderLayout = (SliderLayout) itemView;
        List<BannerSliderView> list = new ArrayList<>();
        for (DBanner banner : banners) {
            if (banner == null) {
                continue;
            }

            BannerSliderView slider = new BannerSliderView(banner);
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

    private final BannerSliderView.OnSliderClickListener sliderClickListener = banner -> {
        if (clickListener != null) {
            clickListener.onClickBanner(banner, banners.indexOf(banner));
        }
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
