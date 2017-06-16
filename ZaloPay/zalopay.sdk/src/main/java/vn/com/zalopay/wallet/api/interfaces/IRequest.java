package vn.com.zalopay.wallet.api.interfaces;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.api.IData;

public interface IRequest<T> {
    Observable<T> getRequest(IData pIData, Map<String, String> pParams);
}
