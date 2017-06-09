package vn.com.zalopay.wallet.repository.banklist;

import rx.Observable;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.datasource.RetryWithDelay;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankListRepository implements BankListStore.Repository {
    protected BankListStore.LocalStorage mLocalStorage;
    protected BankListStore.BankListService mBankListService;

    public BankListRepository(BankListStore.BankListService bankListService, BankListStore.LocalStorage localStorage) {
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
    public BankListStore.LocalStorage getLocalStorage() {
        return mLocalStorage;
    }
}
