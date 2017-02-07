package vn.com.vng.zalopay.utils;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Observer;
import vn.com.vng.zalopay.data.api.entity.ApptransidLogEntity;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.zalopay.wallet.utils.Log;

/**
 * Created by khattn on 2/7/17.
 */

public class ApptransidLogWriter {
    private final ApptransidLogStore.Repository mRepository;

    public ApptransidLogWriter(ApptransidLogStore.Repository logLocalStorage) {
        this.mRepository = logLocalStorage;
    }

    public void writeLog(Map<String, String> log, String apptransid) {

//        mRepository.remove(apptransid).subscribe();
        mRepository.put(log).subscribe();
//        mRepository.get(apptransid).subscribe(new Observer<ApptransidLogEntity>() {
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
//            public void onNext(ApptransidLogEntity apptransidLog) {
//                Log.d("TEST", "trackLog: " + transform(apptransidLog).toString());
//            }
//        });
    }

//    private Map<String, String> transform(ApptransidLogEntity data) {
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
