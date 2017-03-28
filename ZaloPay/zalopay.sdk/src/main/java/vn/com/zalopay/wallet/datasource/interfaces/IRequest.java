package vn.com.zalopay.wallet.datasource.interfaces;

import java.util.HashMap;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.datasource.IData;

public interface IRequest<T extends BaseResponse> {
    Observable<T> getRequest(IData pIData, HashMap<String, String> pParams) throws Exception;

    Observable<T> saveRequestToCache(T pResponse);

    int getRequestEventId();
}
