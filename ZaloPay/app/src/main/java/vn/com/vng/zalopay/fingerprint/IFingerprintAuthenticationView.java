package vn.com.vng.zalopay.fingerprint;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 12/27/16.
 */

interface IFingerprintAuthenticationView extends ILoadDataView {

    void clearPassword();

    void onPinSuccess(String password);

    void showKeyboard();

    void updateStage(Stage stage);

    void dismiss();

    void onAuthenticated();

    void onAuthenticationFailure();

    FingerprintUiHelper getFingerprintUiHelper(Stage stage);
}
