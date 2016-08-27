package vn.com.vng.zalopay.transfer.ui.presenter;

import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.transfer.ui.view.ITransferView;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by huuhoa on 8/27/16.
 */
public interface TransferMoneyPresenter extends IPresenter<ITransferView> {
    void transferMoney(long amount, String message, ZaloFriend zaloFriend, MappingZaloAndZaloPay userMapZaloAndZaloPay);

    void transferMoney(long amount, String message, Person person);
    void updateAmount(String amount);
    void updateMessage(String message);
    void doTransfer();


}
