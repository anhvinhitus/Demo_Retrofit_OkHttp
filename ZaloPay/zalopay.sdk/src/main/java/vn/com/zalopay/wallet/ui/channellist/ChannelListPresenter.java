package vn.com.zalopay.wallet.ui.channellist;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.business.behavior.gateway.PlatformInfoLoader;
import vn.com.zalopay.wallet.business.channel.injector.AbstractChannelLoader;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
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
import vn.com.zalopay.wallet.ui.BaseActivity;

/**
 * Created by chucvv on 6/12/17.
 */

public class ChannelListPresenter extends AbstractPresenter<ChannelListFragment> {
    public static final int REQUEST_CODE = 1000;
    @Inject
    public EventBus mBus;
    @Inject
    public IBank mBankInteractor;
    @Inject
    public IAppInfo mAppInfoInteractor;
    public Action1<Throwable> mBankListException = throwable -> {
        Log.d(this, "load bank list error", throwable);
        String message = TransactionHelper.getMessage(throwable);
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.zpw_alert_error_networking_when_load_banklist);
        }
        try {
            getViewOrThrow().showError(message);
        } catch (Exception e) {
            Log.d(this, e);
        }
    };
    protected PaymentInfoHelper mPaymentInfoHelper;
    private ChannelListAdapter mChannelAdapter;
    private ChannelProxy mChannelProxy;
    private List<Object> mChannelList = new ArrayList<>();
    private AbstractChannelLoader mChannelLoader;
    private int mPreviousPosition = -1;
    private Action1<AppInfo> appInfoSubscriber = new Action1<AppInfo>() {
        @Override
        public void call(AppInfo appInfo) {
            try {
                Log.d(this, "load app info success", appInfo);
                if (appInfo == null || !appInfo.isAllow()) {
                    getViewOrThrow().showAppInfoNotFoundDialog();
                    return;
                }
                String appName = TransactionHelper.getAppNameByTranstype(GlobalData.getAppContext(), mPaymentInfoHelper.getTranstype());
                if (TextUtils.isEmpty(appName)) {
                    appName = appInfo.appname;
                }
                getViewOrThrow().renderAppInfo(appName);
                loadStaticReload();
            } catch (Exception e) {
                Log.d(this, e);
            }
        }
    };
    private Action1<Throwable> appInfoException = throwable -> {
        Log.d(this, "load app info on error", throwable);
        try {
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
        } catch (Exception e) {
            Log.d(this, e);
        }
    };
    private boolean setInputMethodTitle = false;

    public ChannelListPresenter() {
        SDKApplication.getApplicationComponent().inject(this);
        Log.d(this, "call constructor ChannelListPresenter");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            Log.d(this, "onActivityResult resultCode", resultCode);
            switch (resultCode) {
                case Activity.RESULT_OK:
                    callback();
                    try {
                        getViewOrThrow().terminate();
                    } catch (Exception e) {
                        Log.d(this, e);
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    if (data != null) {
                        boolean showDialog = data.getBooleanExtra(Constants.SHOW_DIALOG, false);
                        String message = data.getStringExtra(Constants.MESSAGE);
                        if (showDialog && !TextUtils.isEmpty(message)) {
                            try {
                                getViewOrThrow().showInfoDialog(message);
                            } catch (Exception e) {
                                Log.d(this, e);
                            }
                        }
                    }
                    break;
            }
        }
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
        try {
            //check networking
            Activity activity = BaseActivity.getCurrentActivity();
            if (activity != null && !ConnectionUtil.isOnline(activity)) {
                getViewOrThrow().showOpenSettingNetwokingDialog(null);
                return;
            }
            if (!mChannelProxy.validate(pChannel)) {
                return;
            }
            setSelectChannel(pPosition);
            mChannelAdapter.notifyBinderItemChanged(pPosition);
            //update fee
            if (pChannel.hasFee()) {
                double fee = pChannel.totalfee;
                double total_amount = mPaymentInfoHelper.getAmount() + fee;
                getViewOrThrow().renderOrderFee(total_amount, fee);
            }
            if (GlobalData.analyticsTrackerWrapper != null) {
                GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_ChoosePayMethod, ZPPaymentSteps.OrderStepResult_None, pChannel.pmcid);
            }
        } catch (Exception e) {
            Log.d(this, e);
        }
    }

    private void setSelectChannel(int pPosition) {
        if (mChannelList != null && mChannelList.size() > 0 && pPosition >= 0) {
            //reset the previous one
            try {
                if (mPreviousPosition >= 0) {
                    Object object = mChannelList.get(mPreviousPosition);
                    if (object instanceof PaymentChannel) {
                        ((PaymentChannel) object).select = false;
                    }
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
        try {
            getViewOrThrow().setTitle(mPaymentInfoHelper.getTitleByTrans());
            getViewOrThrow().renderOrderInfo(mPaymentInfoHelper.getOrder());
            renderItemDetail();
            //init channel proxy
            mChannelProxy = ChannelProxy.get()
                    .setChannelListPresenter(this)
                    .setPaymentInfo(mPaymentInfoHelper)
                    .setBankInteractor(mBankInteractor);
            //validate user level
            if (!mPaymentInfoHelper.userLevelValid()) {
                getViewOrThrow().showForceUpdateLevelDialog();
                return;
            }
            //check app info whether this transaction is allowed or not
            loadAppInfo();
        } catch (Exception e) {
            Log.d(this, e);
        }
    }

    private void renderItemDetail() throws Exception {
        if (TextUtils.isEmpty(mPaymentInfoHelper.getOrder().item)) {
            Log.d(this, "item is empty - skip render item detail");
            return;
        }
        List<NameValuePair> items = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(mPaymentInfoHelper.getOrder().item);
            String itemExt = jsonObject.optString("ext");
            if (!TextUtils.isEmpty(itemExt)) {
                items = Strings.parseNameValues(itemExt);
            }
        } catch (Exception e) {
            Log.d(this, e);
        }

       /* List<NameValuePair> expected = new ArrayList<>();
        expected.add(new NameValuePair("Nhà mạng", "Viettel"));
        expected.add(new NameValuePair("Mệnh giá", "50.000 VND"));
        expected.add(new NameValuePair("Nạp cho", "Số của tôi - 0902167233"));*/
        getViewOrThrow().renderDynamicItemDetail(items);
    }

    private void send(PaymentChannel pChannel) {
        ChannelListAdapter.ItemType itemType;
        if (pChannel.isZaloPayChannel()) {
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

    private void doCompleteLoadChannel() {
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.ZALOPAY));
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.MAP));
        if (mChannelAdapter.hasTitle()) {
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

                try {
                    getViewOrThrow().showWarningLinkCardBeforeWithdraw();
                } catch (Exception e) {
                    Log.d(this, e);
                }
            } else {
                String alertMessage = mChannelLoader.getAlertAmount(mPaymentInfoHelper.getAmount());
                if (TextUtils.isEmpty(alertMessage)) {
                    alertMessage = GlobalData.getStringResource(RS.string.zpw_app_info_exclude_channel);
                }
                try {
                    getViewOrThrow().showError(alertMessage);
                } catch (Exception e) {
                    Log.d(this, e);
                }
            }
        }
        makeDefaultSelection();
        mChannelLoader = null;
    }

    /***
     * make default select channel
     */
    private void makeDefaultSelection() {
        for (int i = 0; i < mChannelList.size(); i++) {
            Object object = mChannelList.get(i);
            if (object instanceof PaymentChannel) {
                PaymentChannel paymentChannel = (PaymentChannel) object;
                if (paymentChannel.isEnable()) {
                    setSelectChannel(i);
                    break;
                }
            }
        }
    }

    private Observer<PaymentChannel> getChannelObserver() {
        return new Observer<PaymentChannel>() {
            @Override
            public void onCompleted() {
                Log.d(this, "load channels on complete");
                doCompleteLoadChannel();
                try {
                    getViewOrThrow().hideLoading();
                } catch (Exception e) {
                    Log.d(this, e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(this, "load channel on error", e);
                try {
                    getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zpw_alert_error_data));
                } catch (Exception e1) {
                    Log.d(this, e);
                }
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
    private synchronized void loadChannels() throws Exception {
        getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_process_view));
        try {
            Log.d(this, "preparing channels");
            mChannelAdapter = new ChannelListAdapter(getViewOrThrow().getContext(), mPaymentInfoHelper.getAmount(),
                    mPaymentInfoHelper.getUserInfo(), mPaymentInfoHelper.getTranstype());
            getViewOrThrow().onBindingChannel(mChannelAdapter);

            mChannelLoader = AbstractChannelLoader.createChannelInjector(mPaymentInfoHelper.getAppId(),
                    mPaymentInfoHelper.getUserId(), mPaymentInfoHelper.getAmount(), mPaymentInfoHelper.getBalance(),
                    mPaymentInfoHelper.getTranstype());
            mChannelLoader.source.subscribe(getChannelObserver());
            mChannelLoader.getChannels();
        } catch (Exception e) {
            Log.e(this, e);
            getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zpw_alert_error_data));
        }

    }

    private void readyForPayment() throws Exception {
        getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        loadBankList(new Action1<BankConfigResponse>() {
            @Override
            public void call(BankConfigResponse bankConfigResponse) {
                try {
                    loadChannels();
                } catch (Exception e) {
                    Log.d(this, e);
                }
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
        return mChannelList == null || mChannelList.size() <= 0;
    }

    public void exitHasOneChannel() {
        if (isUniqueChannel()) {
            try {
                getViewOrThrow().callbackThenterminate();
            } catch (Exception e) {
                Log.d(this, e);
            }
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
                .doOnSubscribe(() -> {
                    try {
                        getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_check_app_info));
                    } catch (Exception e) {
                        Log.d(this, e);
                    }
                })
                .subscribe(appInfoSubscriber, appInfoException);
        addSubscription(subscription);
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_GetAppInfo, ZPPaymentSteps.OrderStepResult_None);
        }
    }

    private void loadStaticReload() throws Exception {
        try {
            Log.d(this, "check static resource start");
            PlatformInfoLoader.getInstance(mPaymentInfoHelper.getUserInfo()).checkPlatformInfo();
        } catch (Exception e) {
            getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
            Log.e(this, e);
        }
    }

    public void callback() {
        Log.d(this, "callback presenter");
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
    }

    public void setPaymentStatusAndCallback(@PaymentStatus int pStatus) {
        mPaymentInfoHelper.setResult(pStatus);
        callback();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvalidDataEvent(SdkInvalidDataMessage eventMessge) {
        mPaymentInfoHelper.setResult(PaymentStatus.INVALID_DATA);
        try {
            getViewOrThrow().showError(eventMessge.message);
        } catch (Exception e) {
            Log.d(this, e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnTaskInProcessEvent(SdkLoadingTaskMessage pMessage) {
        try {
            getViewOrThrow().showLoading(pMessage.message);
        } catch (Exception e) {
            Log.d(this, e);
        }
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
                    .subscribe(aBoolean -> {
                        try {
                            readyForPayment();
                        } catch (Exception e) {
                            Log.d(this, e);
                        }
                    }, throwable -> {
                        try {
                            getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zpw_generic_error));
                        } catch (Exception e) {
                            Log.d(this, e);
                        }
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
                try {
                    getViewOrThrow().showError(message);
                } catch (Exception e) {
                    Log.d(this, e);
                }
            } else {
                try {
                    getViewOrThrow().callbackThenterminate();
                } catch (Exception e) {
                    Log.d(this, e);
                }
            }
        }
    }

    private void notifyUpVersionToApp(boolean pForceUpdate, String pVersion, String pMessage) throws Exception {
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
        try {
            notifyUpVersionToApp(pMessage.forceupdate, pMessage.version, pMessage.message);
        } catch (Exception e) {
            Log.d(this, e);
        }
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
