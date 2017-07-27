package vn.com.zalopay.wallet.paymentinfo;

import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;

/**
 * Created by chucvv on 6/5/17.
 */

public interface IBuilder {
    AbstractOrder getOrder();

    IBuilder setOrder(AbstractOrder order);

    @PaymentStatus
    int getStatus();

    IBuilder setStatus(@PaymentStatus int status);

    @TransactionType
    int getTransactionType();

    IBuilder setForceChannels(int[] forceChannelIds);

    int[] getForceChannels();

    DMapCardResult getMapCard();

    IBuilder setCardTypeLink(@CardType String pCardType);

    @CardType String getCardTypeLink();

    IBuilder setMapCard(DMapCardResult mapCard);

    BaseMap getMapBank();

    IBuilder setMapBank(BaseMap mapBank);

    IBuilder setLocation(PaymentLocation location);

    PaymentLocation getLocation();

    IBuilder setTransactionType(@TransactionType int pTransactionType);

    UserInfo getUser();

    IBuilder setUser(UserInfo origin_user);

    UserInfo getDestinationUser();

    IBuilder setDestinationUser(UserInfo destination_user);

    LinkAccInfo getLinkAccountInfo();

    boolean isLinkAccount();
    boolean isUnLinkAccount();


    IBuilder setLinkAccountInfo(LinkAccInfo linkAccountInfo);

    IPaymentInfo build();

    void release();
}
