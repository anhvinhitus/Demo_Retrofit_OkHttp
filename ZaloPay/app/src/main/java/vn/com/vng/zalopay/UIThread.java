package vn.com.vng.zalopay;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;

/**
 * Created by AnhHieu on 3/25/16.
 */
@Singleton
public class UIThread implements PostExecutionThread {

    @Inject
    public UIThread() {}

    

    @Override public Scheduler getScheduler() {
        return AndroidSchedulers.mainThread();
    }
}
