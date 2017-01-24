package vn.com.vng.zalopay.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogLocalStorage;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.data.cache.model.ApptransidLogGD;

/**
 * Created by khattn on 1/23/17.
 */

public class TrackLogs {
    private final ApptransidLogStore.LocalStorage mLocalStorage;

    @Inject
    TrackLogs(ApptransidLogStore.LocalStorage logLocalStorage) {
        this.mLocalStorage = logLocalStorage;
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
        Log.d("TEST", "trackLog: " + transform(mLocalStorage.get(apptransid)).toString());
    }

    private ApptransidLogGD transform(Map<String, String> data) {
        ApptransidLogGD log = new ApptransidLogGD();

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

    private Map<String, String> transform(ApptransidLogGD data) {
        Map<String, String> log = new HashMap<>();

        log.put("apptransid", data.apptransid);
        log.put("appid", String.valueOf(data.appid));
        log.put("step", String.valueOf(data.step));
        log.put("step_result", String.valueOf(data.step_result));
        log.put("pcmid", String.valueOf(data.pcmid));
        log.put("transtype", String.valueOf(data.transtype));
        log.put("transid", String.valueOf(data.transid));
        log.put("sdk_result", String.valueOf(data.sdk_result));
        log.put("server_result", String.valueOf(data.server_result));
        log.put("source", data.source);

        return log;
    }
}
