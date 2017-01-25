package vn.com.vng.zalopay.data.apptransidlog;

import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.model.ApptransidLogGD;
import vn.com.vng.zalopay.data.util.ObservableHelper;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

/**
 * Created by khattn on 1/24/17.
 */

public class ApptransidLogRepository implements ApptransidLogStore.Repository {
    private final ApptransidLogStore.RequestService mRequestService;
    private final ApptransidLogStore.LocalStorage mLocalStorage;

    public ApptransidLogRepository(ApptransidLogStore.RequestService mRequestService, ApptransidLogStore.LocalStorage mLocalStorage) {
        this.mRequestService = mRequestService;
        this.mLocalStorage = mLocalStorage;
    }

    @Override
    public Observable<Void> put(ApptransidLogGD val) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.updateLog(val);
            return null;
        });
    }

    @Override
    public Observable<ApptransidLogGD> get(String apptransid) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.get(apptransid));
    }

    @Override
    public Observable<Void> remove(String apptransid) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.delete(apptransid);
            return null;
        });
    }

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
