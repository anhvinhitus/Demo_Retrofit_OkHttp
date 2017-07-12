package vn.com.zalopay.wallet.repository.platforminfo;

import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PlatformInfoResponse;
import vn.com.zalopay.wallet.constants.ConstantParams;

/**
 * Created by chucvv on 6/7/17.
 */

public class PlatformInfoRepository implements PlatformInfoStore.Repository {
    private PlatformInfoStore.LocalStorage localStorage;
    private PlatformInfoStore.PlatformInfoService platformInfoService;

    @Inject
    public PlatformInfoRepository(PlatformInfoStore.PlatformInfoService platformInfoService, PlatformInfoStore.LocalStorage localStorage) {
        this.platformInfoService = platformInfoService;
        this.localStorage = localStorage;
    }

    @Override
    public Observable<PlatformInfoResponse> fetchCloud(Map<String, String> params) {
        long startTime = System.currentTimeMillis();
        return platformInfoService.fetch(params)
                .doOnNext(platformInfoResponse -> localStorage.put(params.get(ConstantParams.USER_ID), platformInfoResponse))
                .doOnNext(platformInfoResponse -> {
                    long endTime = System.currentTimeMillis();
                    if (GlobalData.analyticsTrackerWrapper != null) {
                        int returnCode = platformInfoResponse != null ? platformInfoResponse.returncode : -100;
                        GlobalData.analyticsTrackerWrapper.trackApiTiming(ZPEvents.CONNECTOR_V001_TPE_V001GETPLATFORMINFO, startTime, endTime, returnCode);
                    }
                });
    }

    @Override
    public PlatformInfoStore.LocalStorage getLocalStorage() {
        return localStorage;
    }
}
