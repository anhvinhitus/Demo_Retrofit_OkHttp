package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import rx.Observable;
import rx.schedulers.Schedulers;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.RetryWithDelay;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;
import vn.com.zalopay.wallet.datasource.request.BaseTask;

public class LoadBankListImpl implements IRequest<BankConfigResponse> {

    protected BaseTask<BankConfigResponse> mTask;

    public LoadBankListImpl(BaseTask<BankConfigResponse> pTask) {
        this.mTask = pTask;
    }

    @Override
    public int getRequestEventId() {
        return ZPEvents.CONNECTOR_V001_TPE_GETBANKLIST;
    }
    @Override
    public Observable<BankConfigResponse> getRequest(IData pIData, HashMap<String, String> pParams) throws Exception {
        return pIData.loadBankList(pParams).subscribeOn(Schedulers.newThread())
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, 500))
                .flatMap(bankConfigResponse -> saveRequestToCache(bankConfigResponse));
    }

    @Override
    public Observable<BankConfigResponse> saveRequestToCache(BankConfigResponse pResponse) {
        return Observable.just(mTask.onSaveResponseToDisk(pResponse));
    }
}
