package vn.com.zalopay.wallet.paymentinfo;

import android.content.Context;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.creditcard.CardSubmit;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.ui.BaseActivity;

import static vn.com.zalopay.wallet.business.error.ErrorManager.mErrorAccountArray;
import static vn.com.zalopay.wallet.business.error.ErrorManager.mErrorArray;
import static vn.com.zalopay.wallet.business.error.ErrorManager.mErrorLoginArray;

/*
 * Created by chucvv on 6/7/17.
 */

public class PaymentInfoHelper extends SingletonBase {
    private IPaymentInfo paymentInfo;

    public PaymentInfoHelper(IPaymentInfo paymentInfo) {
        super();
        this.paymentInfo = paymentInfo;
    }

    public
    @CardType
    String getCardTypeLink() {
        return paymentInfo != null ? paymentInfo.getCardTypeLink() : null;
    }

    public void setCardTypeLink(String cardTypeLink) {
        if (paymentInfo != null) {
            paymentInfo.setCardLinkType(cardTypeLink);
        }
    }

    public long getAppId() {
        if (getOrder() != null) {
            return getOrder().appid;
        }
        return BuildConfig.ZALOPAY_APPID;
    }

    public VoucherInfo getVoucher() {
        return paymentInfo != null ? paymentInfo.getVoucher() : null;
    }

    // set transaction result to notify to app
    public void setVoucher(VoucherInfo voucherInfo) {
        if (paymentInfo != null) {
            paymentInfo.putVoucher(voucherInfo);
        }
    }

    public String getUserId() {
        if (getUserInfo() != null) {
            return getUserInfo().zalopay_userid;
        }
        return null;
    }

    public String getAppTransId() {
        if (getOrder() != null) {
            return getOrder().apptransid;
        }
        return null;
    }

    public int getOrderSource() {
        if (getOrder() != null) {
            return getOrder().ordersource;
        }
        return -1;
    }

    public String getNumberPhone() {
        if (getUserInfo() != null) {
            return getUserInfo().phonenumber;
        }
        return null;
    }

    private int[] getForceChannels() {
        return paymentInfo != null ? paymentInfo.getForceChannels() : new int[0];
    }

    public long getBalance() {
        if (getUserInfo() != null) {
            return getUserInfo().balance;
        }
        return 0;
    }

    public int getLevel() {
        if (getUserInfo() != null) {
            return getUserInfo().level;
        }
        return 0;
    }

    public long getAmount() {
        if (getOrder() != null) {
            return getOrder().amount;
        }
        return 0;
    }

    public double getAmountTotal() {
        if (getOrder() != null) {
            return getOrder().amount_total;
        }
        return 0;
    }

    public void setAmountTotal(double amountTotal) {
        if (getOrder() != null) {
            getOrder().amount_total = amountTotal;
        }
    }

    public AbstractOrder getOrder() {
        return paymentInfo != null ? paymentInfo.getOrder() : null;
    }

    public void setOrder(AbstractOrder order) {
        if (paymentInfo != null && paymentInfo.getBuilder() != null) {
            paymentInfo.getBuilder().setOrder(order);
        }
    }

    public BaseMap getMapBank() {
        return paymentInfo != null ? paymentInfo.getMapBank() : null;
    }

    public void setMapBank(BaseMap mapBank) {
        if (paymentInfo != null && paymentInfo.getBuilder() != null) {
            paymentInfo.getBuilder().setMapBank(mapBank);
        }
    }

    @TransactionType
    public int getTranstype() {
        return paymentInfo != null ? paymentInfo.getTranstype() : TransactionType.PAY;
    }

    public void setTranstype(@TransactionType int transtype) {
        if (paymentInfo != null && paymentInfo.getBuilder() != null) {
            paymentInfo.getBuilder().setTransactionType(transtype);
        }
    }

    public PaymentLocation getLocation() {
        return paymentInfo != null ? paymentInfo.getLocation() : null;
    }

    public UserInfo getUserInfo() {
        return paymentInfo != null ? paymentInfo.getUser() : null;
    }

    public UserInfo getMoneyTransferReceiverInfo() {
        return paymentInfo != null ? paymentInfo.getMoneyTransferReceiverInfo() : null;
    }

    public String getLinkAccBankCode() {
        LinkAccInfo linkAccInfo = getLinkAccountInfo();
        if (linkAccInfo != null) {
            return linkAccInfo.getBankCode();
        }
        return null;
    }

    private LinkAccInfo getLinkAccountInfo() {
        return paymentInfo != null ? paymentInfo.getLinkAccountInfo() : null;
    }

    public void setLinkAccountInfo(LinkAccInfo linkAccountInfo) {
        if (paymentInfo != null && paymentInfo.getBuilder() != null) {
            paymentInfo.getBuilder().setLinkAccountInfo(linkAccountInfo);
        }
    }

    public AbstractOrder takeOrder() {
        AbstractOrder abstractOrder = null;
        if (paymentInfo != null && paymentInfo.getOrder() != null) {
            abstractOrder = paymentInfo.getOrder().clone();
            if (paymentInfo.getBuilder() != null) {
                paymentInfo.getBuilder().setOrder(null);
            }
        }
        return abstractOrder;
    }

    // set transaction result to notify to app
    public void setResult(@PaymentStatus int pStatus) {
        if (paymentInfo != null) {
            paymentInfo.putPaymentStatus(pStatus);
        }
    }

    public void setMapCardResult(DMapCardResult mapBankResult) {
        if (paymentInfo != null && paymentInfo.getBuilder() != null) {
            paymentInfo.getBuilder().setMapCard(mapBankResult);
        }
    }

    @PaymentStatus
    public int getStatus() {
        return paymentInfo != null ? paymentInfo.getPaymentStatus() : PaymentStatus.PROCESSING;
    }

    /***
     * for checking user selected a map card channel.
     */
    public boolean payByCardMap() {
        return paymentInfo != null && paymentInfo.getMapBank() instanceof MapCard && paymentInfo.getMapBank().isValid();
    }

    public boolean payByBankAccountMap() {
        return paymentInfo != null && paymentInfo.getMapBank() instanceof BankAccount && paymentInfo.getMapBank().isValid();
    }

    public boolean isBankAccountTrans() {
        return bankAccountLink() || bankAccountUnlink();
    }

    public boolean bankAccountLink() {
        return getLinkAccountInfo() != null && getLinkAccountInfo().isLinkAcc();
    }

    public boolean bankAccountUnlink() {
        return getLinkAccountInfo() != null && getLinkAccountInfo().isUnlinkAcc();
    }

    public boolean isLinkTrans() {
        return getTranstype() == TransactionType.LINK;
    }

    public boolean isMoneyTranferTrans() {
        return getTranstype() == TransactionType.MONEY_TRANSFER;
    }

    public boolean isTopupTrans() {
        return getTranstype() == TransactionType.TOPUP;
    }

    public boolean isWithDrawTrans() {
        return getTranstype() == TransactionType.WITHDRAW;
    }

    public boolean isPayTrans() {
        return getTranstype() == TransactionType.PAY;
    }

    public void updateTransactionResult(int pReturnCode) {
        try {
            if (!TextUtils.isEmpty(mErrorLoginArray.get(pReturnCode))) {
                setResult(PaymentStatus.TOKEN_EXPIRE);
            }
            if (!TextUtils.isEmpty(mErrorAccountArray.get(pReturnCode))) {
                setResult(PaymentStatus.USER_LOCK);
            }
            if (PaymentStatusHelper.isNeedToChargeMoreMoney(pReturnCode)) {
                setResult(PaymentStatus.ERROR_BALANCE);
            } else if (PaymentStatusHelper.isTransactionProcessing(pReturnCode)) {
                setResult(PaymentStatus.PROCESSING);
            } else if (PaymentStatusHelper.isNeedToUpgradeLevelUser(pReturnCode)) {
                setResult(PaymentStatus.LEVEL_UPGRADE_PASSWORD);
            } else if (PaymentStatusHelper.isServerInMaintenance(pReturnCode)) {
                setResult(PaymentStatus.SERVICE_MAINTENANCE);
            } else if (!TextUtils.isEmpty(mErrorArray.get(pReturnCode))) {
                setResult(PaymentStatus.INVALID_DATA);
            }
        } catch (Exception e) {
            Log.e("updateTransactionResult", e);
        }
    }

    public String getPaymentMethodTitleByTrans(Context context) {
        String title = context.getResources().getString(R.string.sdk_pay_method_title);
        if (isMoneyTranferTrans()) {
            title = context.getResources().getString(R.string.sdk_tranfer_method_title);
        }
        return title;
    }

    public String getTitleByTrans(Context context) {
        String title = context.getString(R.string.sdk_pay_title);
        if (isTopupTrans()) {
            title = context.getString(R.string.sdk_topup_title);
        } else if (isMoneyTranferTrans()) {
            title = context.getString(R.string.sdk_transfer_title);
        } else if (isWithDrawTrans()) {
            title = context.getString(R.string.sdk_withdraw_title);
        } else if (isLinkTrans()) {
            title = context.getString(R.string.sdk_link_title);
        }
        return title;
    }

    public String getFailTitleByTrans(Context context) {
        String title = context.getString(R.string.sdk_pay_fail_title);
        if (isTopupTrans()) {
            title = context.getString(R.string.sdk_topup_fail_title);
        } else if (isMoneyTranferTrans()) {
            title = context.getString(R.string.sdk_transfer_fail_title);
        } else if (isWithDrawTrans()) {
            title = context.getString(R.string.sdk_withdraw_fail_title);
        } else if (isLinkTrans()) {
            title = context.getString(R.string.sdk_link_acc_fail_title);
        }
        return title;
    }

    public String getSuccessTitleByTrans(Context context) {
        String title = context.getResources().getString(R.string.sdk_pay_success_title);
        if (isTopupTrans()) {
            title = context.getResources().getString(R.string.sdk_topup_success_title);
        } else if (isMoneyTranferTrans()) {
            title = context.getResources().getString(R.string.sdk_transfer_success_title);
        } else if (isWithDrawTrans()) {
            title = context.getResources().getString(R.string.sdk_withdraw_success_title);
        } else if (isLinkTrans()) {
            title = context.getResources().getString(R.string.sdk_link_acc_success_title);
        }
        return title;
    }

    public String getQuitMessByTrans(Context context) {
        String title = context.getResources().getString(R.string.sdk_trans_confirm_quit_pay_mess);
        if (isTopupTrans()) {
            title = context.getResources().getString(R.string.sdk_trans_confirm_quit_topup_mess);
        } else if (isMoneyTranferTrans()) {
            title = context.getResources().getString(R.string.sdk_trans_confirm_quit_transfer_mess);
        } else if (isWithDrawTrans()) {
            title = context.getResources().getString(R.string.sdk_trans_confirm_quit_withdraw_mess);
        }
        return title;
    }

    public String getTitlePassword(Context context) {
        String title = context.getResources().getString(R.string.sdk_pay_password_title_text);
        if (isTopupTrans()) {
            title = context.getResources().getString(R.string.sdk_topup_password_title_text);
        } else if (isMoneyTranferTrans()) {
            title = context.getResources().getString(R.string.sdk_transfer_password_title_text);
        } else if (isWithDrawTrans()) {
            title = context.getResources().getString(R.string.sdk_withdraw_password_title_text);
        }
        return title;
    }

    public boolean shouldIgnore(int pChannelId) {
        int[] channels = getForceChannels();
        if (channels == null || channels.length <= 0) {
            return false;
        }
        for (int channel : channels) {
            if (channel == pChannelId) {
                return false;
            }
        }
        return true;
    }

    public String getChargeInfo(DPaymentCard paymentCard) {
        BaseMap mapBank = getMapBank();
        if (mapBank != null && mapBank.isValid() && mapBank instanceof MapCard) {
            CardSubmit mapCard = new CardSubmit((MapCard) mapBank);
            return GsonUtils.toJsonString(mapCard);
        } else if (mapBank != null && mapBank.isValid() && mapBank instanceof BankAccount) {
            return GsonUtils.toJsonString(mapBank);
        } else if (paymentCard != null && paymentCard.isValid()) {
            return GsonUtils.toJsonString(paymentCard);
        } else {
            return null;
        }
    }

    public boolean updateResultNetworkingError(Context pContext, String pMessage) {
        boolean isOffNetworking;
        try {
            isOffNetworking = !ConnectionUtil.isOnline(BaseActivity.getCurrentActivity());
        } catch (Exception ex) {
            Log.e("updateResultNetworkingError", ex);
            isOffNetworking = false;
        }
        if (isOffNetworking &&
                (pMessage.equals(pContext.getResources().getString(R.string.sdk_payment_no_internet_mess)) ||
                        pMessage.equals(pContext.getResources().getString(R.string.sdk_trans_networking_offine_mess)) ||
                        pMessage.equals(pContext.getResources().getString(R.string.sdk_alert_networking_off_in_link_account)) ||
                        pMessage.equals(pContext.getResources().getString(R.string.sdk_alert_networking_off_in_unlink_account)))) {
            setResult(PaymentStatus.DISCONNECT);
        }
        return isOffNetworking;
    }

    public String getMessage(Context context, Throwable throwable) {
        String message = null;
        if (throwable instanceof RequestException) {
            RequestException requestException = (RequestException) throwable;
            message = requestException.getMessage();
            switch (requestException.code) {
                case RequestException.NULL:
                    message = context.getResources().getString(R.string.sdk_payment_generic_error_networking_mess);
                    break;
                default:
                    updateTransactionResult(requestException.code);
            }
        } else if (throwable instanceof NetworkConnectionException) {
            message = context.getResources().getString(R.string.sdk_payment_generic_error_networking_mess);
        }
        return message;
    }

    public String getOfflineMessage(Context context) {
        if (bankAccountLink()) {
            return context.getResources().getString(R.string.sdk_alert_networking_off_in_link_account);
        } else if (bankAccountUnlink()) {
            return context.getResources().getString(R.string.sdk_alert_networking_off_in_unlink_account);
        } else {
            return context.getResources().getString(R.string.sdk_trans_networking_offine_mess);
        }
    }

    public boolean isRedPacket() {
        return getAppId() == BuildConfig.REDPACKET_APPID;
    }

    public void updateOrderTime(long timestamp) {
        AbstractOrder order = getOrder();
        if (order == null) {
            return;
        }
        if (!isMoneyTranferTrans()) {
            return;
        }
        if (timestamp <= 0) {
            return;
        }
        order.apptime = timestamp;
        Timber.d("update transaction time to %s", timestamp);
    }

}
