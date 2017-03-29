package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Response;
import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class LoadBankListImpl implements IRequest<BankConfigResponse> {
    @Override
    public int getRequestEventId() {
        return ZPEvents.CONNECTOR_V001_TPE_GETBANKLIST;
    }

    @Override
    public Observable<Response<BankConfigResponse>> getRequest(IData pIData, HashMap<String, String> pParams) throws Exception {
        return pIData.loadBankList(pParams);
    }
}