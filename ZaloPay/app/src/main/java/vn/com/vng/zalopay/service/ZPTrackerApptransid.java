package vn.com.vng.zalopay.service;

import vn.com.vng.zalopay.utils.ApptransidLogWriter;
import vn.com.vng.zalopay.utils.TrackBuilders;

/**
 * Created by khattn on 1/23/17.
 */

public class ZPTrackerApptransid {

    private static ApptransidLogWriter mWriter;

    public static void initialize(ApptransidLogWriter apptransidLogWriter) {
        mWriter = apptransidLogWriter;
    }

    public static void trackEvent(String apptransid, int appid, int step, int step_result, int pcmid, int transtype,
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

        mWriter.writeLog(eventBuilder.build(), apptransid);
    }
}
