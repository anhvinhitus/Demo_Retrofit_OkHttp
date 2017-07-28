package vn.com.zalopay.wallet.paymentinfo;

import timber.log.Timber;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.voucher.VoucherInfo;

/**
 * Created by chucvv on 6/5/17.
 */

public final class PaymentInfoBuilder implements IBuilder {
    private AbstractOrder order;
    private VoucherInfo voucherInfo;
    @PaymentStatus
    public int status;//order status
    @TransactionType
    private int transactionType;
    private UserInfo origin_user;
    private UserInfo destination_user; // whom tranfer to
    private LinkAccInfo link_acc_info; // info about link/unlink bank account
    private int[] forceChannelIds;// force user go to 1 channel
    private PaymentLocation mLocation; // user location payment
    private BaseMap mapBank; //info about map bank after user payment success
    private DMapCardResult mapCardResult;//notify about card map success in payment
    @CardType
    private String mCardTypeLink;//bank code to link bank

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
    public VoucherInfo getVoucher() {
        return voucherInfo;
    }

    @Override
    public IBuilder setVoucher(VoucherInfo voucherInfo) {
        this.voucherInfo = voucherInfo;
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
    public IBuilder setCardTypeLink(@CardType String pCardType) {
        mCardTypeLink = pCardType;
        return this;
    }

    @Override
    public String getCardTypeLink() {
        return mCardTypeLink;
    }

    @Override
    public IBuilder setMapCard(DMapCardResult mapCard) {
        this.mapCardResult = mapCard;
        return this;
    }

    @Override
    public BaseMap getMapBank() {
        return mapBank;
    }

    @Override
    public IBuilder setMapBank(BaseMap mapBank) {
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
    public boolean isLinkAccount() {
        return link_acc_info != null && link_acc_info.isLinkAcc();
    }

    @Override
    public boolean isUnLinkAccount() {
        return link_acc_info != null && link_acc_info.isUnlinkAcc();
    }

    @Override
    public void release() {
        Timber.d("start release payment info");
        order = null;
        origin_user = null;
        destination_user = null;
        link_acc_info = null;
        mapBank = null;
        mapCardResult = null;
        mCardTypeLink = null;
    }
}
