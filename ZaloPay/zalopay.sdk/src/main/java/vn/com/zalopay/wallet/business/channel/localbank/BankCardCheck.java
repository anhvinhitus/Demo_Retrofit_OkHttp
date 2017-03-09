package vn.com.zalopay.wallet.business.channel.localbank;

import android.app.Activity;
import android.text.TextUtils;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class BankCardCheck extends CardCheck {
    private static BankCardCheck _object;
    /***
     * detect card again after finishing loading banklist
     */
    private ILoadBankListListener mLoadBankListListener = new ILoadBankListListener() {
        @Override
        public void onProcessing() {
        }

        @Override
        public void onComplete() {
            detectCard(mTempCardNumber);
        }

        @Override
        public void onError(String pMessage) {
            Activity activity = BasePaymentActivity.getCurrentActivity();
            if (activity != null && activity instanceof PaymentChannelActivity && !activity.isFinishing())
            {
                ((PaymentChannelActivity) activity).onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error), true);
            }
        }
    };

    public BankCardCheck() {
        super();

        this.mSelectedBank = null;
        this.mCardIndentifier = ResourceManager.getInstance(null).getBankCardIdentifier();

        BankLoader.loadBankList(null);
    }

    public static BankCardCheck getInstance() {
        if (BankCardCheck._object == null)
            BankCardCheck._object = new BankCardCheck();
        return BankCardCheck._object;
    }

    @Override
    public void reset() {
        mSelectedBank = null;
        mFoundIdentifier = null;

    }

    public void dispose() {
        super.dispose();

        mSelectedBank = null;
        mFoundIdentifier = null;
    }

    @Override
    public String getDetectBankCode() {
        if (mSelectedBank != null)
        {
            return mSelectedBank.code;
        }

        return null;
    }

    @Override
    public String getDetectedBankName() {
        if (mSelectedBank != null)
        {
            return mSelectedBank.name;
        }
        return null;
    }

    /***
     * detect bank type
     *
     * @param pCardNumber
     * @return
     */
    @Override
    protected boolean detect(String pCardNumber) {
        //ALL BANK PREFIX ABOVE 4 LENGTH.
        if (TextUtils.isEmpty(pCardNumber) || pCardNumber.length() <= 3) {
            mSelectedBank = null;
            return false;
        }

        mTempCardNumber = pCardNumber;

        if (!BankLoader.existedBankListOnMemory()) {
            BankLoader.loadBankList(mLoadBankListListener);

            mSelectedBank = null;
            return false;
        }

        //TRY GET BANK CODE IN BANK MAP.
        String bankCode = null;
        for (int i = 3; i <= pCardNumber.length(); i++) {
            bankCode = mBankMap.get(pCardNumber.substring(0, i));
            if (!TextUtils.isEmpty(bankCode)) {
                break;
            }
        }

        if (TextUtils.isEmpty(bankCode)) {
            mSelectedBank = null;
            return false;
        }

        try {
            mSelectedBank = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(bankCode), BankConfig.class);
            mCardNumber = pCardNumber;
            if (mSelectedBank != null && mSelectedBank.isBankActive()) {
                //GET API PATTERN OF SELECTED BANK.
                try {
                    if (!TextUtils.isEmpty(mSelectedBank.code)) {
                        mOtpReceiverPatternList = ResourceManager.getInstance(null).getOtpReceiverPattern(bankCode);
                        mFoundIdentifier = ResourceManager.getInstance(null).getBankIdentifier(mSelectedBank.code);
                        Log.d(this, "===otp pattern===" + GsonUtils.toJsonString(mOtpReceiverPatternList));
                        Log.d(this, "===identifier===" + GsonUtils.toJsonString(mFoundIdentifier));
                    }
                } catch (Exception e) {
                    Log.e(this, e);
                }
                return true;
            }

        } catch (Exception e) {
            Log.e(this, e);
        }
        mSelectedBank = null;
        mFoundIdentifier = null;
        return false;
    }

}
