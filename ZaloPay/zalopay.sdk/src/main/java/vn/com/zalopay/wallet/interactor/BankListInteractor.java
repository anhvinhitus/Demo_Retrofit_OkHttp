package vn.com.zalopay.wallet.interactor;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
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
                HashMap<String, String> bankMap = GsonUtils.fromJsonString(mBankListRepository.getLocalStorage().getMap(), type);
                bankConfigResponse.bankcardprefixmap = bankMap;
            }
            return Observable.just(bankConfigResponse);
        } else {
            return Observable.error(new RequestException(bankConfigResponse.returncode, bankConfigResponse.returnmessage));
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

    @Override
    public void clearCheckSum() {
        this.mBankListRepository.getLocalStorage().clearCheckSum();
    }

    @Override
    public void clearConfig() {
        this.mBankListRepository.getLocalStorage().clearConfig();
    }
}

