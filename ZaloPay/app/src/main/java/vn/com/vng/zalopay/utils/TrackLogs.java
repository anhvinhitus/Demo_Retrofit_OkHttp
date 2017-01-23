package vn.com.vng.zalopay.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.LogGD;
import vn.com.vng.zalopay.data.cache.model.LogGDDao;

/**
 * Created by khattn on 1/23/17.
 */

public class TrackLogs extends SqlBaseScopeImpl {

    public TrackLogs(DaoSession daoSession) {
        super(daoSession);
    }

    public void trackLog(String apptransid, int appid, int step, int step_result, int pcmid, int transtype,
                           long transid, int sdk_result, int server_result, String source) {

        TrackBuilders.AppTransIdBuilder eventBuilder = new TrackBuilders.AppTransIdBuilder()
                .setAppTransId(apptransid)
                .setAppId(appid)
                .setStep(step)
                .setStepResult(step_result)
                .setPcmId(pcmid)
                .setTransType(transtype)
                .setTransId(transid)
                .setSdkResult(sdk_result)
                .setServerResult(server_result)
                .setSource(source);

        put(transform(eventBuilder.build()));
    }

    public void put(LogGD val) {
        if (val == null) {
            return;
        }
        try {
            getDaoSession().getLogGDDao().insertOrReplace(val);
        } catch (Exception e) {
            Timber.d(e, "Insert log error");
            return;
        }
    }

    private LogGD get(String apptransid) {
        List<LogGD> list = new ArrayList<>();
        if (apptransid != null) {
            list = getDaoSession().getLogGDDao()
                    .queryBuilder()
                    .where(LogGDDao.Properties.Apptransid.eq(apptransid))
                    .list();
        }
        if (list == null || list.size() <= 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    private LogGD transform(Map<String, String> data) {
        LogGD log = new LogGD();

        log.apptransid = data.get("apptransid");
        log.appid = Integer.parseInt(data.get("appid"));
        log.step = Integer.parseInt(data.get("step"));
        log.step_result = Integer.parseInt(data.get("step_result"));
        log.pcmid = Integer.parseInt(data.get("pcmid"));
        log.transtype = Integer.parseInt(data.get("transtype"));
        log.transid = Long.valueOf(data.get("transid"));
        log.sdk_result = Integer.parseInt(data.get("sdk_result"));
        log.server_result = Integer.parseInt(data.get("server_result"));
        log.source = data.get("source");

        return log;
    }
}
