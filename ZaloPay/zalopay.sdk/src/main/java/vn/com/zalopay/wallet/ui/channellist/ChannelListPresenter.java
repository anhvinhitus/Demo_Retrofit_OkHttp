package vn.com.zalopay.wallet.ui.channellist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
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
import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.business.behavior.gateway.PlatformInfoLoader;
import vn.com.zalopay.wallet.business.channel.injector.AbstractChannelLoader;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.OrderState;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkDownloadResourceMessage;
import vn.com.zalopay.wallet.event.SdkInvalidDataMessage;
import vn.com.zalopay.wallet.event.SdkLoadingTaskMessage;
import vn.com.zalopay.wallet.event.SdkNetworkEvent;
import vn.com.zalopay.wallet.event.SdkResourceInitMessage;
import vn.com.zalopay.wallet.event.SdkSelectedChannelMessage;
import vn.com.zalopay.wallet.event.SdkStartInitResourceMessage;
import vn.com.zalopay.wallet.event.SdkSuccessTransEvent;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.ChannelHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.IAppInfo;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.pay.PayProxy;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.AbstractPresenter;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;

import static vn.com.zalopay.wallet.constants.Constants.CHANNEL_PAYMENT_REQUEST_CODE;
import static vn.com.zalopay.wallet.constants.Constants.MAP_POPUP_RESULT_CODE;
import static vn.com.zalopay.wallet.constants.Constants.SELECTED_PMC_POSITION;

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
    private PayProxy mPayProxy;
    private List<Object> mChannelList = new ArrayList<>();
    private AbstractChannelLoader mChannelLoader;
    private PaymentChannel mSelectChannel = null;
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
    private onCloseSnackBar mOnCloseSnackBarListener = new onCloseSnackBar() {
        @Override
        public void onClose() {
            try {
                getViewOrThrow().showOpenSettingNetwokingDialog(null);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    };

    public ChannelListPresenter() {
        SDKApplication.getApplicationComponent().inject(this);
        Log.d(this, "call constructor ChannelListPresenter");
    }

    public List<Object> getChannelList() {
        return mChannelList;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANNEL_PAYMENT_REQUEST_CODE) {
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
                        } else {
                            exitHasOneChannel();
                        }
                    }
                    break;
                case MAP_POPUP_RESULT_CODE:
                    selectChannelFromPopup(data);
                    break;
            }
        }
    }

    public boolean onBackPressed() {
        Log.d(this, "onBackPressed");
        if (mPayProxy == null) {
            return false;
        }
        @OrderState int orderState = mPayProxy.orderProcessing();
        switch (orderState) {
            case OrderState.SUBMIT:
            case OrderState.QUERY_STATUS:
                return true;
            default:
                return false;
        }
    }

    private void selectChannelFromPopup(Intent data) {
        if (data != null) {
            int position = data.getIntExtra(SELECTED_PMC_POSITION, -1);
            PaymentChannel channel = onSelectedChannel(position);
            if (channel != null) {
                //delay waiting for destroy popup
                new Handler().postDelayed(this::startPayment, 300);
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
        if (ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            PaymentSnackBar.getInstance().dismiss();
        } else {
            showNetworkOfflineSnackBar();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mChannelAdapter = null;
        mChannelList = null;
        if(mPayProxy != null){
            mPayProxy.release();
        }
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
        onSelectedChannel(pMessage.position);
    }

    public boolean networkOffline() {
        boolean offline = false;
        try {
            //check networking
            Activity activity = BaseActivity.getCurrentActivity();
            offline = activity != null && !ConnectionUtil.isOnline(activity);
            if (offline) {
                getViewOrThrow().showOpenSettingNetwokingDialog(null);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return offline;
    }


    private PaymentChannel onSelectedChannel(int pPosition) {
        Log.d(this, "select at position", pPosition);
        if (mChannelList == null || mChannelList.size() <= 0) {
            Log.d(this, "channel list is empty");
            return null;
        }
        PaymentChannel channel = null;
        if (pPosition >= 0) {
            Object object = mChannelList.get(pPosition);
            if (object instanceof PaymentChannel) {
                channel = (PaymentChannel) object;
            }
        }
        if (channel == null) {
            Log.d(this, "channel is null");
            return null;
        }
        if (!changedChannel(channel)) {
            Log.d(this, "click same channel");
            return null;
        }
        //check networking
        if (networkOffline()) {
            return null;
        }
        if (!mPayProxy.validate(channel)) {
            return null;
        }
        try {

            markSelectChannel(channel, pPosition);
            if (GlobalData.analyticsTrackerWrapper != null) {
                GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_ChoosePayMethod, ZPPaymentSteps.OrderStepResult_None, channel.pmcid);
            }
        } catch (Exception e) {
            Log.d(this, e);
        }
        return channel;
    }

    private boolean changedChannel(PaymentChannel selectChannel) {
        return mSelectChannel == null || mSelectChannel != selectChannel;
    }

    private void markSelectChannel(PaymentChannel channel, int position) throws Exception {
        if (channel == null) {
            Log.d(this, "channel is null");
            return;
        }
        if (!changedChannel(channel)) {
            Log.d(this, "click same channel");
            return;
        }
        //update total amount and fee
        double fee = channel.totalfee;
        double total_amount = mPaymentInfoHelper.getAmount() + fee;
        getViewOrThrow().renderTotalAmountAndFee(total_amount, fee);

        if (mSelectChannel != null) {
            mSelectChannel.select = false;
        }
        mSelectChannel = channel;
        mSelectChannel.select = true;
        mChannelAdapter.notifyBinderItemChanged(position);
        updateButton(channel);
    }

    public void startPayment() {
        try {
            if (mSelectChannel != null) {
                mPayProxy.setChannel(mSelectChannel).start();
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private void paymentInfoReady() {
        try {
            getViewOrThrow().setTitle(mPaymentInfoHelper.getTitleByTrans());
            getViewOrThrow().renderOrderInfo(mPaymentInfoHelper.getOrder());
            renderItemDetail();
            initAdapter();
            //init channel proxy
            mPayProxy = PayProxy.get()
                    .setChannelListPresenter(this)
                    .setPaymentInfo(mPaymentInfoHelper);
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

    private void initAdapter() throws Exception {
        mChannelAdapter = new ChannelListAdapter();
        Context context = getViewOrThrow().getContext();
        long amount = mPaymentInfoHelper.getAmount();
        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        int userLevel = mPaymentInfoHelper.getLevel();
        @TransactionType int transtype = mPaymentInfoHelper.getTranstype();
        mChannelAdapter.addZaloPayBinder(context, amount, userInfo, transtype);
        mChannelAdapter.addMapBinder(context, amount, userLevel);
        mChannelAdapter.addTitle();
        mChannelAdapter.addInputBinder(context, amount, userInfo, transtype);
        getViewOrThrow().onBindingChannel(mChannelAdapter);
    }

    private void renderItemDetail() throws Exception {
        List<NameValuePair> items = mPaymentInfoHelper.getOrder().parseItems();
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
        mChannelAdapter.add(itemType, pChannel);
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
        try {
            for (int position = 0; position < mChannelList.size(); position++) {
                Object object = mChannelList.get(position);
                if (object instanceof PaymentChannel) {
                    PaymentChannel paymentChannel = (PaymentChannel) object;
                    if (paymentChannel.meetPaymentCondition()) {
                        Log.d(this, "make default select channel", paymentChannel);
                        markSelectChannel(paymentChannel, position);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private void updateButton(PaymentChannel channel) throws Exception {
        if (channel == null) {
            getViewOrThrow().disableConfirmButton();
        } else {
            //update text by trans type
            int btnTextId = ChannelHelper.btnConfirmText(channel, mPaymentInfoHelper.getTranstype());
            int btnBgDrawableId = ChannelHelper.btnConfirmDrawable(channel);
            getViewOrThrow().enableConfirmButton(btnTextId, btnBgDrawableId);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnNetworkEvent(SdkNetworkEvent networkEvent) {
        Log.d(this, "networking is changed ", networkEvent.online);
        if (!networkEvent.online) {
            showNetworkOfflineSnackBar();
        } else {
            PaymentSnackBar.getInstance().dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessTransEvent(SdkSuccessTransEvent event) {
        if (mPayProxy != null) {
            try {
                mPayProxy.OnTransEvent(EEventType.ON_NOTIFY_TRANSACTION_FINISH, event);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    private void showNetworkOfflineSnackBar() {
        try {
            getViewOrThrow().showSnackBar(
                    GlobalData.getStringResource(RS.string.zpw_string_alert_networking_offline),
                    GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking),
                    TSnackbar.LENGTH_INDEFINITE, mOnCloseSnackBarListener);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }
}
