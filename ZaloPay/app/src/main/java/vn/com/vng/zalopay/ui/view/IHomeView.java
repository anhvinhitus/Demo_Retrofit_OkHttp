package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import vn.zalopay.promotion.IBuilder;
import vn.zalopay.promotion.PromotionEvent;
import vn.zalopay.promotion.RenderBuilder;

/**
 * Created by AnhHieu on 3/26/16.
 * *
 */
public interface IHomeView extends ILoadDataView {
    Activity getActivity();

    void refreshIconFont();

    void showCashBackView(IBuilder builder, PromotionEvent event);
}
