package vn.com.zalopay.wallet.ui;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.behavior.gateway.PlatformInfoLoader;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.interactor.IAppInfo;
import vn.com.zalopay.wallet.interactor.IBank;

/**
 * Created by chucvv on 6/24/17.
 */

public abstract class PaymentPresenter<T extends IContract> extends AbstractPresenter<T> {

    public Action1<Throwable> bankListException = this::loadBankListOnError;
    private Action0 appInfoInProcess = this::loadAppInfoOnProcess;
    private Action1<AppInfo> appInfoSubscriber = this::loadAppInfoOnComplete;
    private Action1<Throwable> appInfoException = this::loadAppInfoOnError;
    private Action1<BankConfigResponse> bankListSubscriber = this::loadBankListOnComplete;
    private Action0 loadBankInProcess = this::loadBankListOnProgress;

    protected abstract void onUpdateVersion(SdkUpVersionMessage pMessage);

    protected void loadAppInfoOnProcess() {
    }

    protected void loadAppInfoOnError(Throwable throwable) {
    }

    protected void loadAppInfoOnComplete(AppInfo appInfo) {
    }

    protected void loadBankListOnComplete(BankConfigResponse bankConfigResponse) {
    }

    protected void loadBankListOnError(Throwable throwable) {
    }

    protected void loadBankListOnProgress() {
    }

    protected boolean loadStaticResource(UserInfo userInfo) throws Exception {
        try {
            Log.d(this, "start load static resource");
            PlatformInfoLoader.getInstance(userInfo).checkPlatformInfo();
        } catch (Exception e) {
            Log.e(this, e);
            return false;
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnUpVersionEvent(SdkUpVersionMessage pMessage) {
        Log.d(this, "OnUpVersionEvent", pMessage);
        onUpdateVersion(pMessage);
    }

    public void callback() {
        Log.d(this, "callback presenter");
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
    }

    /***
     * load app info from cache or api
     */
    protected void loadAppInfo(IAppInfo appInfoInteractor, long appId, @TransactionType int transtype, UserInfo userInfo) {
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        long currentTime = System.currentTimeMillis();
        Subscription subscription = appInfoInteractor.loadAppInfo(appId, new int[]{transtype},
                userInfo.zalopay_userid, userInfo.accesstoken, appVersion, currentTime)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(appInfoInProcess)
                .subscribe(appInfoSubscriber, appInfoException);
        addSubscription(subscription);
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_GetAppInfo, ZPPaymentSteps.OrderStepResult_None);
        }
    }

    protected void loadBankList(IBank bankInteractor) {
        loadBankList(bankInteractor, bankListSubscriber);
    }

    public void loadBankList(IBank bankInteractor, Action1<BankConfigResponse> bankListSubscriber) {
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        long currentTime = System.currentTimeMillis();
        Subscription subscription = bankInteractor.getBankList(appVersion, currentTime)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(loadBankInProcess)
                .subscribe(bankListSubscriber, bankListException);
        addSubscription(subscription);
    }

}
