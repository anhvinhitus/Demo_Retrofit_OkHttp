package vn.com.zalopay.wallet.repository.bankaccount;

import rx.Observable;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.api.RetryWithDelay;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankAccountRepository implements BankAccountStore.Repository {
    protected BankAccountStore.LocalStorage mLocalStorage;
    protected BankAccountStore.BankAccountService bankAccountService;

    public BankAccountRepository(BankAccountStore.BankAccountService bankAccountService, BankAccountStore.LocalStorage localStorage) {
        this.bankAccountService = bankAccountService;
        this.mLocalStorage = localStorage;
    }

    @Override
    public Observable<BankAccountListResponse> fetchCloud(String userid, String accesstoken, String checksum, String appversion) {
        return bankAccountService.fetch(userid, accesstoken, checksum, appversion)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(bankAccountListResponse -> mLocalStorage.saveResponse(userid, bankAccountListResponse));
    }

    @Override
    public BankAccountStore.LocalStorage getLocalStorage() {
        return mLocalStorage;
    }
}
