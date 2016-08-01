package vn.com.vng.zalopay.monitors;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import timber.log.Timber;

/**
 * Created by huuhoa on 6/5/16.
 * Report for monitors
 */
public class MonitorReportImpl implements IMonitorReport {
    @Override
    public void reportTiming(int event, long timeElapsed) {
        Timber.i("Event: %d, duration: %d", event, timeElapsed);
        Crashlytics.log(String.format("Event: %d, duration: %d", event, timeElapsed));
        Answers.getInstance().logCustom(new CustomEvent("Timing").putCustomAttribute("event", event).putCustomAttribute("elapsed", timeElapsed));
    }
}
