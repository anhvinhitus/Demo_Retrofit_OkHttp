package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.monitors.IMonitorReport;
import vn.com.vng.zalopay.monitors.IMonitorTiming;
import vn.com.vng.zalopay.monitors.MonitorReportImpl;
import vn.com.vng.zalopay.monitors.MonitorTimingImpl;

/**
 * Created by huuhoa on 6/5/16.
 * Provide monitor modules
 */
@Module
public class AppMonitorModule {
    @Provides
    @Singleton
    IMonitorTiming provideTimingMonitor(IMonitorReport monitorReport) {
        return new MonitorTimingImpl(monitorReport);
    }

    @Provides
    @Singleton
    IMonitorReport provideMonitorReport() {
        return new MonitorReportImpl();
    }
}
