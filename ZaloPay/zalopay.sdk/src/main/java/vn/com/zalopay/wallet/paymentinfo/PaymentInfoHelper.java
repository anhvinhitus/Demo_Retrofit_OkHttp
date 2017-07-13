package vn.com.zalopay.wallet.paymentinfo;

import android.content.Context;
import android.text.TextUtils;

import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.creditcard.CardSubmit;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;

import static vn.com.zalopay.wallet.business.error.ErrorManager.mErrorAccountArray;
import static vn.com.zalopay.wallet.business.error.ErrorManager.mErrorArray;
import static vn.com.zalopay.wallet.business.error.ErrorManager.mErrorLoginArray;

/**
 * Created by chucvv on 6/7/17.
 */

public class PaymentInfoHelper extends SingletonBase {
    public IPaymentInfo paymentInfo;

    public PaymentInfoHelper(IPaymentInfo paymentInfo) {
        super();
        this.paymentInfo = paymentInfo;
    }

    public long getAppId() {
        if (getOrder() != null) {
            return getOrder().appid;
        }
        return BuildConfig.ZALOAPP_ID;
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
        if (getLinkAccountInfo() != null) {
            return getLinkAccountInfo().getBankCode();
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

    public String getPaymentMethodTitleByTrans() {
        String title = GlobalData.getStringResource(RS.string.sdk_pay_method_title);
        if (isMoneyTranferTrans()) {
            title = GlobalData.getStringResource(RS.string.sdk_tranfer_method_title);
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
        String title = context.getString(R.string.sdk_pay_success_title);
        if (isTopupTrans()) {
            title = context.getString(R.string.sdk_topup_success_title);
        } else if (isMoneyTranferTrans()) {
            title = context.getString(R.string.sdk_transfer_success_title);
        } else if (isWithDrawTrans()) {
            title = context.getString(R.string.sdk_withdraw_success_title);
        } else if (isLinkTrans()) {
            title = context.getString(R.string.sdk_link_acc_success_title);
        }
        return title;
    }

    public String getQuitMessByTrans(Context context) {
        String title = context.getString(R.string.sdk_quit_pay_confirm_text);
        if (isTopupTrans()) {
            title = context.getString(R.string.sdk_quit_topup_confirm_text);
        } else if (isMoneyTranferTrans()) {
            title = context.getString(R.string.sdk_quit_tranfer_confirm_text);
        } else if (isWithDrawTrans()) {
            title = context.getString(R.string.sdk_quit_withdraw_confirm_text);
        }
        return title;
    }

    public String getTitlePassword(Context context) {
        String title = context.getString(R.string.sdk_pay_password_title_text);
        if (isTopupTrans()) {
            title = context.getString(R.string.sdk_topup_password_title_text);
        } else if (isMoneyTranferTrans()) {
            title = context.getString(R.string.sdk_transfer_password_title_text);
        } else if (isWithDrawTrans()) {
            title = context.getString(R.string.sdk_withdraw_password_title_text);
        }
        return title;
    }

    /***
     * user level 1 can not tranfer money.
     * user level 1 can not withdraw.
     */
    public boolean userLevelValid() {
        boolean userLevelValid = true;
        try {
            int user_level = getLevel();
            if (isMoneyTranferTrans() && user_level < BuildConfig.level_allow_use_zalopay) {
                userLevelValid = false;
            } else if (isWithDrawTrans() && user_level < BuildConfig.level_allow_withdraw) {
                userLevelValid = false;
            } else if ((payByCardMap() || payByBankAccountMap()) && user_level < BuildConfig.level_allow_cardmap) {
                userLevelValid = false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return userLevelValid;
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

    public boolean updateResultNetworkingError(String pMessage) {
        boolean isOffNetworking;
        try {
            isOffNetworking = !ConnectionUtil.isOnline(BaseActivity.getCurrentActivity());
        } catch (Exception ex) {
            Log.e("updateResultNetworkingError", ex);
            isOffNetworking = false;
        }
        if (isOffNetworking &&
                (pMessage.equals(GlobalData.getStringResource(RS.string.zingpaysdk_alert_no_connection)) ||
                        pMessage.equals(GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction)) ||
                        pMessage.equals(GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_link_account)) ||
                        pMessage.equals(GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_unlink_account)))) {
            setResult(PaymentStatus.DISCONNECT);
        }
        return isOffNetworking;
    }

    public String getMessage(Throwable throwable) {
        String message = null;
        if (throwable instanceof RequestException) {
            RequestException requestException = (RequestException) throwable;
            message = requestException.getMessage();
            switch (requestException.code) {
                case RequestException.NULL:
                    message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
                    break;
                default:
                    updateTransactionResult(requestException.code);
            }
        } else if (throwable instanceof NetworkConnectionException) {
            message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        }
        return message;
    }
}
