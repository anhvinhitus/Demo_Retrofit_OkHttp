package vn.com.zalopay.wallet.interactor;

import android.text.TextUtils;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;

/**
 * Interactor decide which get data from
 * do some bussiness logic on return result and delegate the result to caller
 * Created by chucvv on 6/8/17.
 */

public class AppInfoInteractor implements IAppInfo {
    public AppInfoStore.Repository mAppInfoRepository;

    @Inject
    public AppInfoInteractor(AppInfoStore.Repository appInfoRepository) {
        this.mAppInfoRepository = appInfoRepository;
        Log.d(this, "call constructor AppInfoInteractor");
    }

    /***
     *if app info is not expire then use data on local
     * other hand, call api to reload app info
     * @param appid
     * @param userid
     * @param accesstoken
     * @param currentTime
     * @return
     */
    @Override
    public Observable<AppInfo> loadAppInfo(long appid, @TransactionType int[] transtypes, String userid, String accesstoken, String appversion, long currentTime) {
        String appInfoCheckSum = mAppInfoRepository.getLocalStorage().getAppInfoCheckSum(appid);
        String transtypeString = transtypeToString(transtypes);
        String transtypeCheckSum = transtypeCheckSum(appid, transtypes);
        Observable<AppInfo> appInfoOnCache = mAppInfoRepository
                .getLocalStorage()
                .get(appid)
                .onErrorReturn(null);
        Observable<AppInfo> appInfoOnCloud = mAppInfoRepository
                .fetchCloud(appid, userid, accesstoken, appInfoCheckSum, transtypeString, transtypeCheckSum, appversion)
                .flatMap(mapResult(appid));
        return Observable.concat(appInfoOnCache, appInfoOnCloud).first(appInfo -> appInfo != null && (appInfo.expriretime > currentTime))
                .compose(SchedulerHelper.applySchedulers());
    }

    protected Func1<AppInfoResponse, Observable<AppInfo>> mapResult(long appId) {
        return appInfoResponse -> {
            if (appInfoResponse == null) {
                return Observable.error(new RequestException(RequestException.NULL, null));
            } else if (appInfoResponse.returncode == 1) {
                //success, load appinfo from cache
                return mAppInfoRepository.getLocalStorage().get(appId);
            } else {
                return Observable.error(new RequestException(appInfoResponse.returncode, appInfoResponse.returnmessage));
            }
        };
    }

    protected String transtypeToString(@TransactionType int[] pTranstype) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < pTranstype.length; i++) {
            stringBuilder.append(pTranstype[i]);
            if (i + 1 < pTranstype.length) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    protected String transtypeCheckSum(long appId, @TransactionType int[] transtypes) {
        String[] transtypeCheckSum = new String[0];
        try {
            String appInfoCheckSum = mAppInfoRepository.getLocalStorage().getAppInfoCheckSum(appId);
            if (!TextUtils.isEmpty(appInfoCheckSum)) {
                transtypeCheckSum = new String[transtypes.length];
                for (int i = 0; i < transtypes.length; i++) {
                    transtypeCheckSum[i] = mAppInfoRepository.getLocalStorage()
                            .getTranstypeCheckSum(mAppInfoRepository.getLocalStorage().getTranstypeCheckSumKey(appId, transtypes[i]));
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        boolean isReload = false;
        for (int i = 0; i < transtypeCheckSum.length; i++) {
            if (TextUtils.isEmpty(transtypeCheckSum[i])) {
                isReload = true;
                break;
            }
            stringBuilder.append(transtypeCheckSum[i]);
            if (i + 1 < transtypeCheckSum.length) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        if (isReload) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("[]");
        }
        return stringBuilder.toString();
    }

    @Override
    public void setExpireTime(long appId, long expireTime) {
        mAppInfoRepository.getLocalStorage().setExpireTime(appId, expireTime);
    }
}
