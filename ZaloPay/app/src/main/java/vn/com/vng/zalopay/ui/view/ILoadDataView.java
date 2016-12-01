
package vn.com.vng.zalopay.ui.view;

import android.content.Context;


public interface ILoadDataView {

    void showLoading();

    void hideLoading();

    void showRetry();

    void hideRetry();

    void showError(String message);

    void showWarning(String message);

    Context getContext();

}
