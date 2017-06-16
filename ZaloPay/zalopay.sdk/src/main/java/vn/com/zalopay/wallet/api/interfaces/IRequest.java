package vn.com.zalopay.wallet.api.interfaces;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.api.ITransService;

public interface IRequest<T> {
    Observable<T> getRequest(ITransService pIData, Map<String, String> pParams);
}
