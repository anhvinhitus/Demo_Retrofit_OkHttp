package vn.com.vng.zalopay.protect.ui;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 12/26/16.
 */

interface IProtectAccountView extends ILoadDataView {
    void setCheckedFingerprint(boolean var);

    void setCheckedTouchId(boolean checked);

    void setCheckedProtectAccount(boolean checked);

    void hideFingerprintLayout();
}
