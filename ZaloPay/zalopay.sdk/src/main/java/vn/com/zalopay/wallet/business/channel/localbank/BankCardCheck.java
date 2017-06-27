package vn.com.zalopay.wallet.business.channel.localbank;

import android.text.TextUtils;

import java.util.Map;

import timber.log.Timber;
import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.controller.SDKApplication;

public class BankCardCheck extends CardCheck {
    private static BankCardCheck _object;
    private Map<String, String> bankPrefix;
    public BankCardCheck() {
        super();
        this.mSelectBank = null;
        this.mCardIdentifier = ResourceManager.getInstance(null).getBankCardIdentifier();
        this.bankPrefix = SDKApplication.getApplicationComponent().bankListInteractor().getBankPrefix();
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
    public String getBankName() {
        return mSelectBank != null ? mSelectBank.name : null;
    }

    @Override
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
        if (bankPrefix == null) {
            Timber.d("bank prefix is null");
            return false;
        }
        //get bank code in bank map
        String bankCode = null;
        for (int i = 3; i <= pCardNumber.length(); i++) {
            bankCode = bankPrefix.get(pCardNumber.substring(0, i));
            if (!TextUtils.isEmpty(bankCode)) {
                break;
            }
        }
        if (TextUtils.isEmpty(bankCode)) {
            mSelectBank = null;
            return false;
        }
        try {
            mSelectBank = mBankInteractor.getBankConfig(bankCode);
            mCardNumber = pCardNumber;
            if (mSelectBank != null) {
                if (!TextUtils.isEmpty(mSelectBank.code)) {
                    mOtpReceiverPatternList = ResourceManager.getInstance(null).getOtpReceiverPattern(bankCode);
                    mIdentifier = ResourceManager.getInstance(null).getBankIdentifier(mSelectBank.code);
                    Log.d(this, "rule to get otp", mOtpReceiverPatternList);
                    Log.d(this, "rule to read card number", mIdentifier);
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
