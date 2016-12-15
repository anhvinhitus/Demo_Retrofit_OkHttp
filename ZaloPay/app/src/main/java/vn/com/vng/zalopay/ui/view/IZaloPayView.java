package vn.com.vng.zalopay.ui.view;

import android.app.Activity;
import android.content.Context;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 5/9/16.
 * *
 */
public interface IZaloPayView {

    Context getContext();

    Activity getActivity();

    void refreshInsideApps(List<AppResource> list);

    void setTotalNotify(int total);

    void showWsConnectError();

    void showNetworkError();

    void hideNetworkError();

    void setBalance(long balance);

    void showError(String error);

    void showErrorDialog(String error);

    void showLoading();

    void hideLoading();

    void enableShowShow(boolean isEnable);
}
