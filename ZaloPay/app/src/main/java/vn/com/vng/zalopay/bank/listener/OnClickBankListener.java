package vn.com.vng.zalopay.bank.listener;

import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by longlv on 1/20/17.
 * *
 */

public interface OnClickBankListener {
    void onClickBankItem(ZPCard card);
}
