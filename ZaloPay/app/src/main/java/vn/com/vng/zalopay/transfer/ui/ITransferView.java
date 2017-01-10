package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;

import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 13/06/2016.
 * Interface of TransferPresenter
 */
public interface ITransferView extends ILoadDataView {
    Activity getActivity();
    Fragment getFragment();

    void setEnableBtnContinue(boolean isEnable);

    /**
     * Set Receiver info when view had created
     *
     * @param displayName displayName
     * @param avatar      avatar
     * @param zalopayName If zaloPayName isn't not null or empty then set zaloPayName to view
     */
    void setReceiverInfo(String displayName, String avatar, String zalopayName);

    /**
     * Set Receiver info when server return user info
     *
     * @param displayName displayName
     * @param avatar      avatar
     * @param zalopayName If zaloPayName isn't not null or empty then set zaloPayName to view else invisible zaloPayName
     */
    void updateReceiverInfo(String displayName, String avatar, String zalopayName);

    void setInitialValue(long currentAmount, String currentMessage);

    void showDialogThenClose(String content, String title, int dialogType);

    void confirmTransferUnRegistryZaloPay();

    void setMinMaxMoney(long min, long max);
}
