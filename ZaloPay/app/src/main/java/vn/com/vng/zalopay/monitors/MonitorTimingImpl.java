package vn.com.vng.zalopay.monitors;

import java.util.HashMap;

/**
 * Created by huuhoa on 6/5/16.
 * Implementation of timing monitor
 */
public class MonitorTimingImpl implements IMonitorTiming {
    private final IMonitorReport monitorReport;
    private final HashMap<Integer, Long> events = new HashMap<>();

    public MonitorTimingImpl(IMonitorReport monitorReport) {
        this.monitorReport = monitorReport;
    }

    @Override
    public void startEvent(int event) {
        events.put(event, System.currentTimeMillis());
    }

    @Override
    public long finishEvent(int event) {
        Long start = events.get(event);
        if (start == null) {
            return 0;
        }

        events.remove(event);
        long elapsed = System.currentTimeMillis() - start;
        monitorReport.reportTiming(event, elapsed);
        return elapsed;
    }

    @Override
    public void cancelEvent(int event) {
        events.remove(event);
    }
}
