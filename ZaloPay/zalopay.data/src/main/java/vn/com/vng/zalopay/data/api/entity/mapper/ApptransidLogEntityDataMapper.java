package vn.com.vng.zalopay.data.api.entity.mapper;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogApiCallGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogTimingGD;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPApptransidLogApiCall;

/**
 * Created by khattn on 1/25/17.
 * Apptransid mapper
 */

@Singleton
public class ApptransidLogEntityDataMapper {
    final private String VALUE_APPTRANSID = "apptransid";
    final private String VALUE_APPID = "appid";
    final private String VALUE_STEP = "step";
    final private String VALUE_STEP_RESULT = "step_result";
    final private String VALUE_PCMID = "pcmid";
    final private String VALUE_TRANSTYPE = "transtype";
    final private String VALUE_TRANSID = "transid";
    final private String VALUE_SDK_RESULT = "sdk_result";
    final private String VALUE_SERVER_RESULT = "server_result";
    final private String VALUE_SOURCE = "source";
    final private String VALUE_START_TIME = "start_time";
    final private String VALUE_FINISH_TIME = "finish_time";
    final private String VALUE_TIMING = "timing";
    final private String VALUE_TIMESTAMP = "timestamp";
    final private String VALUE_BANK_CODE = "bank_code";
    final private String VALUE_STATUS = "status";
    final private String VALUE_API_CALL = "api_calls";
    final private String VALUE_APIID = "api";
    final private String VALUE_API_TIME_BEGIN = "time_begin";
    final private String VALUE_API_TIME_END = "time_end";
    final private String VALUE_API_RETURN_CODE = "return_code";

    @Inject
    public ApptransidLogEntityDataMapper() {

    }

    public JSONObject transform(ApptransidLogGD data, List<ApptransidLogTimingGD> timingData, List<ApptransidLogApiCallGD> apiCallData) {
        if (data == null) {
            return null;
        }

        JSONObject value = new JSONObject();

        try {
            value.put(VALUE_APPTRANSID, data.apptransid);
            value.put(VALUE_APPID, data.appid);
            value.put(VALUE_STEP, data.step);
            value.put(VALUE_STEP_RESULT, data.step_result);
            value.put(VALUE_PCMID, data.pcmid);
            value.put(VALUE_TRANSTYPE, data.transtype);
            value.put(VALUE_TRANSID, data.transid);
            value.put(VALUE_SDK_RESULT, data.sdk_result);
            value.put(VALUE_SERVER_RESULT, data.server_result);
            value.put(VALUE_SOURCE, data.source);
            value.put(VALUE_START_TIME, data.start_time);
            JSONArray array = new JSONArray();
            for (int i = 0; i < timingData.size(); i++) {
                JSONObject object = new JSONObject();
                object.put(VALUE_STEP, timingData.get(i).step);
                object.put(VALUE_TIMESTAMP, timingData.get(i).timestamp);
                array.put(object);
            }
            value.put(VALUE_TIMING, array);
            JSONArray apiCallArray = new JSONArray();
            for (int i = 0; i < apiCallData.size(); i++) {
                JSONObject object = new JSONObject();
                object.put(VALUE_APIID, apiCallData.get(i).apiid);
                object.put(VALUE_API_TIME_BEGIN, apiCallData.get(i).timebegin);
                object.put(VALUE_API_TIME_END, apiCallData.get(i).timeend);
                object.put(VALUE_API_RETURN_CODE, apiCallData.get(i).returncode);
                apiCallArray.put(object);
            }
            value.put(VALUE_API_CALL, apiCallArray);
            value.put(VALUE_FINISH_TIME, data.finish_time);
            value.put(VALUE_BANK_CODE, data.bank_code);
        } catch (JSONException e) {
            Timber.d("Fail to transform object");
            return null;
        }

        return value;
    }

    public ApptransidLogGD transform(ZPApptransidLog data) {
        ApptransidLogGD log = new ApptransidLogGD();

        log.apptransid = data.apptransid;

        if (data.appid != 0) {
            log.appid = data.appid;
        }

        if (data.step != 0) {
            log.step = data.step;
        }

        if (data.step_result != 0) {
            log.step_result = data.step_result;
        }

        if (data.pcmid != 0) {
            log.pcmid = data.pcmid;
        }

        if (data.transtype != 0) {
            log.transtype = data.transtype;
        }

        if (data.transid != 0) {
            log.transid = data.transid;
        }

        if (data.sdk_result != 0) {
            log.sdk_result = data.sdk_result;
        }

        if (data.server_result != 0) {
            log.server_result = data.server_result;
        }

        if (data.source != 0) {
            log.source = data.source;
        }

        if (data.start_time != 0) {
            log.start_time = data.start_time;
        }

        if (data.finish_time != 0) {
            log.finish_time = data.finish_time;
        }

        if (data.bank_code != null) {
            log.bank_code = data.bank_code;
        }

        if (data.status != 0) {
            log.status = data.status;
        } else {
            log.status = 0;
        }

        return log;
    }

    public ApptransidLogTimingGD transformTiming(ZPApptransidLog data) {
        if (data.step == 0 || data.finish_time == 0) {
            return null;
        }

        ApptransidLogTimingGD log = new ApptransidLogTimingGD();
        log.apptransid = data.apptransid;
        log.step = data.step;
        log.timestamp = data.finish_time;
        return log;
    }

    public ApptransidLogApiCallGD transformApiCall(ZPApptransidLogApiCall data) {
        if (data.apiid == 0 || TextUtils.isEmpty(data.apptransid)) {
            return null;
        }

        ApptransidLogApiCallGD log = new ApptransidLogApiCallGD();
        log.apptransid = data.apptransid;
        log.apiid = data.apiid;
        log.timebegin = data.time_begin;
        log.timeend = data.time_end;
        log.returncode = data.return_code;
        return log;
    }

}
