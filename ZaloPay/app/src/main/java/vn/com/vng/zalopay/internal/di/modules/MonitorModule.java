package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Singleton;

import dagger.Provides;
import vn.com.vng.zalopay.monitors.IMonitorReport;
import vn.com.vng.zalopay.monitors.IMonitorTiming;
import vn.com.vng.zalopay.monitors.MonitorReportImpl;
import vn.com.vng.zalopay.monitors.MonitorTimingImpl;

/**
 * Created by huuhoa on 6/5/16.
 * Provide monitor modules
 */
public class MonitorModule {
    @Provides
    @Singleton
    IMonitorTiming provideTimingMonitor() {
        return new MonitorTimingImpl();
    }

    @Provides
    @Singleton
    IMonitorReport monitorReport() {
        return new MonitorReportImpl();
    }
}
