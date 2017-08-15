package vn.com.zalopay.wallet.card;

import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.entity.config.CardRule;

/**
 * class for detect working with
 * visa master jcb
 */
public class CreditCardDetector extends AbstractCardDetector {
    private static CreditCardDetector _object;
    private String mCardCode;
    private String mCardName;
    private boolean mValid;

    public CreditCardDetector() {
        super();
        this.mCardRules = mResourceManager.getCreditCardIdentifier();
        this.mCardCode = "";
        this.mCardName = "";
    }

    public CreditCardDetector(List<CardRule> pCardRules) {
        this.mCardRules = pCardRules;
        this.mCardCode = "";
        this.mCardName = "";
    }


    public static CreditCardDetector getInstance() {
        if (CreditCardDetector._object == null) {
            CreditCardDetector._object = new CreditCardDetector();
        }
        return CreditCardDetector._object;
    }

    @Override
    public void reset() {
        super.reset();
        this.mCardCode = "";
        this.mCardName = "";
        this.mValid = false;
    }

    public void dispose() {
        super.dispose();
        this.mCardCode = "";
        this.mCardName = "";
        this.mValid = false;
    }

    @Override
    public boolean isValidCardLength() {
        return mValid;
    }

    @Override
    public String getDetectBankCode() {
        return BuildConfig.CC_CODE;
    }

    @Override
    public String getCodeBankForVerifyCC() {
        return mCardCode;
    }

    @Override
    public String getBankName() {
        return mCardName;
    }

    @Override
    public String getShortBankName() {
        return getBankName();
    }

    private boolean isJCB(CardRule cardRule, String pCardNumber) throws Exception {
        if (TextUtils.isEmpty(pCardNumber)) {
            return false;
        }
        String prefixDetector = cardRule.startPin;
        if (TextUtils.isEmpty(prefixDetector)) {
            return false;
        }
        if (!prefixDetector.contains("-")) {
            return false;
        }
        String[] prefixNumbs = prefixDetector.split("-");
        long minStart = Integer.parseInt(prefixNumbs[0]);
        long maxStart = Integer.parseInt(prefixNumbs[1]);

        for (int i = 3; i < pCardNumber.length() + 1; i++) {
            String sub = pCardNumber.substring(0, i);
            Long value = Long.parseLong(sub);
            if (value <= maxStart && value >= minStart) {
                return true;
            }
        }
        return false;
    }

    private boolean isMasterCard(CardRule cardRule, String pCardNumber) throws Exception {
        if (TextUtils.isEmpty(pCardNumber)) {
            return false;
        }
        String prefixDetector = cardRule.startPin;
        if (TextUtils.isEmpty(prefixDetector)) {
            return false;
        }
        if (!prefixDetector.contains(",")) {
            return false;
        }
        String[] prefixNums = prefixDetector.split(",");
        if (prefixNums.length <= 0) {
            return false;
        }
        for (String preNum : prefixNums) {
            if (!TextUtils.isEmpty(preNum) && (pCardNumber.equals(preNum) || pCardNumber.startsWith(preNum))) {
                return true;
            }
        }
        return false;
    }

    private boolean isVisa(CardRule cardRule, String pCardNumber) throws Exception {
        if (TextUtils.isEmpty(pCardNumber)) {
            return false;
        }
        String prefixDetector = cardRule.startPin;
        if (TextUtils.isEmpty(prefixDetector)) {
            return false;
        }
        return pCardNumber.equals(prefixDetector) || pCardNumber.startsWith(prefixDetector);
    }

    @Override
    protected boolean detect(String pCardNumber) {
        mCardNumber = pCardNumber;
        mFoundCardRule = null;
        mCardCode = "";
        mCardName = "";
        mValid = false;
        mValidLuhnFormula = true;
        try {
            for (CardRule cardRule : mCardRules) {
                boolean isMasterCard = isMasterCard(cardRule, pCardNumber);
                if (isMasterCard) {
                    mFoundCardRule = cardRule;
                    break;
                }
                boolean isVisa = isVisa(cardRule, pCardNumber);
                if (isVisa) {
                    mFoundCardRule = cardRule;
                    break;
                }
            }
        } catch (Exception e) {
            Timber.w(e);
        }
        if (mFoundCardRule != null) {
            mCardCode = mFoundCardRule.code;
            mCardName = mFoundCardRule.name;
            mValid = matchCardLength(pCardNumber, mFoundCardRule);
            Timber.d("found card %s", GsonUtils.toJsonString(mFoundCardRule));
        }
        return mFoundCardRule != null;
    }

    private boolean matchCardLength(String pCardNumber, CardRule pIdentifier) {
        return ((pCardNumber.length() >= pIdentifier.min_length) && (pCardNumber.length() <= pIdentifier.max_length));
    }
}
