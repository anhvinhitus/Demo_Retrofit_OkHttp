package vn.com.vng.zalopay.monitors;

/**
 * Created by huuhoa on 6/5/16.
 * Report for monitoring
 */
public interface IMonitorReport {
    void reportTiming(int event, long timeElapsed);
}
