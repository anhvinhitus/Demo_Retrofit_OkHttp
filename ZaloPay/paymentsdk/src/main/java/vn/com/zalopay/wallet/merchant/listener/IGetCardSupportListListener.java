package vn.com.zalopay.wallet.merchant.listener;

import java.util.ArrayList;

import vn.com.zalopay.wallet.merchant.entities.ZPCard;

public interface IGetCardSupportListListener extends IMerchantListener {
    void onComplete(ArrayList<ZPCard> cardSupportArrayList);
}
