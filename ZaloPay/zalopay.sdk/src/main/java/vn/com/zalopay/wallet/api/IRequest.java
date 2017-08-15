package vn.com.zalopay.wallet.api;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.entity.response.BaseResponse;

/**
 * Created by chucvv on 6/17/17.
 */

public interface IRequest<T extends BaseResponse> {
    Map<String, String> buildParams();
    Observable<T> getObserver();
    boolean isRunning();
}
