package vn.com.vng.zalopay.monitors;

/**
 * Created by huuhoa on 6/5/16.
 * Interface for timing an event
 */
public interface IMonitorTiming {
    void startEvent(int event);

    //! @return time elapsed from start of the event
    long finishEvent(int event);

    //! cancel the event, don't report the time
    void cancelEvent(int event);
}
