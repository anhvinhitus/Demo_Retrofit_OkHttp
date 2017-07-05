package vn.com.zalopay.wallet.business.channel.base;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.staticconfig.DCardIdentifier;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.interactor.IBankInteractor;

public abstract class CardCheck extends SingletonBase {
    public String mCardNumber;
    public String mTempCardNumber;
    protected List<DCardIdentifier> mCardIdentifier;
    protected DCardIdentifier mIdentifier;
    protected BankConfig mSelectBank;
    protected List<DOtpReceiverPattern> mOtpReceiverPatternList;
    protected boolean mValidLuhn; //check card number by Luhn formula
    protected IBankInteractor mBankInteractor;

    public CardCheck() {
        super();
        mCardNumber = "";
        mIdentifier = null;
        mValidLuhn = true;
        mOtpReceiverPatternList = new ArrayList<>();
        mBankInteractor = SDKApplication.getApplicationComponent().bankListInteractor();
    }

    public String getCardNumber() {
        return mCardNumber;
    }

    public String getFirst6CardNo() {
        if (TextUtils.isEmpty(mCardNumber) || mCardNumber.length() <= 6) {
            return null;
        }
        return mCardNumber.substring(0, 6);
    }

    public String getLast4CardNo() {
        if (TextUtils.isEmpty(mCardNumber) || mCardNumber.length() <= 4) {
            return null;
        }
        return mCardNumber.substring(mCardNumber.length() - 4);
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

    public abstract String getBankName();

    public abstract String getShortBankName();

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
        Timber.d("card number " + pCardNumber + " match luhn " + mValidLuhn);
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
        return !TextUtils.isEmpty(getBankName());
    }

    protected boolean detect(String pCardNumber) {
        return false;
    }

    public Subscription detectOnAsync(String pCardNumber, Action1<Boolean> detectCardSubscriber) {
        return detectObservable(pCardNumber)
                .doOnNext(aBoolean -> Log.d(this, "start detect card number", pCardNumber))
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(detectCardSubscriber, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(this, "detect card number on thread error", throwable);
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
        return Observable.defer(() -> {
            try {
                return Observable.just(detect(pCardNumber));
            }catch (Exception e){
                return Observable.error(e);
            }
        });
    }

    public String warningCardExistMessage() {
        String bankName = getShortBankName();
        String last4Number = getLast4CardNo();
        String message = GlobalData.getStringResource(RS.string.sdk_link_card_exist);
        if (!TextUtils.isEmpty(bankName)) {
            message = String.format(GlobalData.getStringResource(RS.string.sdk_link_card_exist_detail), bankName, last4Number);
        }
        return message;
    }
}
