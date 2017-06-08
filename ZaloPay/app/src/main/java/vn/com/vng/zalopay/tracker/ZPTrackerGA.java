package vn.com.vng.zalopay.tracker;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.ga.AnalyticsStore;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;

/**
 * Created by huuhoa on 6/27/16.
 * Default implementation for translate ZPTracker to Google Analytics
 */
public class ZPTrackerGA extends DefaultTracker {

    private static final long INTERVAL_SEND_PAYLOAD = 20;
    private static final String FORMAT_GOOGLE_ANALYTICS = "[Android][%s]";

    private static boolean initialized = false;

    private final GoogleReporter mGoogleReporter;
    private final ThreadExecutor mThreadExecutor;
    private final AnalyticsStore.Repository mAnalyticsRepository;

    public ZPTrackerGA(GoogleReporter googleReporter) {
        
        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();

        mThreadExecutor = applicationComponent.threadExecutor();
        mAnalyticsRepository = applicationComponent.analyticsRepository();

        mGoogleReporter = googleReporter;

        startTimerSendPayloadData();
    }

    private void startTimerSendPayloadData() {

        if (initialized) {
            return;
        }

        initialized = true;

        Observable.interval(INTERVAL_SEND_PAYLOAD, TimeUnit.SECONDS)
                .flatMap(aLong -> mAnalyticsRepository.sendBatch())
                .subscribeOn(Schedulers.from(mThreadExecutor))
                .subscribe(new DefaultSubscriber<>());
    }

    @Override
    public void trackEvent(int eventId, Long eventValue) {
        mThreadExecutor.execute(() -> mGoogleReporter.trackEvent(eventId, eventValue));
    }

    @Override
    public void trackScreen(String screenName) {
        mThreadExecutor.execute(() -> {
            String screenWithFormat = String.format(FORMAT_GOOGLE_ANALYTICS, screenName);
            mGoogleReporter.trackScreen(screenWithFormat);
        });
    }

    /**
     * Log timing value for given event.
     * Events are defined in https://docs.google.com/spreadsheets/d/1kdqC78-qMsRGY_n4hMyzUsbHlS3Gl4yWwgr62qLO8Co/edit#gid=0
     *
     * @param eventId If of timing event
     * @param value   time recorded in milliseconds
     */
    @Override
    public void trackTiming(int eventId, long value) {
        mThreadExecutor.execute(() -> mGoogleReporter.trackTiming(eventId, value));
    }
}
