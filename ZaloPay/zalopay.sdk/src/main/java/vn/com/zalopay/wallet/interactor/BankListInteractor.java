package vn.com.zalopay.wallet.interactor;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.repository.banklist.BankListStore;

/**
 * Interactor decide which get data from
 * do some bussiness logic on return result and delegate the result to caller
 * Created by chucvv on 6/8/17.
 */

public class BankListInteractor implements IBankList {
    public BankListStore.Repository mBankListRepository;
    protected Func1<BankConfigResponse, Observable<BankConfigResponse>> mapResult = bankConfigResponse -> {
        if (bankConfigResponse == null) {
            return Observable.error(new RequestException(RequestException.NULL, null));
        } else if (bankConfigResponse.returncode == 1) {
            bankConfigResponse.expiredtime = mBankListRepository.getLocalStorage().getExpireTime();
            if (bankConfigResponse.bankcardprefixmap == null) {
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
                HashMap<String, String> bankMap = GsonUtils.fromJsonString(mBankListRepository.getLocalStorage().getBankPrefix(), type);
                bankConfigResponse.bankcardprefixmap = bankMap;
            }
            return Observable.just(bankConfigResponse);
        } else {
            return Observable.error(new RequestException(bankConfigResponse.returncode, bankConfigResponse.returnmessage));
        }
    };
    private Func1<BankConfigResponse, Observable<List<ZPCard>>> supportCards = new Func1<BankConfigResponse, Observable<List<ZPCard>>>() {
        @Override
        public Observable<List<ZPCard>> call(BankConfigResponse bankConfigResponse) {
            Log.d(this, "start load support card");
            try {
                List<ZPCard> supportCards = new ArrayList<>();
                //cc must be hardcode
                String bankCodeVisa = CardType.VISA;
                String bankCodeMaster = CardType.MASTER;
                String bankNameVisa = GlobalData.getStringResource(RS.string.zpw_string_bankname_visa);
                String bankNameMaster = GlobalData.getStringResource(RS.string.zpw_string_bankname_master);
                ZPCard visaCard = new ZPCard(bankCodeVisa, getCardBitmapName(bankCodeVisa), bankNameVisa);
                ZPCard masterCard = new ZPCard(bankCodeMaster, getCardBitmapName(bankCodeMaster), bankNameMaster);
                //build support cards
                String bankCodes = mBankListRepository.getLocalStorage().getBankCodeList();
                if (!TextUtils.isEmpty(bankCodes)) {
                    String[] arrayBankCode = bankCodes.split(Constants.COMMA);
                    for (int i = 0; i < arrayBankCode.length; i++) {
                        String bankCode = arrayBankCode[i];
                        if (!TextUtils.isEmpty(bankCode) && !BuildConfig.CC_CODE.equals(bankCode)) {
                            boolean isBankAccount = BankAccountHelper.isBankAccount(bankCode);
                            ZPCard zpCard = new ZPCard(bankCode, getCardBitmapName(bankCode), isBankAccount, getBankName(bankCode));
                            if (!supportCards.contains(zpCard)) {
                                supportCards.add(zpCard);
                            }
                        }else if(!TextUtils.isEmpty(bankCode) && BuildConfig.CC_CODE.equals(bankCode)){
                            supportCards.add(visaCard);
                            supportCards.add(masterCard);
                        }
                    }
                }
                return Observable.just(supportCards);
            } catch (Exception e) {
                Log.e(this, e);
                return Observable.error(e);
            }
        }
    };

    @Inject
    public BankListInteractor(BankListStore.Repository bankListRepository) {
        this.mBankListRepository = bankListRepository;
        Log.d(this, "call constructor BankListInteractor");
    }

    @Override
    public Observable<BankConfigResponse> getBankList(String appVersion, long currentTime) {
        String checksum = mBankListRepository.getLocalStorage().getCheckSum();
        String platform = BuildConfig.PAYMENT_PLATFORM;
        Observable<BankConfigResponse> bankListCache = mBankListRepository
                .getLocalStorage()
                .get()
                .onErrorReturn(null);
        Observable<BankConfigResponse> bankListCloud = mBankListRepository
                .fetchCloud(platform, checksum, appVersion)
                .flatMap(mapResult);
        return Observable.concat(bankListCache, bankListCloud)
                .first(bankConfigResponse -> bankConfigResponse != null && (bankConfigResponse.expiredtime > currentTime))
                .compose(SchedulerHelper.applySchedulers());
    }

    /***
     * reload bank list
     * and return support card list
     * @param appVersion
     * @param currentTime
     * @return
     */
    @Override
    public Observable<List<ZPCard>> getSupportCards(String appVersion, long currentTime) {
        return getBankList(appVersion, currentTime)
                .subscribeOn(Schedulers.io())
                .flatMap(supportCards)
                .observeOn(AndroidSchedulers.mainThread());
    }

    protected String getCardBitmapName(String pBankCode) {
        return String.format("bank_%s%s", pBankCode, Constants.BITMAP_EXTENSION);
    }

    private String getBankName(String bankCode) {
        if (TextUtils.isEmpty(bankCode)) {
            return null;
        }
        BankConfig bankConfig = this.mBankListRepository.getLocalStorage().getBankConfig(bankCode);
        return bankConfig != null ? bankConfig.getDisplayName() : null;
    }

    @Override
    public String getBankPrefix() {
        return this.mBankListRepository.getLocalStorage().getBankPrefix();
    }

    @Override
    public void clearCheckSum() {
        this.mBankListRepository.getLocalStorage().clearCheckSum();
    }

    @Override
    public void clearConfig() {
        this.mBankListRepository.getLocalStorage().clearConfig();
    }
}

