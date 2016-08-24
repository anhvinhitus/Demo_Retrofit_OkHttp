package vn.com.vng.zalopay.transfer.ui.view;

import android.app.Activity;

import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 13/06/2016.
 */
public interface ITransferView extends ILoadDataView {
    Activity getActivity();
    void onTokenInvalid();
    void setEnableBtnContinue(boolean isEnable);
    void onGetMappingUserSuccess(MappingZaloAndZaloPay userMapZaloAndZaloPay);
    void onGetMappingUserError();
}
