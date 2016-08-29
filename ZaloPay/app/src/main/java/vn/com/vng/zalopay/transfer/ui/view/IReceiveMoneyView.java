package vn.com.vng.zalopay.transfer.ui.view;

import android.graphics.Bitmap;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by huuhoa on 8/28/16.
 * Interface for receiving money
 */

public interface IReceiveMoneyView extends ILoadDataView {
    void setQrImage(Bitmap image);
    void setUserInfo(String displayName, String avatar);
    void displayReceivedMoney();
    void displayWaitForMoney();

    void setReceiverInfo(String displayName, String avatar);

    void setReceivedMoney(String displayName, String avatar, long amount);
}
