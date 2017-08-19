package vn.com.zalopay.wallet.card;

import android.text.TextUtils;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.entity.config.CardRule;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.repository.ResourceManager;

public abstract class AbstractCardDetector extends SingletonBase {
    String mCardNumber;
    String mTempCardNumber;

    List<CardRule> mCardRules;
    CardRule mFoundCardRule;

    boolean mValidLuhnFormula; //check card number by Luhn formula
    protected ResourceManager mResourceManager;

    public AbstractCardDetector() {
        super();
        mCardNumber = "";
        mFoundCardRule = null;
        mValidLuhnFormula = true;
        mResourceManager = ResourceManager.getInstance(null);
    }

    public String getCardNumber() {
        return mCardNumber;
    }

    public String getLast4CardNo() {
        if (TextUtils.isEmpty(mCardNumber) || mCardNumber.length() <= 4) {
            return null;
        }
        return mCardNumber.substring(mCardNumber.length() - 4);
    }

    protected void reset() {
        mValidLuhnFormula = true;
    }

    public void dispose() {
        if (this.mCardRules != null) {
            this.mCardRules.clear();
            this.mCardRules = null;
        }
        this.mCardNumber = "";
        this.mValidLuhnFormula = true;
    }

    public CardRule getFoundCardRule() {
        return mFoundCardRule;
    }

    public abstract String getBankName();

    public abstract String getShortBankName();

    public boolean isValidCardLength() {
        return true;
    }

    public boolean validCardNumberLuhnFormula(String pCardNumber) {
        return matchLuhnFormula(pCardNumber);
    }

    private boolean matchLuhnFormula(String pCardNumber) {
        if (TextUtils.isEmpty(pCardNumber)) {
            return mValidLuhnFormula;
        }
        mValidLuhnFormula = SdkUtils.validateCardNumberByLuhn(pCardNumber.trim());
        Timber.d("match Luhn Formula card number %s - ismatch %s", pCardNumber, mValidLuhnFormula);
        return mValidLuhnFormula;
    }

    /***
     * This is code to detect bank type
     * For example: 123PSCB 123PVTB 123PCC
     */
    public String getDetectBankCode() {
        return null;
    }

    /***
     * we must use this to detect which credit card type
     * because #getDetectBankCode always return 123PCC for credit card
     * check this in CreditCardDetector override
     */
    public String getCodeBankForVerifyCC() {
        return getDetectBankCode();
    }

    public boolean detected() {
        return !TextUtils.isEmpty(getBankName());
    }

    protected boolean detect(String pCardNumber) {
        return false;
    }

    public Observable<Boolean> detectOnAsync(String pCardNumber) {
        return detectObservable(pCardNumber);
    }

    /***
     * detect card type on main thread
     */
    public boolean detectOnSync(String pCardNumber) {
        return detect(pCardNumber);
    }

    private Observable<Boolean> detectObservable(String pCardNumber) {
        return Observable.defer(() -> {
            try {
                return Observable.just(detect(pCardNumber));
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

    public String warningCardExistMessage() {
        String bankName = getShortBankName();
        String last4Number = getLast4CardNo();
        String message = GlobalData.getAppContext().getResources().getString(R.string.sdk_link_card_exist);
        if (!TextUtils.isEmpty(bankName)) {
            message = String.format(GlobalData.getAppContext().getResources().getString(R.string.sdk_link_card_exist_detail), bankName, last4Number);
        }
        return message;
    }
}
