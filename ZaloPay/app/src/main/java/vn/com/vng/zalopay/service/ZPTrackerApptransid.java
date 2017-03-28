package vn.com.vng.zalopay.service;

import java.util.HashMap;
import java.util.Map;

import rx.Observer;
import vn.com.vng.zalopay.data.api.entity.ApptransidLogEntity;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.utils.TrackBuilders;
import vn.com.zalopay.analytics.ZPTracker;
import vn.com.zalopay.wallet.utils.Log;

/**
 * Created by khattn on 1/23/17.
 */

public class ZPTrackerApptransid implements ZPTracker {

    private ApptransidLogStore.Repository mRepository;

    public ZPTrackerApptransid(ApptransidLogStore.Repository logLocalStorage) {
        this.mRepository = logLocalStorage;
    }

    @Override
    public void trackEvent(int eventId, Long eventValue) {

    }

    @Override
    public void trackScreen(String screenName) {

    }

    @Override
    public void trackTiming(int eventId, long value) {

    }

    @Override
    public void trackApptransidEvent(String apptransid, int appid, int step, int step_result,
                                     int pcmid, int transtype, long transid, int sdk_result,
                                     int server_result, String source) {

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

        mRepository.remove(apptransid).subscribe();
//        mRepository.put(eventBuilder.build()).subscribe();
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

    private static Map<String, String> transform(ApptransidLogEntity data) {
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
