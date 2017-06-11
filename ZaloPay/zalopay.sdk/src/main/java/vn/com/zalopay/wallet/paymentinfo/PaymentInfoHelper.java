package vn.com.zalopay.wallet.paymentinfo;

import android.text.TextUtils;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;

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

    public String getNumberPhone() {
        if (getUserInfo() != null) {
            return getUserInfo().phonenumber;
        }
        return null;
    }

    public int[] getForceChannels() {
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

    public BaseMap getMapBank() {
        return paymentInfo != null ? paymentInfo.getMapBank() : null;
    }

    public void setMapBank(BaseMap mapBank) {
        if (paymentInfo != null) {
            paymentInfo.setMapBank(mapBank);
        }
    }

    @TransactionType
    public int getTranstype() {
        return paymentInfo != null ? paymentInfo.getTranstype() : TransactionType.PAY;
    }

    public PaymentLocation getLocation() {
        return paymentInfo != null ? paymentInfo.getLocation() : null;
    }

    public UserInfo getUserInfo() {
        return paymentInfo != null ? paymentInfo.getUser() : null;
    }

    public UserInfo getDestinationUser() {
        return paymentInfo != null ? paymentInfo.getDestinationUser() : null;
    }

    public String getLinkAccBankCode() {
        if (getLinkAccountInfo() != null) {
            return getLinkAccountInfo().getBankCode();
        }
        return null;
    }

    public LinkAccInfo getLinkAccountInfo() {
        return paymentInfo != null ? paymentInfo.getLinkAccoutInfo() : null;
    }

    // set transaction result to notify to app
    public void setResult(@PaymentStatus int pStatus) {
        if (paymentInfo != null) {
            paymentInfo.putPaymentStatus(pStatus);
        }
    }

    public void setMapCardResult(DMapCardResult mapBankResult) {
        if (paymentInfo != null) {
            paymentInfo.setMapCard(mapBankResult);
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

    public boolean isCardLinkTrans() {
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
                setResult(PaymentStatus.MONEY_NOT_ENOUGH);
            } else if (PaymentStatusHelper.isTransactionProcessing(pReturnCode)) {
                setResult(PaymentStatus.PROCESSING);
            } else if (PaymentStatusHelper.isNeedToUpgradeLevelUser(pReturnCode)) {
                setResult(PaymentStatus.LEVEL_UPGRADE_PASSWORD);
            } else if (PaymentStatusHelper.isServerInMaintenance(pReturnCode)) {
                setResult(PaymentStatus.SERVICE_MAINTENANCE);
            } else {
                if (!TextUtils.isEmpty(mErrorArray.get(pReturnCode))) {
                    setResult(PaymentStatus.INVALID_DATA);
                }
            }
        } catch (Exception e) {
            Log.e("updateTransactionResult", e);
        }
    }
}
