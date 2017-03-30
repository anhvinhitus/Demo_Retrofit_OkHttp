package com.zalopay.ui.widget.slider;

import android.view.View;
import android.view.ViewGroup;

public interface BaseSliderView {

    View getView(ViewGroup container);

    void destroyItem(View view);

}
