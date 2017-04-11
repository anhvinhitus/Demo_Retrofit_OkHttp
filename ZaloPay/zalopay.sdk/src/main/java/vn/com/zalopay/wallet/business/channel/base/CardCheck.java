package vn.com.zalopay.wallet.business.channel.base;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.staticconfig.DCardIdentifier;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.listener.OnDetectCardListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.SdkUtils;

public class CardCheck extends SingletonBase {
    public String mCardNumber;
    public String mTempCardNumber;
    protected OnDetectCardListener mDetectCardListener;
    protected List<DCardIdentifier> mCardIdentifier;
    protected DCardIdentifier mIdentifier;
    protected BankConfig mSelectBank;
    protected List<DOtpReceiverPattern> mOtpReceiverPatternList;
    protected boolean mValidLuhn; //check card number by Luhn formula
    protected Subscription mSubscription;

    public CardCheck() {
        super();
        mCardNumber = "";
        mIdentifier = null;
        mValidLuhn = true;
        mOtpReceiverPatternList = new ArrayList<>();
    }

    protected void reset() {
        mValidLuhn = true;
    }

    public boolean isBankAccount() {
        if (mSelectBank != null) {
            return mSelectBank.isBankAccount();
        }
        return false;
    }

    public void dispose() {
        if (this.mCardIdentifier != null) {
            this.mCardIdentifier.clear();
            this.mCardIdentifier = null;
        }
        this.mCardNumber = "";
        this.mValidLuhn = true;
    }

    public BankConfig getDetectBankConfig() {
        return mSelectBank;
    }

    public List<DOtpReceiverPattern> getOtpReceiverPatternList() {
        return mOtpReceiverPatternList;
    }

    public DCardIdentifier getCardIdentifier() {
        return mIdentifier;
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
            this.mValidLuhn = SdkUtils.validateCardNumberByLuhn(pCardNumber.trim());
        }
        Log.d(this, "card number " + pCardNumber + " match luhn " + mValidLuhn);
        return this.mValidLuhn;
    }

    /***
     * This is code to detect bank type
     * For example: 123PSCB 123PVTB 123PCC
     * @return
     */
    public String getDetectBankCode() {
        return null;
    }

    /***
     * we must use this to detect which credit card type
     * because #getDetectBankCode always return 123PCC for credit card
     * check this in CreditCardCheck override
     * @return
     */
    public String getCodeBankForVerify() {
        return getDetectBankCode();
    }

    public boolean isDetected() {
        return !TextUtils.isEmpty(getDetectedBankName()) ? true : false;
    }

    protected boolean detect(String pCardNumber) {
        return false;
    }

    /***
     * detect card type on other thread
     * @param pCardNumber
     * @param pListener
     */
    public void detectOnAsync(String pCardNumber, OnDetectCardListener pListener) {
        this.mDetectCardListener = pListener;
        Log.d(this, "detect card " + pCardNumber + " should run on new thread");
        mSubscription = detectObservable(pCardNumber)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Log.d(this, "detect card number on complete");
                        mSubscription.unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(this, "detect card number on thread error " + GsonUtils.toJsonString(e));
                    }

                    @Override
                    public void onNext(Boolean detected) {
                        if (mDetectCardListener != null) {
                            mDetectCardListener.onDetectCardComplete(detected);
                        }
                    }
                });
    }

    /***
     * detect card type on main thread
     * @param pCardNumber
     * @return
     */
    public boolean detectOnSync(String pCardNumber) {
        return detect(pCardNumber);
    }

    protected Observable<Boolean> detectObservable(String pCardNumber) {
        return Observable.create(subscriber -> {
            subscriber.onNext(detect(pCardNumber));
            subscriber.onCompleted();
        });
    }
    //endregion
}
