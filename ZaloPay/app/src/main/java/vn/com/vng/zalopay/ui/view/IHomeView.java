package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import vn.com.vng.zalopay.event.PromotionEvent;

/**
 * Created by AnhHieu on 3/26/16.
 * *
 */
public interface IHomeView extends ILoadDataView {
    Activity getActivity();

    void refreshIconFont();

    void showCashBackView(PromotionEvent event);

    void hideCashBackView();

    boolean showingCashBackView();
}
