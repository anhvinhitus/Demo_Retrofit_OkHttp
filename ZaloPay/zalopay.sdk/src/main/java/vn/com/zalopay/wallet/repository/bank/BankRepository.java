package vn.com.zalopay.wallet.repository.bank;

import rx.Observable;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.api.RetryWithDelay;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankRepository implements BankStore.Repository {
    protected BankStore.LocalStorage mLocalStorage;
    protected BankStore.BankListService mBankListService;

    public BankRepository(BankStore.BankListService bankListService, BankStore.LocalStorage localStorage) {
        this.mBankListService = bankListService;
        this.mLocalStorage = localStorage;
    }

    @Override
    public Observable<BankConfigResponse> fetchCloud(String platform, String checksum, String appversion) {
        return mBankListService.fetch(platform, checksum, appversion)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(bankConfigResponse -> mLocalStorage.put(bankConfigResponse));
    }

    @Override
    public BankStore.LocalStorage getLocalStorage() {
        return mLocalStorage;
    }
}
