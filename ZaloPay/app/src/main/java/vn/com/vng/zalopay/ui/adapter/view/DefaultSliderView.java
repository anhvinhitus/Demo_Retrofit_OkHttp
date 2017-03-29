package vn.com.vng.zalopay.ui.adapter.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.slider.BaseSliderView;

import vn.com.vng.zalopay.R;

/**
 * Created by hieuvm on 3/20/17.
 * *
 */

public class DefaultSliderView extends BaseSliderView {

    public DefaultSliderView(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        SimpleDraweeView target = (SimpleDraweeView) LayoutInflater.from(getContext()).inflate(R.layout.banner_item, null);
        bindEventAndShow(target, target);
        return target;
    }
}