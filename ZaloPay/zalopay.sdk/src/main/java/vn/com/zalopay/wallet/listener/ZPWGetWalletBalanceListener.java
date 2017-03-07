package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.zalopay.ZaloPayBalance;


public interface ZPWGetWalletBalanceListener {
    public void onGetBalanceComplete(ZaloPayBalance message);
}
