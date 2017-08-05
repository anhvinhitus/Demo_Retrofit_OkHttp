package vn.com.zalopay.wallet.paymentinfo;

import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.voucher.VoucherInfo;

/**
 * Created by chucvv on 6/5/17.
 */

public final class PaymentInfo implements IPaymentInfo {
    private IBuilder builder;

    public PaymentInfo(IBuilder pBuilder) {
        this.builder = pBuilder;
    }

    public static IBuilder newBuilder() {
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
    public UserInfo getMoneyTransferReceiverInfo() {
        return builder != null ? builder.getDestinationUser() : null;
    }

    @Override
    public LinkAccInfo getLinkAccountInfo() {
        return builder != null ? builder.getLinkAccountInfo() : null;
    }

    @Override
    public BaseMap getMapBank() {
        return builder != null ? builder.getMapBank() : null;
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

    @Override
    public IBuilder getBuilder() {
        return builder;
    }

    @Override
    public String getCardTypeLink() {
        return builder != null ? builder.getCardTypeLink() : null;
    }

    @Override
    public void setCardLinkType(String cardTypeLink) {
        if (builder != null) {
            builder.setCardTypeLink(cardTypeLink);
        }
    }

    @Override
    public void putVoucher(VoucherInfo voucherInfo) {
        if (builder != null) {
            builder.setVoucher(voucherInfo);
        }
    }

    @Override
    public VoucherInfo getVoucher() {
        return builder != null ? builder.getVoucher() : null;
    }
}
