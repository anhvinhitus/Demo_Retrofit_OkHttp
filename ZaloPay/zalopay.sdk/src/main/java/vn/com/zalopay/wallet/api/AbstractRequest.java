package vn.com.zalopay.wallet.api;

import android.os.Build;
import android.support.annotation.CallSuper;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

/**
 * Created by chucvv on 6/17/17.
 */

public abstract class AbstractRequest<T extends BaseResponse> implements IRequest<T> {
    protected ITransService mTransService;
    protected boolean running = false;
    protected long startTime = 0;
    protected Observable.Transformer<T, T> applyState = observable -> observable
            .doOnSubscribe(this::doOnSubscribe)
            .doOnError(this::doOnError)
            .doOnNext(this::doOnNext);

    public AbstractRequest(ITransService transService) {
        mTransService = transService;
    }

    @CallSuper
    protected void doOnError(Throwable throwable) {
    }

    @CallSuper
    protected void doOnSubscribe() {
        running = true;
        startTime = System.currentTimeMillis();
    }

    @CallSuper
    protected void doOnNext(T t) {
        running = false;
    }

    @Override
    public Map<String, String> buildParams() {
        return null;
    }

    protected Map<String, String> getMapTable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new ArrayMap<>();
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
