package vn.com.zalopay.wallet.business.channel.localbank;

import android.app.Activity;
import android.text.TextUtils;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class BankCardCheck extends CardCheck {
    private static BankCardCheck _object;
    /***
     * load bank list again
     */
    private ILoadBankListListener mLoadBankListListener = new ILoadBankListListener() {
        @Override
        public void onProcessing() {
        }

        @Override
        public void onComplete() {
            detectOnSync(mTempCardNumber);//detect again card number after loading bank list
        }

        @Override
        public void onError(String pMessage) {
            Activity activity = BasePaymentActivity.getCurrentActivity();
            if (activity != null && activity instanceof PaymentChannelActivity && !activity.isFinishing()) {
                ((PaymentChannelActivity) activity).onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error), true);
            }
        }
    };

    public BankCardCheck() {
        super();
        this.mSelectBank = null;
        this.mCardIdentifier = ResourceManager.getInstance(null).getBankCardIdentifier();
        BankLoader.loadBankList(null);
    }

    public static BankCardCheck getInstance() {
        if (BankCardCheck._object == null) {
            BankCardCheck._object = new BankCardCheck();
        }
        return BankCardCheck._object;
    }

    @Override
    public void reset() {
        mSelectBank = null;
        mIdentifier = null;
    }

    public void dispose() {
        super.dispose();
        mSelectBank = null;
        mIdentifier = null;
    }

    @Override
    public String getDetectBankCode() {
        return mSelectBank != null ? mSelectBank.code : null;
    }

    @Override
    public String getDetectedBankName() {
        return mSelectBank != null ? mSelectBank.name : null;
    }

    public String getShortBankName() {
        if (mSelectBank != null) {
            return mSelectBank.getShortBankName();
        }
        return null;
    }

    /***
     * detect bank type
     * @param pCardNumber
     * @return
     */
    @Override
    protected boolean detect(String pCardNumber) {
        if (TextUtils.isEmpty(pCardNumber) || pCardNumber.length() <= 3) {
            mSelectBank = null;
            return false;
        }
        mTempCardNumber = pCardNumber;

        if (!BankLoader.existedBankListOnMemory()) {
            BankLoader.loadBankList(mLoadBankListListener);
            mSelectBank = null;
            return false;
        }

        //get bank code in bank map
        String bankCode = null;
        for (int i = 3; i <= pCardNumber.length(); i++) {
            bankCode = BankLoader.mapBank.get(pCardNumber.substring(0, i));
            if (!TextUtils.isEmpty(bankCode)) {
                break;
            }
        }

        if (TextUtils.isEmpty(bankCode)) {
            mSelectBank = null;
            return false;
        }

        try {
            mSelectBank = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(bankCode), BankConfig.class);
            mCardNumber = pCardNumber;
            if (mSelectBank != null && mSelectBank.isBankActive()) {
                try {
                    if (!TextUtils.isEmpty(mSelectBank.code)) {
                        mOtpReceiverPatternList = ResourceManager.getInstance(null).getOtpReceiverPattern(bankCode);
                        mIdentifier = ResourceManager.getInstance(null).getBankIdentifier(mSelectBank.code);
                        Log.d(this, "rule to get otp", mOtpReceiverPatternList);
                        Log.d(this, "rule to read card number", mIdentifier);
                    }
                } catch (Exception e) {
                    Log.e(this, e);
                }
                return true;
            }

        } catch (Exception e) {
            Log.e(this, e);
        }
        mSelectBank = null;
        mIdentifier = null;
        return false;
    }

}
