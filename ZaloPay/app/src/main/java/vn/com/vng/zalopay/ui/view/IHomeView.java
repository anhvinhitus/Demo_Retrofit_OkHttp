package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import vn.zalopay.promotion.IBuilder;
import vn.zalopay.promotion.model.PromotionEvent;

/**
 * Created by AnhHieu on 3/26/16.
 * *
 */
public interface IHomeView extends ILoadDataView {
    Activity getActivity();

    void showCashBackView(IBuilder builder, PromotionEvent event);

    void showBadgePreferential();
}
