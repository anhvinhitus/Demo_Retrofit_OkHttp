package vn.com.vng.zalopay.tracker;

import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPApptransidLogApiCall;

/**
 * Created by khattn on 1/23/17.
 * ZPTracker apptransid
 */

public class ZPTrackerApptransid extends DefaultTracker {

    private ApptransidLogStore.Repository mRepository;

    public ZPTrackerApptransid(ApptransidLogStore.Repository repository) {
        this.mRepository = repository;
    }

    @Override
    public void trackApptransidEvent(ZPApptransidLog log) {
        mRepository.put(log)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }

    @Override
    public void trackApptransidApiCall(ZPApptransidLogApiCall log) {
        mRepository.putApiCall(log)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }

}
