package vn.com.zalopay.wallet.business.channel.base;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.staticconfig.DCardIdentifier;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.listener.ZPWOnDetectCardListener;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;

public class CardCheck extends SingletonBase {
    public static Map<String, String> mBankMap;
    public String mCardNumber;
    public String mTempCardNumber;
    protected ZPWOnDetectCardListener mDetectCardListener;
    protected List<DCardIdentifier> mCardIndentifier;
    protected DCardIdentifier mFoundIdentifier;
    protected BankConfig mSelectedBank;
    protected ArrayList<DOtpReceiverPattern> mOtpReceiverPatternList;
    //check cardnumber by Luhn formula
    protected boolean mValidLuhn;

    public CardCheck() {
        super();

        mCardNumber = "";
        mFoundIdentifier = null;
        mValidLuhn = true;

        mOtpReceiverPatternList = new ArrayList<>();
    }

    protected void reset() {
        mValidLuhn = true;
    }

    public boolean isBankAccount() {
        if (mSelectedBank != null)
        {
            return mSelectedBank.isBankAccount();
        }
        return false;
    }

    public void dispose() {
        if (this.mCardIndentifier != null) {
            this.mCardIndentifier.clear();
            this.mCardIndentifier = null;
        }
        this.mCardNumber = "";
        this.mValidLuhn = true;

    }

    public BankConfig getDetectBankConfig() {
        return mSelectedBank;
    }

    public ArrayList<DOtpReceiverPattern> getOtpReceiverPatternList() {
        return mOtpReceiverPatternList;
    }

    public DCardIdentifier getCardIdentifier() {
        return mFoundIdentifier;
    }

    public String getDetectedBankName() {
        return null;
    }

    public boolean isValidCardLength() {
        return true;
    }

    public boolean isValidCardLuhn(String pCardNumber) {
        return matchCardLuhn(pCardNumber);
    }

    protected boolean matchCardLuhn(String pCardNumber) {
        if (!TextUtils.isEmpty(pCardNumber)) {
            this.mValidLuhn = ZPWUtils.validateCardNumberByLuhn(pCardNumber.trim());
        }

        Log.d(this, "===matchCardLuhn===" + pCardNumber + "===mValidLuhn=" + mValidLuhn);

        return this.mValidLuhn;
    }

    /***
     * This is code to detect bank type
     * For example: 123PSCB 123PVTB 123PCC
     *
     * @return
     */
    public String getDetectBankCode() {
        return null;
    }

    /***
     * we must use this to detect which credit card type
     * because #getDetectBankCode always return 123PCC for credit card
     * check this in CreditCardCheck override
     *
     * @return
     */
    public String getCodeBankForVerify() {
        return getDetectBankCode();
    }

    public String getTinyCardNumber() {
        if (TextUtils.isEmpty(mCardNumber))
            return "";

        String hidden_character = GlobalData.getStringResource(RS.string.zpw_string_hidden_character);

        return hidden_character + mCardNumber.substring(mCardNumber.length() - 4);
    }

    public boolean isDetected() {
        return !TextUtils.isEmpty(getDetectedBankName()) ? true : false;
    }

    protected boolean detect(String pCardNumber) {
        return false;
    }

    /***
     * detect card type on other thread
     *
     * @param pCardNumber
     * @param pListener
     */
    public void detectOnOtherThread(String pCardNumber, ZPWOnDetectCardListener pListener) {
        this.mDetectCardListener = pListener;

        DetectCardThread detectCardThread = new DetectCardThread();
        detectCardThread.execute(pCardNumber);
    }

    /***
     * detect card type on main thread
     *
     * @param pCardNumber
     * @return
     */
    public boolean detectCard(String pCardNumber) {
        return detect(pCardNumber);
    }

    public class DetectCardThread extends AsyncTask<String, Object, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            if (params == null && TextUtils.isEmpty(params[0])) {
                Log.d(this, "===params = NULL or empty");
                return false;
            }

            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            detect(params[0]);

            return isDetected();
        }

        @Override
        protected void onPostExecute(Boolean detected) {
            if (mDetectCardListener != null)
                mDetectCardListener.onDetectCardComplete(detected);
        }
    }
    //endregion
}
