package vn.com.zalopay.wallet.datasource.interfaces;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.datasource.IData;

public interface IRequest<T> {
    Observable<T> getRequest(IData pIData, Map<String, String> pParams);
}
