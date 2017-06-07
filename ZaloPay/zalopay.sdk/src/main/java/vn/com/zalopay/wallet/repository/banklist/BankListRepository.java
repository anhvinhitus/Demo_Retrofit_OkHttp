package vn.com.zalopay.wallet.repository.banklist;

import rx.Observable;
import rx.functions.Func1;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.datasource.RetryWithDelay;
import vn.com.zalopay.wallet.exception.RequestException;

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
    public Observable<BankConfigResponse> fetchBankListCloud(String platform, String checksum, String appversion) {
        return mBankListService.fetchBankList(platform, checksum, appversion)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .filter(bankConfigResponse -> bankConfigResponse != null)
                .doOnNext(bankConfigResponse -> mLocalStorage.putBankList(bankConfigResponse))
                .flatMap(new Func1<BankConfigResponse, Observable<BankConfigResponse>>() {
                    @Override
                    public Observable<BankConfigResponse> call(BankConfigResponse bankConfigResponse) {
                        if (bankConfigResponse.returncode != 1) {
                            return Observable.error(new RequestException(bankConfigResponse.returncode, bankConfigResponse.returnmessage));
                        } else if (bankConfigResponse.bankcardprefixmap != null) {
                            bankConfigResponse.expiredtime = bankConfigResponse.expiredtime + System.currentTimeMillis();
                            return Observable.just(bankConfigResponse);
                        } else {
                            return mLocalStorage.getBankList();
                        }
                    }
                });
    }

    @Override
    public BankListStore.LocalStorage getLocalStorage() {
        return mLocalStorage;
    }
}
