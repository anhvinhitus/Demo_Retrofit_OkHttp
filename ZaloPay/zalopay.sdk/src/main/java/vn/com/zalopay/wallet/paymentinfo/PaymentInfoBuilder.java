package vn.com.zalopay.wallet.paymentinfo;

import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;

/**
 * Created by chucvv on 6/5/17.
 */

public final class PaymentInfoBuilder implements IBuilder {
    public AbstractOrder order;
    @PaymentStatus
    public int status;//order status
    @TransactionType
    public int transactionType;
    public UserInfo origin_user;
    public UserInfo destination_user; // whom tranfer to
    public LinkAccInfo link_acc_info; // info about link/unlink bank account
    public int[] forceChannelIds;// force user go to 1 channel
    public PaymentLocation mLocation; // user location payment
    public DBaseMap mapBank; //info about map bank after user payment success
    public DMapCardResult mapCardResult;//notify about card map success in payment

    @Override
    public AbstractOrder getOrder() {
        return order;
    }

    @Override
    public IBuilder setOrder(AbstractOrder order) {
        this.order = order;
        return this;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public IBuilder setStatus(@PaymentStatus int status) {
        this.status = status;
        return this;
    }

    @Override
    public int getTransactionType() {
        return transactionType;
    }

    @Override
    public IBuilder setForceChannels(int[] forceChannelIds) {
        this.forceChannelIds = forceChannelIds;
        return this;
    }

    @Override
    public int[] getForceChannels() {
        return forceChannelIds;
    }

    @Override
    public DMapCardResult getMapCard() {
        return mapCardResult;
    }

    @Override
    public IBuilder setMapCard(DMapCardResult mapCard) {
        this.mapCardResult = mapCard;
        return this;
    }

    @Override
    public DBaseMap getMapBank() {
        return mapBank;
    }

    @Override
    public IBuilder setMapBank(DBaseMap mapBank) {
        this.mapBank = mapBank;
        return this;
    }

    @Override
    public IBuilder setLocation(PaymentLocation location) {
        this.mLocation = location;
        return this;
    }

    @Override
    public PaymentLocation getLocation() {
        return mLocation;
    }

    @Override
    public IBuilder setTransactionType(@TransactionType int pTransactionType) {
        this.transactionType = pTransactionType;
        return this;
    }

    @Override
    public UserInfo getUser() {
        return origin_user;
    }

    @Override
    public IBuilder setUser(UserInfo origin_user) {
        this.origin_user = origin_user;
        return this;
    }

    @Override
    public UserInfo getDestinationUser() {
        return destination_user;
    }

    @Override
    public IBuilder setDestinationUser(UserInfo destination_user) {
        this.destination_user = destination_user;
        return this;
    }

    @Override
    public LinkAccInfo getLinkAccountInfo() {
        return link_acc_info;
    }

    @Override
    public IBuilder setLinkAccountInfo(LinkAccInfo linkAccountInfo) {
        this.link_acc_info = linkAccountInfo;
        return this;
    }

    @Override
    public IPaymentInfo build() {
        return new PaymentInfo(this);
    }

    @Override
    public void release() {
        Log.d(this, "start release payment info");
        order = null;
        origin_user = null;
        destination_user = null;
        link_acc_info = null;
        mapBank = null;
        mapCardResult = null;
    }
}
