package vn.com.zalopay.wallet.card;

import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.staticconfig.CardRule;
import vn.com.zalopay.wallet.repository.ResourceManager;

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
                String[] startPin;
                String strStartPin = cardRule.startPin;
                //master card start with 51,52,53,54,55
                if (strStartPin.contains(",")) {
                    startPin = strStartPin.split(",");
                    for (String aStartPin : startPin) {
                        if (pCardNumber.equals(aStartPin) || pCardNumber.startsWith(aStartPin)) {
                            mFoundCardRule = cardRule;
                            break;
                        }
                    }
                } else if (strStartPin.contains("-")) {  //JCB in range of 3528-3589
                    startPin = strStartPin.split("-");
                    long minStart = Integer.parseInt(startPin[0]);
                    long maxStart = Integer.parseInt(startPin[1]);
                    for (int i = 3; i < pCardNumber.length() + 1; i++) {
                        String sub = pCardNumber.substring(0, i);
                        Long value = Long.parseLong(sub);
                        if (value <= maxStart && value >= minStart) {
                            mFoundCardRule = cardRule;
                            break;
                        }
                    }
                } else if (pCardNumber.equals(strStartPin) || pCardNumber.startsWith(strStartPin)) { // visa start with 4
                    mFoundCardRule = cardRule;
                    break;
                }
                if(mFoundCardRule != null){
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
        return !TextUtils.isEmpty(mCardName);
    }

    private boolean matchCardLength(String pCardNumber, CardRule pIdentifier) {
        return  ((pCardNumber.length() >= pIdentifier.min_length) && (pCardNumber.length() <= pIdentifier.max_length));
    }
}
