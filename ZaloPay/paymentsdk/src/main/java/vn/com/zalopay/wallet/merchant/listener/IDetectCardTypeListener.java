package vn.com.zalopay.wallet.merchant.listener;

import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;

public interface IDetectCardTypeListener extends IMerchantListener {
    void onComplete(ECardType pCardType);
}
