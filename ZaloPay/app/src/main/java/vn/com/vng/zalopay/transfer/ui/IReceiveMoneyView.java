package vn.com.vng.zalopay.transfer.ui;

import android.graphics.Bitmap;

import vn.com.vng.zalopay.domain.model.PersonTransfer;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by huuhoa on 8/28/16.
 * Interface for receiving money
 */

public interface IReceiveMoneyView extends ILoadDataView {
    void setQrImage(Bitmap image);

    void setUserInfo(String displayName, String avatar);

    void displayReceivedMoney(String senderDisplayName, String senderAvatar, long amount, String transactionId);

    void displayWaitForMoney();

    void addPersonTransfer(PersonTransfer person);

    void replacePersonTransfer(int position, PersonTransfer person);

    void insertPersonTransfer(int position, PersonTransfer person);
}
