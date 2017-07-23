package vn.com.zalopay.wallet.card;

import android.text.TextUtils;

import java.util.List;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.staticconfig.DCardIdentifier;

/**
 * class for detect working with
 * visa master jcb
 */
public class CreditCardCheck extends CardCheck {
    private static final String TAG = CreditCardCheck.class.getCanonicalName();
    private static CreditCardCheck _object;
    private String mCardCode;
    private String mCardName;
    private boolean mIsValid;

    public CreditCardCheck() {
        super();
        this.mCardIdentifier = ResourceManager.getInstance(null).getCreditCardIdentifier();
        this.mCardCode = "";
        this.mCardName = "";
    }

    public CreditCardCheck(List<DCardIdentifier> pCreditCardIdentifier) {
        this.mCardIdentifier = pCreditCardIdentifier;
        this.mCardCode = "";
        this.mCardName = "";
    }

    public static CreditCardCheck getInstance() {
        if (CreditCardCheck._object == null) {
            CreditCardCheck._object = new CreditCardCheck();
        }
        return CreditCardCheck._object;
    }

    @Override
    public void reset() {
        super.reset();
        this.mCardCode = "";
        this.mCardName = "";
        this.mIsValid = false;
    }

    public void dispose() {
        super.dispose();
        this.mCardCode = "";
        this.mCardName = "";
        this.mIsValid = false;
    }

    @Override
    public boolean isValidCardLength() {
        return mIsValid;
    }

    @Override
    public String getDetectBankCode() {
        return BuildConfig.CC_CODE;
    }

    @Override
    public String getCodeBankForVerify() {
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
        mIdentifier = null;
        mCardCode = "";
        mCardName = "";
        mIsValid = false;
        this.mValidLuhn = true;
        if (mCardIdentifier == null) {
            this.mCardIdentifier = ResourceManager.getInstance(null).getCreditCardIdentifier();
        }
        try {
            for (DCardIdentifier identifier : mCardIdentifier) {
                String[] startPin;
                String strStartPin = identifier.startPin;
                //master card start with 51,52,53,54,55
                if (strStartPin.contains(",")) {
                    startPin = strStartPin.split(",");

                    for (String aStartPin : startPin) {
                        if (pCardNumber.equals(aStartPin) || pCardNumber.startsWith(aStartPin)) {
                            mIdentifier = identifier;
                            break;
                        }
                    }
                } else if (strStartPin.contains("-")) {  //JCB in range of 3528-3589
                    startPin = strStartPin.split("-");
                    try {
                        long minStart = Integer.parseInt(startPin[0]);
                        long maxStart = Integer.parseInt(startPin[1]);
                        for (int i = 3; i < pCardNumber.length() + 1; i++) {
                            String sub = pCardNumber.substring(0, i);
                            Long value = Long.parseLong(sub);
                            if (value <= maxStart && value >= minStart) {
                                mIdentifier = identifier;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                } else if (pCardNumber.equals(strStartPin) || pCardNumber.startsWith(strStartPin)) { // visa start with 4
                    mIdentifier = identifier;
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        if (mIdentifier != null) {
            mCardCode = mIdentifier.code;
            mCardName = mIdentifier.name;
            Log.d(this, "found card", mIdentifier);
            matchCardLength(pCardNumber, mIdentifier);
        }
        return !TextUtils.isEmpty(mCardName);
    }

    private void matchCardLength(String pCardNumber, DCardIdentifier pIdentifier) {
        this.mIsValid = ((pCardNumber.length() >= pIdentifier.min_length) && (pCardNumber.length() <= pIdentifier.max_length));
    }
}
