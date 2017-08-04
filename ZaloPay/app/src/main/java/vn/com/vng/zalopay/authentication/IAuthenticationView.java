package vn.com.vng.zalopay.authentication;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 12/27/16.
 */

interface IAuthenticationView extends ILoadDataView {
    void initView();

    void dismiss();

    void onAuthenticated(String password);

    void onAuthenticationFailure();

    void showFingerprintError(CharSequence error, boolean retry);

    void showFingerprintSuccess();
}
