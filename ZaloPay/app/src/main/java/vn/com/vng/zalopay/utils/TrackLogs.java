package vn.com.vng.zalopay.utils;

import android.support.annotation.NonNull;

import java.util.Map;

import vn.com.vng.zalopay.data.LogLocalStorage;
import vn.com.vng.zalopay.data.cache.model.LogGD;

/**
 * Created by khattn on 1/23/17.
 */

public class TrackLogs {
    LogLocalStorage mLocalStorage;

    public TrackLogs(@NonNull LogLocalStorage logLocalStorage) {
        mLocalStorage = logLocalStorage;
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

        mLocalStorage.put(transform(eventBuilder.build()));
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
