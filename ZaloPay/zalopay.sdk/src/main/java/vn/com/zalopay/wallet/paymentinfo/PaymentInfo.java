package vn.com.zalopay.wallet.paymentinfo;

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

public final class PaymentInfo implements IPaymentInfo {
    private IBuilder builder;

    public PaymentInfo(IBuilder pBuilder) {
        this.builder = pBuilder;
    }

    public static IBuilder getBuilder() {
        return new PaymentInfoBuilder();
    }

    @Override
    public AbstractOrder getOrder() {
        return builder != null ? builder.getOrder() : null;
    }

    @Override
    public int getTranstype() {
        return builder != null ? builder.getTransactionType() : TransactionType.PAY;
    }

    @Override
    public UserInfo getUser() {
        return builder != null ? builder.getUser() : null;
    }

    @Override
    public UserInfo getDestinationUser() {
        return builder != null ? builder.getDestinationUser() : null;
    }

    @Override
    public LinkAccInfo getLinkAccoutInfo() {
        return builder != null ? builder.getLinkAccountInfo() : null;
    }

    @Override
    public DBaseMap getMapBank() {
        return builder != null ? builder.getMapBank() : null;
    }

    @Override
    public void setMapBank(DBaseMap mapBank) {
        if (builder != null) {
            builder.setMapBank(mapBank);
        }
    }

    @Override
    public void setMapCard(DMapCardResult mapCard) {
        if (builder != null) {
            builder.setMapCard(mapCard);
        }
    }

    @Override
    public int[] getForceChannels() {
        return builder != null ? builder.getForceChannels() : null;
    }

    @Override
    public PaymentLocation getLocation() {
        return builder != null ? builder.getLocation() : null;
    }

    @Override
    public void putPaymentStatus(@PaymentStatus int payment_status) {
        if (builder != null) {
            builder.setStatus(payment_status);
        }
    }

    @Override
    public int getPaymentStatus() {
        return builder != null ? builder.getStatus() : PaymentStatus.PROCESSING;
    }
}
