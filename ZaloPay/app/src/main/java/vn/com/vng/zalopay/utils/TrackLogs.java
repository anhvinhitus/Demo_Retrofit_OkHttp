package vn.com.vng.zalopay.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Observer;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogLocalStorage;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.data.cache.model.ApptransidLogGD;

/**
 * Created by khattn on 1/23/17.
 */

public class TrackLogs {
    private final ApptransidLogStore.Repository mRepository;

    @Inject
    TrackLogs(ApptransidLogStore.Repository logLocalStorage) {
        this.mRepository = logLocalStorage;
    }

    public void trackLog(String apptransid, int appid, int step, int step_result, int pcmid, int transtype,
                           long transid, int sdk_result, int server_result, String source) {

        final TrackBuilders.AppTransIdBuilder eventBuilder = new TrackBuilders.AppTransIdBuilder()
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

//        mRepository.remove(apptransid).subscribe();
        mRepository.put(transform(eventBuilder.build())).subscribe();
//        mRepository.get(apptransid).subscribe(new Observer<ApptransidLogGD>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onNext(ApptransidLogGD apptransidLogGD) {
//                Log.d("TEST", "trackLog: ");
//                Log.d("TEST", "trackLogValue: " + transform(apptransidLogGD).toString());
//            }
//        });
    }

    private ApptransidLogGD transform(Map<String, String> data) {
        ApptransidLogGD log = new ApptransidLogGD();

        for (Map.Entry<String, String> entry: data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            switch (key) {
                case "apptransid":
                    log.apptransid = value;
                    break;
                case "appid":
                    log.appid = Integer.valueOf(value);
                    break;
                case "step":
                    log.step = Integer.valueOf(value);
                    break;
                case "step_result":
                    log.step_result = Integer.valueOf(value);
                    break;
                case "pcmid":
                    log.pcmid = Integer.valueOf(value);
                    break;
                case "transtype":
                    log.transtype = Integer.valueOf(value);
                    break;
                case "transid":
                    log.transid = Long.valueOf(value);
                    break;
                case "sdk_result":
                    log.sdk_result = Integer.valueOf(value);
                    break;
                case "server_result":
                    log.server_result = Integer.valueOf(value);
                    break;
                case "source":
                    log.source = value;
                    break;
            }
        }

        return log;
    }

//    private Map<String, String> transform(ApptransidLogGD data) {
//        Map<String, String> log = new HashMap<>();
//
//        log.put("apptransid", data.apptransid);
//        log.put("appid", String.valueOf(data.appid));
//        log.put("step", String.valueOf(data.step));
//        log.put("step_result", String.valueOf(data.step_result));
//        log.put("pcmid", String.valueOf(data.pcmid));
//        log.put("transtype", String.valueOf(data.transtype));
//        log.put("transid", String.valueOf(data.transid));
//        log.put("sdk_result", String.valueOf(data.sdk_result));
//        log.put("server_result", String.valueOf(data.server_result));
//        log.put("source", data.source);
//
//        return log;
//    }
}
