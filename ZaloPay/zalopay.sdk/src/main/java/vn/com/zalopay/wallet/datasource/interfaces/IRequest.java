package vn.com.zalopay.wallet.datasource.interfaces;

import java.util.HashMap;

import retrofit2.Response;
import rx.Observable;
import rx.functions.Action1;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.datasource.IData;

public interface IRequest<T> {
    Observable<T> getRequest(IData pIData, HashMap<String, String> pParams);
}
