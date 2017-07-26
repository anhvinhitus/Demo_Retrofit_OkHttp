package vn.com.zalopay.wallet.card;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.repository.bank.BankStore;

public class BankDetector extends AbstractCardDetector {
    private static BankDetector _object;
    private Map<String, String> nBankPrefix;
    private BankStore.Interactor mBankInteractor;
    BankConfig mFoundBank;
    List<DOtpReceiverPattern> mFoundOtpRules;

    public BankDetector() {
        super();
        mCardRules = mResourceManager.getBankCardIdentifier();
        mBankInteractor = SDKApplication.getApplicationComponent().bankListInteractor();
        nBankPrefix = mBankInteractor.getBankPrefix();
        mFoundOtpRules = new ArrayList<>();
        mFoundBank = null;
    }

    public static BankDetector getInstance() {
        if (BankDetector._object == null) {
            BankDetector._object = new BankDetector();
        }
        return BankDetector._object;
    }

    public BankConfig getFoundBankConfig() {
        return mFoundBank;
    }

    public boolean isBankAccount() {
        return mFoundBank != null && mFoundBank.isBankAccount();
    }

    public List<DOtpReceiverPattern> getFoundOtpRules() {
        return mFoundOtpRules;
    }

    @Override
    public void reset() {
        mFoundBank = null;
        mFoundCardRule = null;
    }

    public void dispose() {
        super.dispose();
        mFoundBank = null;
        mFoundCardRule = null;
    }

    @Override
    public String getDetectBankCode() {
        return mFoundBank != null ? mFoundBank.code : null;
    }

    @Override
    public String getBankName() {
        return mFoundBank != null ? mFoundBank.name : null;
    }

    @Override
    public String getShortBankName() {
        return mFoundBank != null ? mFoundBank.getShortBankName() : null;
    }

    private String getBankCode(String pCardNumber) {
        if (nBankPrefix == null || nBankPrefix.size() <= 0) {
            return null;
        }
        if (TextUtils.isEmpty(pCardNumber)) {
            return null;
        }
        //get bank code in bank map
        String bankCode = null;
        int length = pCardNumber.length();
        for (int i = 3; i <= length; i++) {
            bankCode = nBankPrefix.get(pCardNumber.substring(0, i));
            if (!TextUtils.isEmpty(bankCode)) {
                break;
            }
        }
        return bankCode;
    }

    /***
     * detect bank type
     */
    @Override
    protected boolean detect(String pCardNumber) {
        if (TextUtils.isEmpty(pCardNumber) || pCardNumber.length() <= 3) {
            mFoundBank = null;
            return false;
        }
        mTempCardNumber = pCardNumber;
        if (nBankPrefix == null) {
            Timber.d("bank prefix is null");
            return false;
        }
        //get bank code in bank map
        String bankCode = getBankCode(pCardNumber);
        if (TextUtils.isEmpty(bankCode)) {
            mFoundBank = null;
            return false;
        }
        mFoundBank = mBankInteractor.getBankConfig(bankCode);
        mCardNumber = pCardNumber;
        if (mFoundBank == null) {
            mFoundCardRule = null;
            return false;
        }
        if (!TextUtils.isEmpty(mFoundBank.code)) {
            mFoundOtpRules = mResourceManager.getOtpReceiverPattern(bankCode);
            mFoundCardRule = mResourceManager.getBankIdentifier(mFoundBank.code);
            Timber.d("rule to get otp %s", GsonUtils.toJsonString(mFoundOtpRules));
            Timber.d("rule to read card number %s", GsonUtils.toJsonString(mFoundCardRule));
        }
        return true;
    }
}
