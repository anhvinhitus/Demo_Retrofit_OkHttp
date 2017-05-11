package vn.com.vng.zalopay.tracker;

import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPTracker;

/**
 * Created by khattn on 1/23/17.
 * ZPTracker apptransid
 */

public class ZPTrackerApptransid implements ZPTracker {

    private ApptransidLogStore.Repository mRepository;

    public ZPTrackerApptransid(ApptransidLogStore.Repository repository) {
        this.mRepository = repository;
    }

    @Override
    public void trackEvent(int eventId, Long eventValue) {

    }

    @Override
    public void trackScreen(String screenName) {

    }

    @Override
    public void trackTiming(int eventId, long value) {

    }

    @Override
    public void trackApptransidEvent(ZPApptransidLog log) {
        mRepository.put(log)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }

    @Override
    public void trackAPIError(String apiName, int httpCode, int serverCode, int networkCode) {

    }
}
