package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.ApptransidLogEntity;
import vn.com.vng.zalopay.data.cache.model.ApptransidLogGD;

/**
 * Created by khattn on 1/25/17.
 */

@Singleton
public class ApptransidLogEntityDataMapper {

    @Inject
    public ApptransidLogEntityDataMapper() {

    }

    public ApptransidLogGD transform(ApptransidLogEntity data) {
        if (data == null) {
            return null;
        }

        ApptransidLogGD log = new ApptransidLogGD();
        log.apptransid = data.apptransid;
        log.appid = data.appid;
        log.step = data.step;
        log.step_result = data.step_result;
        log.pcmid = data.pcmid;
        log.transtype = data.transtype;
        log.transid = data.transid;
        log.sdk_result = data.sdk_result;
        log.server_result = data.server_result;
        log.source = data.source;
        return log;
    }

    public ApptransidLogEntity transform(ApptransidLogGD data) {
        if (data == null) {
            return null;
        }

        ApptransidLogEntity log = new ApptransidLogEntity();
        log.apptransid = data.apptransid;
        log.appid = data.appid;
        log.step = data.step;
        log.step_result = data.step_result;
        log.pcmid = data.pcmid;
        log.transtype = data.transtype;
        log.transid = data.transid;
        log.sdk_result = data.sdk_result;
        log.server_result = data.server_result;
        log.source = data.source;
        return log;
    }

    public ApptransidLogGD transform(Map<String, String> data) {
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

}
