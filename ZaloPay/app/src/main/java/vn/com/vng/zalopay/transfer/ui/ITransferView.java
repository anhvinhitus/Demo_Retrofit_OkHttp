package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;

import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 13/06/2016.
 *
 */
public interface ITransferView extends ILoadDataView {
    Activity getActivity();
    void onTokenInvalid();
    void setEnableBtnContinue(boolean isEnable);
    void updateReceiverInfo(String displayName, String avatar, String zalopayName, String phoneNumber);
    void updateReceiverInfo(String phoneNumber);
    void toggleAmountError(String error);

    void setInitialValue(long currentAmount, String currentMessage);

    void showDialogThenClose(String content, String title, int dialogType);
}
