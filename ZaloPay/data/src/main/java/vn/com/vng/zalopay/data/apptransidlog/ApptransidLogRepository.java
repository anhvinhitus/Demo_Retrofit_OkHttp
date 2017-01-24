package vn.com.vng.zalopay.data.apptransidlog;

import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.model.ApptransidLogGD;

/**
 * Created by khattn on 1/24/17.
 */

public class ApptransidLogRepository {
//    private final ApptransidLogStore.RequestService mRequestService;
//    private final ApptransidLogStore.LocalStorage mLocalStorage;
//
//    public ApptransidLogRepository(ApptransidLogStore.RequestService mRequestService, ApptransidLogStore.LocalStorage mLocalStorage) {
//        this.mRequestService = mRequestService;
//        this.mLocalStorage = mLocalStorage;
//    }
//
//    @Override
//    public Observable<Boolean> submitLog(String apptransid) {
//        ApptransidLogGD apptransidLogGD = mLocalStorage.get(apptransid);
//        return mRequestService
//                .submitLog(apptransidLogGD.apptransid, apptransidLogGD.appid,
//                        apptransidLogGD.step, apptransidLogGD.step_result, apptransidLogGD.pcmid, apptransidLogGD.transtype,
//                        apptransidLogGD.transid, apptransidLogGD.sdk_result, apptransidLogGD.server_result,
//                        apptransidLogGD.source)
//                .map(BaseResponse::isSuccessfulResponse);
//    }
}
