package vn.com.zalopay.wallet.paymentinfo;

import vn.com.zalopay.wallet.entity.PaymentLocation;
import vn.com.zalopay.wallet.entity.bank.BaseMap;
import vn.com.zalopay.wallet.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;

/**
 * Created by chucvv on 6/5/17.
 */

public interface IPaymentInfo {
    AbstractOrder getOrder();

    @TransactionType
    int getTranstype();

    UserInfo getUser();

    /**
     * whom tranfer to
     */
    UserInfo getMoneyTransferReceiverInfo();

    /**
     * info about link/unlink bank account
     */
    LinkAccInfo getLinkAccountInfo();

    BaseMap getMapBank();

    int[] getForceChannels();

    PaymentLocation getLocation();

    void putPaymentStatus(@PaymentStatus int payment_status);

    @PaymentStatus
    int getPaymentStatus();

    IBuilder getBuilder();

    @CardType
    String getCardTypeLink();

    void putVoucher(VoucherInfo voucherInfo);

    VoucherInfo getVoucher();

    void setCardLinkType(String cardTypeLink);
}
