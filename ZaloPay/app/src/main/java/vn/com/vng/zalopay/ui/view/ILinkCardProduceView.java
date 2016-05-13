package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

/**
 * Created by AnhHieu on 5/11/16.
 */
public interface ILinkCardProduceView {
    Activity getActivity();

    void showLoading();

    void hideLoading();
}
