package vn.com.vng.zalopay.transfer.ui;

import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by huuhoa on 8/27/16.
 * Controller for money transfer
 */
public interface TransferMoneyPresenter extends IPresenter<ITransferView> {
    /**
     * Update money amount as user input
     * @param amount Money amount
     */
    void updateAmount(long amount);

    /**
     * Update message as user input
     * @param message message
     */
    void updateMessage(String message);

    /**
     * Invoke transfer process.
     * + First check to see if all inputs are validated
     * + Call api to create money transfer order
     * + Call SDK to initiate payment process
     */
    void doTransfer();

    void onViewCreated();
    void initView(ZaloFriend zaloFriend, RecentTransaction recentTransaction, Long amount, String message);
    void navigateBack();
    void setTransferMode(int anInt);
}
