package vn.com.vng.zalopay.data.apptransidlog;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.model.ApptransidLogGD;
import vn.com.vng.zalopay.data.net.adapter.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by khattn on 1/24/17.
 */

public interface ApptransidLogStore {
    interface LocalStorage {

        void put(ApptransidLogGD val);

        ApptransidLogGD get(String apptransid);
    }

//    interface RequestService {
//
//        @API_NAME(ZPEvents.API_REDPACKAGE_CREATEBUNDLEORDER)
//        @FormUrlEncoded
//        @POST(Constants.REDPACKET_API.SUBMITTOSENDBUNDLE)
//        Observable<BaseResponse> submitLog(@Field("apptransid") String appTransId, @Field("appid") int appId, @Field("step") int step, @Field("step_result") int stepResult, @Field("pcmid") int pcmId, @Field("transtype") int transType, @Field("transid") long transId, @Field("sdk_result") int sdkResult, @Field("server_result") int serverResult, @Field("source") String source);
//
//    }
//
//    interface Repository {
//
//        Observable<Boolean> submitLog(String apptransid);
//    }
}
