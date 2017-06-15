package vn.com.zalopay.wallet.ui.channellist;

import android.app.Activity;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.business.behavior.gateway.PlatformInfoLoader;
import vn.com.zalopay.wallet.business.channel.injector.BaseChannelInjector;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkDownloadResourceMessage;
import vn.com.zalopay.wallet.event.SdkInvalidDataMessage;
import vn.com.zalopay.wallet.event.SdkLoadingTaskMessage;
import vn.com.zalopay.wallet.event.SdkResourceInitMessage;
import vn.com.zalopay.wallet.event.SdkSelectedChannelMessage;
import vn.com.zalopay.wallet.event.SdkStartInitResourceMessage;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.IAppInfo;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.AbstractPresenter;

/**
 * Created by chucvv on 6/12/17.
 */

public class ChannelListPresenter extends AbstractPresenter<ChannelListFragment> {

    @Inject
    public EventBus mBus;
    @Inject
    public IBank mBankInteractor;
    @Inject
    public IAppInfo mAppInfoInteractor;
    public Action1<Throwable> mBankListException = throwable -> {
        Log.d(this, "load appinfo on error", throwable);
        String message = TransactionHelper.getMessage(throwable);
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.zpw_alert_error_networking_when_load_banklist);
        }
        getViewOrThrow().showError(message);
    };
    protected PaymentInfoHelper mPaymentInfoHelper;
    private ChannelListAdapter mChannelAdapter;
    private ChannelProxy mChannelProxy;
    private List<Object> mChannelList = new ArrayList<>();
    private BaseChannelInjector baseChannelInjector;
    private int mPreviousPosition;
    private Action1<AppInfo> appInfoSubscriber = new Action1<AppInfo>() {
        @Override
        public void call(AppInfo appInfo) {
            Log.d(this, "load appinfo success", appInfo);
            if (appInfo == null || !appInfo.isAllow()) {
                getViewOrThrow().showAppInfoNotFoundDialog();
                return;
            }
            if (!mPaymentInfoHelper.isWithDrawTrans()) {
                getViewOrThrow().renderAppInfo(appInfo);
            }
            loadStaticReload();
        }
    };
    private Action1<Throwable> appInfoException = throwable -> {
        Log.d(this, "load appinfo on error", throwable);
        getViewOrThrow().hideLoading();
        //update payment status depend on api code from server
        if (throwable instanceof RequestException) {
            RequestException requestException = (RequestException) throwable;
            mPaymentInfoHelper.updateTransactionResult(requestException.code);
        }

        String message = TransactionHelper.getMessage(throwable);
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.sdk_load_appinfo_error_message);
        }
        boolean showDialog = ErrorManager.shouldShowDialog(mPaymentInfoHelper.getStatus());
        if (showDialog) {
            getViewOrThrow().showError(message);
        } else {
            getViewOrThrow().callbackThenterminate();
        }
    };
    private boolean setInputMethodTitle = false;

    public ChannelListPresenter() {
        SDKApplication.getApplicationComponent().inject(this);
        Log.d(this, "call constructor ChannelListPresenter");
    }

    @Override
    public void onStart() {
        mBus.register(this);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
    }

    @Override
    public void onResume() {

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnPaymentInfoEvent(PaymentInfoHelper paymentInfoHelper) {
        mBus.removeStickyEvent(PaymentInfoHelper.class);
        mPaymentInfoHelper = paymentInfoHelper;
        paymentInfoReady();
        Log.d(this, "got event payment info", mPaymentInfoHelper);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnSelectChannelEvent(SdkSelectedChannelMessage pMessage) {
        Log.d(this, "select at position", pMessage.position);
        if (mChannelList == null || mChannelList.size() <= 0) {
            Log.d(this, "channel list is empty");
            return;
        }
        if (pMessage.position >= 0) {//prevent so many click on channel
            Object object = mChannelList.get(pMessage.position);
            if (object instanceof PaymentChannel) {
                onSelectedChannel(pMessage.position, (PaymentChannel) object);
            }
        }
    }

    private void onSelectedChannel(int pPosition, PaymentChannel pChannel) {
        if (pChannel == null) {
            Log.d(this, "channel is null");
            return;
        }
        //check networking
        Activity activity = getViewOrThrow().getActivity();
        if (activity != null && !ConnectionUtil.isOnline(activity)) {
            getViewOrThrow().showOpenSettingNetwokingDialog(null);
            return;
        }
        if (!mChannelProxy.validateChannel(pChannel)) {
            return;
        }
        setSelectChannel(pPosition);
        mChannelAdapter.notifyBinderItemChanged(pPosition);

        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_ChoosePayMethod, ZPPaymentSteps.OrderStepResult_None, pChannel.pmcid);
        }
    }

    private void setSelectChannel(int pPosition) {
        if (mChannelList != null && mChannelList.size() > 0) {
            //reset the previous one
            try {
                Object object = mChannelList.get(mPreviousPosition);
                if (object instanceof PaymentChannel) {
                    ((PaymentChannel) object).select = false;
                }
            } catch (Exception e) {
                Log.e(this, e);
            }
            try {
                Object object = mChannelList.get(pPosition);
                if (object instanceof PaymentChannel) {
                    ((PaymentChannel) object).select = true;
                }
            } catch (Exception e) {
                Log.e(this, e);
            }
            mPreviousPosition = pPosition;//save to the previous position
        }
    }

    public void startPayment() {
        if (mPreviousPosition >= 0 && mChannelList != null && mChannelList.size() > 0) {
            try {
                Object object = mChannelList.get(mPreviousPosition);
                if (object instanceof PaymentChannel) {
                    mChannelProxy.setChannel((PaymentChannel) object).start();
                }
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    private void paymentInfoReady() {
        getViewOrThrow().setTitle(mPaymentInfoHelper.getTitleByTrans());
        getViewOrThrow().showOrderAmount(mPaymentInfoHelper.getAmount());
        getViewOrThrow().renderOrderInfo(mPaymentInfoHelper.getUserInfo(), mPaymentInfoHelper.getOrder(), mPaymentInfoHelper.getTranstype());
        //init channel proxy
        mChannelProxy = ChannelProxy.get()
                .setChannelListPresenter(this)
                .setPaymentInfo(mPaymentInfoHelper);
        //validate user level
        if (!mPaymentInfoHelper.userLevelValid()) {
            getViewOrThrow().showForceUpdateLevelDialog();
            return;
        }
        //check app info whether this transaction is allowed or not
        loadAppInfo();
    }

    private void send(PaymentChannel pChannel) {
        ChannelListAdapter.ItemType itemType;
        if (pChannel.isZaloPayChannel()) {
            pChannel.select = true;
            mPreviousPosition = 0;
            itemType = ChannelListAdapter.ItemType.ZALOPAY;
        } else if (pChannel.isMapCardChannel() || pChannel.isBankAccountMap()) {
            itemType = ChannelListAdapter.ItemType.MAP;
        } else {
            itemType = ChannelListAdapter.ItemType.INPUT;
            if (!setInputMethodTitle) {
                mChannelAdapter.setTitle(mPaymentInfoHelper.getPaymentMethodTitleByTrans());
                setInputMethodTitle = true;
            }
        }
        mChannelAdapter.setChannel(itemType, pChannel);
    }

    private void doCompleteLoadChannel(){
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.ZALOPAY));
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.MAP));
        if(mChannelAdapter.hasTitle()){
            mChannelList.add(new Object());
        }
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.INPUT));
        // don't have any channel now
        if (mChannelList.size() <= 0) {
            /***
             * this is withdraw link card and no mapp card.
             * need remind user go to link card to can withdraw
             */
            if (mPaymentInfoHelper.isWithDrawTrans()) {
                getViewOrThrow().showWarningLinkCardBeforeWithdraw();
            } else {
                String alertMessage = baseChannelInjector.getAlertAmount(mPaymentInfoHelper.getAmount());
                if (TextUtils.isEmpty(alertMessage)) {
                    alertMessage = GlobalData.getStringResource(RS.string.zpw_app_info_exclude_channel);
                }
                getViewOrThrow().showError(alertMessage);
            }
        }
    }

    private Observer<PaymentChannel> getChannelObserver() {
        return new Observer<PaymentChannel>() {
            @Override
            public void onCompleted() {
                Log.d(this, "load channels on complete");
                doCompleteLoadChannel();
                getViewOrThrow().hideLoading();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(this, "load channel on error", e);
                getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zpw_alert_error_data));
            }

            @Override
            public void onNext(PaymentChannel paymentChannel) {
                Log.d(this, "load channel on next", paymentChannel);
                if (mPaymentInfoHelper.shouldIgnore(paymentChannel.pmcid)) {
                    Log.d(this, "this channel is not in filter list");
                    return;
                }
                send(paymentChannel);
            }
        };
    }

    /***
     * prepare show channel and show header.
     */
    private synchronized void loadChannels() {
        getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_process_view));
        try {
            Log.d(this, "preparing channels");
            mChannelAdapter = new ChannelListAdapter(getViewOrThrow().getContext(), mPaymentInfoHelper.getAmount(),
                    mPaymentInfoHelper.getUserInfo(), mPaymentInfoHelper.getTranstype());
            getViewOrThrow().onBindingChannel(mChannelAdapter);

            baseChannelInjector = BaseChannelInjector.createChannelInjector(mPaymentInfoHelper);
            baseChannelInjector.source.subscribe(getChannelObserver());
            baseChannelInjector.getChannels();
        } catch (Exception e) {
            Log.e(this, e);
            getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zpw_alert_error_data));
        }

    }

    private void readyForPayment() {
        getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        loadBankList(new Action1<BankConfigResponse>() {
            @Override
            public void call(BankConfigResponse bankConfigResponse) {
                loadChannels();
                Log.d(this, "load bank list finish");
            }
        }, mBankListException);
        Log.d(this, "ready for payment");
    }

    public void loadBankList(Action1<BankConfigResponse> success, Action1<Throwable> error) {
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        long currentTime = System.currentTimeMillis();
        Subscription subscription = mBankInteractor.getBankList(appVersion, currentTime)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success, error);
        addSubscription(subscription);
    }

    public boolean isUniqueChannel() {
        return mChannelList != null || mChannelList.size() <= 0;
    }

    public void exitHasOneChannel() {
        if (isUniqueChannel()) {
            getViewOrThrow().callbackThenterminate();
        }
    }

    /***
     * load app info from cache or api
     */
    private void loadAppInfo() {
        long appId = mPaymentInfoHelper.getAppId();
        @TransactionType int transtype = mPaymentInfoHelper.getTranstype();
        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        long currentTime = System.currentTimeMillis();
        Subscription subscription = mAppInfoInteractor.loadAppInfo(appId, new int[]{transtype},
                userInfo.zalopay_userid, userInfo.accesstoken, appVersion, currentTime)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_check_app_info)))
                .subscribe(appInfoSubscriber, appInfoException);
        addSubscription(subscription);
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_GetAppInfo, ZPPaymentSteps.OrderStepResult_None);
        }
    }

    private void loadStaticReload() {
        try {
            Log.d(this, "check static resource start");
            PlatformInfoLoader.getInstance(mPaymentInfoHelper.getUserInfo()).checkPlatformInfo();
        } catch (Exception e) {
            getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
            Log.e(this, e);
        }
    }

    public void terminate() {
        Log.d(this, "terminate presenter");
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
    }

    public void setPaymentStatusAndCallback(@PaymentStatus int pStatus) {
        mPaymentInfoHelper.setResult(pStatus);
        terminate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvalidDataEvent(SdkInvalidDataMessage eventMessge) {
        mPaymentInfoHelper.setResult(PaymentStatus.INVALID_DATA);
        getViewOrThrow().showError(eventMessge.message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnTaskInProcessEvent(SdkLoadingTaskMessage pMessage) {
        getViewOrThrow().showLoading(pMessage.message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnInitialResourceCompleteEvent(SdkResourceInitMessage pMessage) {
        Log.d(this, "OnFinishInitialResourceEvent" + GsonUtils.toJsonString(pMessage));
        if (pMessage.success) {
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
            Subscription subscription = SDKApplication.getApplicationComponent()
                    .linkInteractor()
                    .getMap(userInfo.zalopay_userid, userInfo.accesstoken, false, appVersion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aBoolean -> readyForPayment(), throwable -> {
                        getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zpw_generic_error));
                        Log.e("load card and bank account error", throwable.getMessage());
                    });
            addSubscription(subscription);
        } else {
            Log.d(this, "init resource error " + pMessage);
            /***
             * delete folder resource to download again.
             * this prevent case file resource downloaded but was damaged on the wire so
             * can not parse json file.
             */
            try {
                String resPath = SharedPreferencesManager.getInstance().getUnzipPath();
                if (!TextUtils.isEmpty(resPath))
                    StorageUtil.deleteRecursive(new File(resPath));
            } catch (Exception e) {
                Log.d(this, e);
            }
            String message = pMessage.message;
            if (TextUtils.isEmpty(message)) {
                message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
            }
            boolean showDialog = ErrorManager.shouldShowDialog(mPaymentInfoHelper.getStatus());
            if (showDialog) {
                getViewOrThrow().showError(message);
            } else {
                getViewOrThrow().callbackThenterminate();
            }
        }
    }

    private void notifyUpVersionToApp(boolean pForceUpdate, String pVersion, String pMessage) {
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onUpVersion(pForceUpdate, pVersion, pMessage);
        }
        if (pForceUpdate) {
            getViewOrThrow().terminate();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnUpVersionEvent(SdkUpVersionMessage pMessage) {
        Log.d(this, "OnUpVersionEvent" + GsonUtils.toJsonString(pMessage));
        notifyUpVersionToApp(pMessage.forceupdate, pMessage.version, pMessage.message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnDownloadResourceMessageEvent(SdkDownloadResourceMessage result) {
        Log.d(this, "OnDownloadResourceMessageEvent " + GsonUtils.toJsonString(result));
        if (result.success) {
            SdkStartInitResourceMessage message = new SdkStartInitResourceMessage();
            mBus.post(message);
        } else {
            SdkResourceInitMessage message = new SdkResourceInitMessage(result.success, result.message);
            mBus.post(message);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnInitialResourceCompleteEvent(SdkStartInitResourceMessage pMessage) {
        if (!SDKApplication.getApplicationComponent().platformInfoInteractor().isValidConfig()) {
            Log.d(this, "call init resource but not ready for now, waiting for downloading resource");
            return;
        }
        Subscription subscription = ResourceManager.initResource()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    SdkResourceInitMessage message = new SdkResourceInitMessage(true);
                    mBus.post(message);
                }, throwable -> {
                    SdkResourceInitMessage message = new SdkResourceInitMessage(false, GlobalData.getStringResource(RS.string.zpw_alert_error_resource_not_download));
                    mBus.post(message);
                    Log.d("init resource fail", throwable);
                });
        addSubscription(subscription);
    }
}
