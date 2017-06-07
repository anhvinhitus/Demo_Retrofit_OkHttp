package vn.com.zalopay.wallet.interactor;

import javax.inject.Inject;

import rx.Observable;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;
import vn.com.zalopay.wallet.repository.banklist.BankListStore;

/**
 * Created by chucvv on 6/7/17.
 */

public class PlatformInteractor implements IPlatform {
    public AppInfoStore.Repository mAppInfoRepository;
    public BankListStore.Repository mBankListRepository;

    @Inject
    public PlatformInteractor(AppInfoStore.Repository appInfoRepository, BankListStore.Repository bankListRepository) {
        this.mBankListRepository = bankListRepository;
        this.mAppInfoRepository = appInfoRepository;
        Log.d(this, "call constructor PlatformInteractor");
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
    public Observable<AppInfo> loadAppInfo(String appid, String userid, String accesstoken, long currentTime) {
        String checksum = mAppInfoRepository.getLocalStorage().getCheckSum(appid);
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        Observable<AppInfo> appInfoOnCache = mAppInfoRepository.getLocalStorage().getAppInfo(appid).onErrorReturn(null);
        Observable<AppInfo> appInfoOnCloud = mAppInfoRepository.fetchAppInfoCloud(appid, userid, accesstoken, checksum, appVersion);
        return Observable.concat(appInfoOnCache, appInfoOnCloud).first(appInfo -> appInfo != null && (appInfo.expriretime > currentTime)).compose(SchedulerHelper.applySchedulers());
    }

    @Override
    public Observable<BankConfigResponse> getBankList(String platform, String appversion, long currentTime) {
        String checksum = mBankListRepository.getLocalStorage().getCheckSum();
        Observable<BankConfigResponse> bankListCache = mBankListRepository.getLocalStorage().getBankList().onErrorReturn(null);
        Observable<BankConfigResponse> bankListCloud = mBankListRepository.fetchBankListCloud(platform, checksum, appversion);
        return Observable.concat(bankListCache, bankListCloud)
                .first(bankConfigResponse -> bankConfigResponse != null && (bankConfigResponse.expiredtime > currentTime))
                .compose(SchedulerHelper.applySchedulers());
    }
}
