package vn.com.zalopay.wallet.merchant.listener;

import java.util.ArrayList;

import vn.com.zalopay.wallet.merchant.entities.ZPBank;

public interface IGetCardSupportListListener extends IMerchantListener {
    void onComplete(ArrayList<ZPBank> cardSupportArrayList);
}
