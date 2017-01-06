package vn.com.vng.zalopay.authentication;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 12/27/16.
 */

interface IAuthenticationView extends ILoadDataView {

    void clearPassword();

    void showKeyboard();

    void updateStage(Stage stage);

    void dismiss();

    void onAuthenticated(String password);

    void onAuthenticationFailure();

    void setErrorVerifyPassword(String error);

    void showFingerprintError(CharSequence error);

    void showFingerprintSuccess();
}
