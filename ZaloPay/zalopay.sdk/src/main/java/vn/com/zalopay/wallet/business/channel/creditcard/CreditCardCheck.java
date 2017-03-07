package vn.com.zalopay.wallet.business.channel.creditcard;

import android.text.TextUtils;

import java.util.List;

import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.staticconfig.DCardIdentifier;
import vn.com.zalopay.wallet.utils.Log;

/**
 * CLASS DETECT TYPE OF CREDIT CARD(VISA-MASTER-JCB)
 */
public class CreditCardCheck extends CardCheck {
    private static final String TAG = CreditCardCheck.class.getName();

    private static CreditCardCheck _object;

    private String mSelectedCCCode;
    private String mSelectedCCName;
    private boolean mIsValid;


    public CreditCardCheck() {
        super();

        this.mCardIndentifier = ResourceManager.getInstance(null).getCreditCardIdentifier();


        this.mSelectedCCCode = "";
        this.mSelectedCCName = "";
    }

    public CreditCardCheck(List<DCardIdentifier> pCreditCardIndentifier) {
        this.mCardIndentifier = pCreditCardIndentifier;
        this.mSelectedCCCode = "";
        this.mSelectedCCName = "";
    }

    public static CreditCardCheck getInstance() {
        if (CreditCardCheck._object == null)
            CreditCardCheck._object = new CreditCardCheck();
        return CreditCardCheck._object;
    }

    @Override
    public void reset() {
        super.reset();
        this.mSelectedCCCode = "";
        this.mSelectedCCName = "";
        this.mIsValid = false;
    }

    public void dispose() {
        super.dispose();

        this.mSelectedCCCode = "";
        this.mSelectedCCName = "";
        this.mIsValid = false;
    }

    @Override
    public boolean isValidCardLength() {
        return mIsValid;
    }

    @Override
    public String getDetectBankCode() {
        return Constants.CCCode;
    }

    @Override
    public String getCodeBankForVerify() {
        return mSelectedCCCode;
    }

    @Override
    public String getDetectedBankName() {
        return mSelectedCCName;
    }

    @Override
    protected boolean detect(String pCardNumber) {
        mCardNumber = pCardNumber;
        mFoundIdentifier = null;
        mSelectedCCCode = "";
        mSelectedCCName = "";
        mIsValid = false;
        this.mValidLuhn = true;

        if (mCardIndentifier == null) {
            this.mCardIndentifier = ResourceManager.getInstance(null).getCreditCardIdentifier();
        }

        try {
            for (DCardIdentifier identifier : mCardIndentifier) {
                String[] startPin;
                String strStartPin = identifier.startPin;
                if (strStartPin.contains(",")) {
                    //MASTER CARD START WITH 51,52,53,54,55
                    startPin = strStartPin.split(",");

                    for (int i = 0; i < startPin.length; i++) {
                        if (pCardNumber.equals(startPin[i]) || pCardNumber.startsWith(startPin[i])) {
                            mFoundIdentifier = identifier;
                            break;
                        }
                    }
                } else if (strStartPin.contains("-")) {
                    //JCB START IN RANGE FROM 3528-3589
                    startPin = strStartPin.split("-");

                    try {
                        long minStart = Integer.parseInt(startPin[0]);
                        long maxStart = Integer.parseInt(startPin[1]);

                        for (int i = 3; i < pCardNumber.length() + 1; i++) {
                            String sub = pCardNumber.substring(0, i);
                            Long value = Long.parseLong(sub);

                            if (value <= maxStart && value >= minStart) {
                                mFoundIdentifier = identifier;
                                break;
                            }
                        }

                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                } else if (pCardNumber.equals(strStartPin) || pCardNumber.startsWith(strStartPin)) {
                    mFoundIdentifier = identifier;
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
        }


        if (mFoundIdentifier != null) {
            mSelectedCCCode = mFoundIdentifier.code;
            mSelectedCCName = mFoundIdentifier.name;

            matchCardLength(pCardNumber, mFoundIdentifier);
        }

        if (!TextUtils.isEmpty(mSelectedCCName))
            return true;
        else
            return false;
    }

    private void matchCardLength(String pCardNumber, DCardIdentifier pIdentifier) {
        this.mIsValid = ((pCardNumber.length() >= pIdentifier.min_length) && (pCardNumber.length() <= pIdentifier.max_length));
    }
}
