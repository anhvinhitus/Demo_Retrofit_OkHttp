package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by Duke on 5/10/17.
 */

public interface IHomeListAppView {
    Activity getActivity();

    void setAppItems(List<AppResource> list);

    void showWsConnectError();

    void showNetworkError();

    void hideNetworkError();

    void showError(String error);

    void showErrorDialog(String error);

    void showLoading();

    void hideLoading();

    void setRefreshing(boolean val);
}
