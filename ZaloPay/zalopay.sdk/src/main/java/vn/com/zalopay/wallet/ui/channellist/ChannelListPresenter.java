package vn.com.zalopay.wallet.ui.channellist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.injector.AbstractChannelLoader;
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
import vn.com.zalopay.wallet.event.SdkNetworkEvent;
import vn.com.zalopay.wallet.event.SdkSelectedChannelMessage;
import vn.com.zalopay.wallet.event.SdkSuccessTransEvent;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.ChannelHelper;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.ChannelListInteractor;
import vn.com.zalopay.wallet.interactor.IAppInfo;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.pay.PayProxy;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.PaymentPresenter;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;

import static vn.com.zalopay.wallet.BuildConfig.CC_CODE;
import static vn.com.zalopay.wallet.constants.Constants.CHANNEL_PAYMENT_REQUEST_CODE;
import static vn.com.zalopay.wallet.constants.Constants.COMMA;
import static vn.com.zalopay.wallet.constants.Constants.MAP_POPUP_RESULT_CODE;
import static vn.com.zalopay.wallet.constants.Constants.SELECTED_PMC_POSITION;
import static vn.com.zalopay.wallet.constants.PaymentStatus.DIRECT_LINKCARD;
import static vn.com.zalopay.wallet.constants.PaymentStatus.DIRECT_LINKCARD_AND_PAYMENT;

/**
 * Created by chucvv on 6/12/17.
 */

public class ChannelListPresenter extends PaymentPresenter<ChannelListFragment> {
    @Inject
    public EventBus mBus;
    @Inject
    public IBank mBankInteractor;
    @Inject
    public IAppInfo mAppInfoInteractor;
    protected PaymentInfoHelper mPaymentInfoHelper;
    private ChannelListAdapter mChannelAdapter;
    private PayProxy mPayProxy;
    private List<Object> mChannelList = new ArrayList<>();
    private Map<String, Object> mActiveMapChannels = new HashMap<>();
    private Map<String, Object> mInActiveMapChannels = new HashMap<>();
    private Map<String, Object> mCCMapChannel = new HashMap<>();
    private AbstractChannelLoader mChannelLoader;
    private PaymentChannel mSelectChannel = null;
    private boolean setInputMethodTitle = false;
    private int lastSelectPos = -1;
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
        Timber.d("call constructor ChannelListPresenter");
        mPaymentInfoHelper = GlobalData.paymentInfoHelper;
        SDKApplication.getApplicationComponent().inject(this);
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_START_CHANNEL_LIST_PRESENTER);
    }

    @Override
    protected void loadBankListOnProgress() {
        try {
            mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_BANKLIST_START);
            getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    @Override
    protected void loadBankListOnError(Throwable throwable) {
        Log.d(this, "load bank list error", throwable);
        String message = TransactionHelper.getMessage(throwable);
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.zpw_alert_error_networking_when_load_banklist);
        }
        try {
            getViewOrThrow().showError(message);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    @Override
    protected void loadBankListOnComplete(BankConfigResponse bankConfigResponse) {
        try {
            SDKApplication.getApplicationComponent().monitorEventTiming().recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_BANKLIST_END);
            loadChannels();
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    protected void loadAppInfoOnError(Throwable throwable) {
        Log.d(this, "load app info on error", throwable);
        try {
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
                getViewOrThrow().hideLoading();
                getViewOrThrow().callbackThenTerminate();
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    protected void loadAppInfoOnComplete(AppInfo appInfo) {
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
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public List<Object> getChannelList() {
        return mChannelList;
    }

    @Override
    protected boolean manualRelease() {
        switch (mPaymentInfoHelper.getStatus()) {
            case DIRECT_LINKCARD:
            case DIRECT_LINKCARD_AND_PAYMENT:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANNEL_PAYMENT_REQUEST_CODE) {
            Timber.d("onActivityResult resultCode %s", resultCode);
            switch (resultCode) {
                case Activity.RESULT_OK:
                    try {
                        callback();
                        getViewOrThrow().terminate();
                    } catch (Exception e) {
                        Timber.w(e);
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
                                Timber.w(e);
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
        Timber.d("onBackPressed");
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
        Timber.d("onDetach - release channel adapter - payment proxy");
        mChannelAdapter = null;
        mChannelList = null;
        if (mPayProxy != null) {
            mPayProxy.release();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnSelectChannel(SdkSelectedChannelMessage pMessage) {
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
            Timber.d("channel list is empty");
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
            Timber.d("channel is null");
            return null;
        }
        if (!changedChannel(channel)) {
            Timber.d("click same channel");
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
            trackingPaymentChannel(channel.pmcid);
            markSelectChannel(channel, pPosition);
            if (GlobalData.analyticsTrackerWrapper != null) {
                GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_ChoosePayMethod, ZPPaymentSteps.OrderStepResult_None, channel.pmcid);
            }
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        return channel;
    }

    private boolean changedChannel(PaymentChannel selectChannel) {
        return mSelectChannel == null || mSelectChannel != selectChannel;
    }

    private void markSelectChannel(PaymentChannel channel, int position) throws Exception {
        if (channel == null) {
            Timber.d("channel is null");
            return;
        }
        if (!changedChannel(channel)) {
            Timber.d("click same channel");
            return;
        }
        //update total amount and fee
        double fee = channel.totalfee;
        double total_amount = mPaymentInfoHelper.getAmount() + fee;
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_RENDER_TOTALAMOUNTANDFEE);
        getViewOrThrow().renderTotalAmountAndFee(total_amount, fee);

        if (mSelectChannel != null) {
            mSelectChannel.select = false;
        }
        mSelectChannel = channel;
        mSelectChannel.select = true;

        if (lastSelectPos != -1) {
            mChannelAdapter.notifyBinderItemChanged(lastSelectPos);
        }
        lastSelectPos = position;
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

    public void onPaymentReady() {
        try {
            mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_ON_PAYMENT_READY);
            startSubscribePaymentReadyMessage();
            initAdapter();
            getViewOrThrow().setTitle(mPaymentInfoHelper.getTitleByTrans(GlobalData.getAppContext()));
            mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_RENDER_ORDERINFO);
            getViewOrThrow().renderOrderInfo(mPaymentInfoHelper.getOrder());
            renderItemDetail();
            //validate user level
            if (!mPaymentInfoHelper.userLevelValid()) {
                getViewOrThrow().showForceUpdateLevelDialog();
                return;
            }
            //check app info whether this transaction is allowed or not
            //getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_check_app_info));


            //init channel proxy
            mPayProxy = PayProxy.shared().initialize((BaseActivity) getViewOrThrow().getActivity())
                    .setChannelListPresenter(this)
                    .setPaymentInfo(mPaymentInfoHelper);
        } catch (Exception e) {
            Timber.d(e.getMessage());
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
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_RENDER_DYNAMICITEMDETAIL);
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
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_ADD_PAYMENTCHANNEL);
        mChannelAdapter.add(itemType, pChannel);
    }

    private void renderMapChannels() {
        if (mCCMapChannel.size() > 0) {
            for (Map.Entry<String, Object> channel : mCCMapChannel.entrySet()) {
                send((PaymentChannel) channel.getValue());
            }
        }
        if (mActiveMapChannels.size() > 0) {
            for (Map.Entry<String, Object> channel : mActiveMapChannels.entrySet()) {
                send((PaymentChannel) channel.getValue());
            }
        }
        if (mInActiveMapChannels.size() > 0) {
            for (Map.Entry<String, Object> channel : mInActiveMapChannels.entrySet()) {
                send((PaymentChannel) channel.getValue());
            }
        }
    }

    private void renderCC(String pmcNames) {
        if (TextUtils.isEmpty(pmcNames)) {
            return;
        }
        String[] pmcNameList = pmcNames.split(COMMA);
        if (pmcNameList.length > 0) {
            for (String name : pmcNameList) {
                if (TextUtils.isEmpty(name)) {
                    continue;
                }
                send((PaymentChannel) mCCMapChannel.get(name));
            }
        }
    }

    private void renderMapChannels(Map<String, Object> channels, String... bankCodes) {
        for (String bankCode : bankCodes) {
            if (TextUtils.isEmpty(bankCode)) {
                continue;
            }
            if (CC_CODE.equals(bankCode)) {
                renderCC((String) channels.get(bankCode));
                continue;
            }
            Object channel = channels.get(bankCode);
            if (channel instanceof PaymentChannel) {
                send((PaymentChannel) channel);
            }
        }
    }

    private void clearObjects() {
        mActiveMapChannels.clear();
        mActiveMapChannels = null;
        mInActiveMapChannels.clear();
        mInActiveMapChannels = null;
        mCCMapChannel.clear();
        mCCMapChannel = null;

        mChannelLoader = null;
    }

    private Observable<Boolean> sortChannels(String bankCodes) {
        return Observable.defer(() -> {
            String[] sortedBankCode = bankCodes.split(COMMA);
            renderMapChannels(mActiveMapChannels, sortedBankCode);
            renderMapChannels(mInActiveMapChannels, sortedBankCode);
            return Observable.just(true);
        });

    }

    private void collectChannelsToList() {
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.ZALOPAY));
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.MAP));
        if (mChannelAdapter.hasTitle()) {
            mChannelList.add(new Object());
        }
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.INPUT));
    }

    private void loadChannelOnDoLast() {
        // have no channel
        if (mChannelList.size() <= 0 || (mChannelList.size() == 1 && !(mChannelList.get(0) instanceof PaymentChannel))) {
            /***
             * this is withdraw link card and no mapp card.
             * need remind user go to link card to can withdraw
             */
            if (mPaymentInfoHelper.isWithDrawTrans()) {
                try {
                    getViewOrThrow().showWarningLinkCardBeforeWithdraw();
                } catch (Exception e) {
                    Timber.d(e);
                }
            } else {
                String alertMessage = mChannelLoader.getAlertAmount(mPaymentInfoHelper.getAmount());
                if (TextUtils.isEmpty(alertMessage)) {
                    alertMessage = GlobalData.getStringResource(RS.string.zpw_app_info_exclude_channel);
                }
                try {
                    getViewOrThrow().showError(alertMessage);
                } catch (Exception e) {
                    Timber.d(e);
                }
            }
        } else {
            try {
                makeDefaultChannel();
            } catch (Exception e) {
                Timber.w(e.getMessage());
            }
        }
        clearObjects();
    }

    private void loadChannelOnCompleted() {
        String sortedBankCodes = mBankInteractor.getBankCodeList();
        if (TextUtils.isEmpty(sortedBankCodes)) {
            renderMapChannels();
            collectChannelsToList();
            loadChannelOnDoLast();
        } else {
            sortChannels(sortedBankCodes)
                    .doOnNext(aBoolean -> collectChannelsToList())
                    .subscribe(aBoolean -> loadChannelOnDoLast(), throwable -> {
                        try {
                            getViewOrThrow().showError(GlobalData.getAppContext().getString(R.string.zpw_string_error_layout));
                        } catch (Exception e) {
                            Timber.w(e);
                        }
                    });
        }
    }

    private PaymentChannel getLastPaymentChannel() {
        String lastPaymentBank = mBankInteractor.getPaymentBank(mPaymentInfoHelper.getUserId());
        if (TextUtils.isEmpty(lastPaymentBank)) {
            return null;
        }
        PaymentChannel paymentChannel = null;
        for (int position = 0; position < mChannelList.size(); position++) {
            Object object = mChannelList.get(position);
            if (object instanceof PaymentChannel) {
                PaymentChannel channel = (PaymentChannel) object;
                if (!channel.meetPaymentCondition()) {
                    continue;
                }
                if (channel.isMapValid() && lastPaymentBank.equals(channel.cardKey())) {
                    paymentChannel = channel;
                    paymentChannel.position = position;
                    break;
                }
            }
        }
        return paymentChannel;
    }

    private void makeDefaultChannel() throws Exception {
        PaymentChannel selectChannel = getLastPaymentChannel();
        if (selectChannel != null) {
            markSelectChannel(selectChannel, selectChannel.position);
            getViewOrThrow().scrollToPos(selectChannel.position);
            return;
        }
        boolean hasActiveChannel = false;
        int pos = -1;
        for (int position = 0; position < mChannelList.size(); position++) {
            Object object = mChannelList.get(position);
            if (object instanceof PaymentChannel) {
                PaymentChannel channel = (PaymentChannel) object;
                if (!channel.meetPaymentCondition()) {
                    continue;
                }
                hasActiveChannel = true;
                pos = position;
                selectChannel = channel;
                break;
            }
        }
        if (selectChannel != null) {
            markSelectChannel(selectChannel, pos);
            getViewOrThrow().scrollToPos(pos);
        }
        if (!hasActiveChannel) {
            getViewOrThrow().disableConfirmButton();
            getViewOrThrow().showSnackBar(GlobalData.getAppContext().getString(R.string.sdk_warning_no_channel), null,
                    Snackbar.LENGTH_INDEFINITE, null);
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
            trackingPaymentChannel(channel.pmcid);
        }
    }

    private Observer<PaymentChannel> getChannelObserver() {
        return new Observer<PaymentChannel>() {
            @Override
            public void onCompleted() {
                Timber.d("load channels on complete");
                loadChannelOnCompleted();
                try {
                    getViewOrThrow().hideLoading();
                } catch (Exception e) {
                    Timber.d(e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.w("load channel on error %s", e);
                try {
                    getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zpw_alert_error_data));
                } catch (Exception e1) {
                    Timber.d(e);
                }
            }

            @Override
            public void onNext(PaymentChannel channel) {
                if (channel == null) {
                    return;
                }
                Timber.d("load channel on next %s", channel);
                if (mPaymentInfoHelper.shouldIgnore(channel.pmcid)) {
                    Timber.d("this channel is not in filter list");
                    return;
                }
                if (!TextUtils.isEmpty(channel.bankcode) && channel.isMapValid()) {
                    queueChannel(channel);
                } else {
                    send(channel);
                }
            }
        };
    }

    /***
     * visa/mastercard has same bankcode 123PCC
     * need another storage for him
     * then key is pmc name instead of bank code
     * @param channel
     */
    private void queueChannel(PaymentChannel channel) {
        String key = channel.bankcode;
        Object object = channel;
        boolean active = channel.meetPaymentCondition();
        if (CC_CODE.equals(key)) {
            Object value = active ? mActiveMapChannels.get(key) : mInActiveMapChannels.get(key);
            object = channel.pmcname;
            if (value != null) {
                StringBuilder valueBuilder = new StringBuilder();
                valueBuilder.append(object)
                        .append(COMMA)
                        .append(value.toString());
                object = valueBuilder.toString();
            }
            mCCMapChannel.put(channel.pmcname, channel);
        }
        if (active) {
            mActiveMapChannels.put(key, object);
        } else {
            mInActiveMapChannels.put(key, object);
        }
    }

    private synchronized void loadChannels() throws Exception {
        getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_process_view));
        try {
            Timber.d("preparing channels");
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

    private void readyForPayment() {
        loadBankList(mBankInteractor);
        Timber.d("ready for payment");
    }

    public boolean isUniqueChannel() {
        return mChannelList == null || mChannelList.size() <= 1;
    }

    public void exitHasOneChannel() {
        if (isUniqueChannel()) {
            try {
                getViewOrThrow().callbackThenTerminate();
            } catch (Exception e) {
                Timber.d(e.getMessage());
            }
        }
    }

    /***
     * load app info from cache or api
     */
    private void startSubscribePaymentReadyMessage() {
        Timber.d("start loading appinfo");
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_ON_SUBSCRIBE_START);
        ChannelListInteractor interactor = SDKApplication.getApplicationComponent().channelListInteractor();
        interactor.subscribeOnPaymentReady(message -> {
            try {
                mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_ON_SUBSCRIBE);
                loadAppInfoOnComplete(message.mAppInfo);
                loadChannels();
            } catch (Exception e) {
                Timber.d(e, "Exception when loading payment info");
            }
        });
    }

    /***
     * load app info from cache or api
     */
    private void loadAppInfo() {
        long appId = mPaymentInfoHelper.getAppId();
        @TransactionType int transtype = mPaymentInfoHelper.getTranstype();
        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        loadAppInfo(mAppInfoInteractor, appId, transtype, userInfo);
    }

    public void setPaymentStatusAndCallback(@PaymentStatus int pStatus) {
        mPaymentInfoHelper.setResult(pStatus);
        callback();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvalidData(SdkInvalidDataMessage eventMessge) {
        mPaymentInfoHelper.setResult(PaymentStatus.INVALID_DATA);
        try {
            getViewOrThrow().showError(eventMessge.message);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    @Override
    public void onPlatformError(Throwable e) {
        try {
            getViewOrThrow().showError(e.getMessage());
        } catch (Exception e1) {
            Timber.w(e);
        }
    }

    @Override
    public void onResourceError(Throwable throwable) {
        Timber.w("init resource error", throwable);
        /***
         * delete folder resource to download again.
         * this prevent case file resource downloaded but was damaged on the wire so
         * can not parse json file.
         */
        try {
            String resPath = platformInteractor.getUnzipPath();
            if (!TextUtils.isEmpty(resPath)) {
                StorageUtil.deleteRecursive(new File(resPath));
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        String message = throwable.getMessage();
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getAppContext().getString(R.string.sdk_alert_generic_init_resource);
        }
        try {
            getViewOrThrow().showError(message);
        } catch (Exception e1) {
            Timber.w(e1);
        }
    }

    @Override
    public void onResourceReady() {
        super.onResourceReady();
        try {
            loadChannels();
        } catch (Exception e) {
            setPaymentStatusAndCallback(PaymentStatus.FAILURE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadResource(SdkDownloadResourceMessage result) {
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_DOWNLOAD_RESOURCE_END);
        if (result.success) {
            Timber.d("download resource success - start init resource");
            onResourceInit();
        } else {
            try {
                getViewOrThrow().showError(result.message);
            } catch (Exception ignored) {
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnNetworkChanged(SdkNetworkEvent networkEvent) {
        Log.d(this, "networking is changed ", networkEvent.online);
        if (!networkEvent.online) {
            showNetworkOfflineSnackBar();
        } else {
            PaymentSnackBar.getInstance().dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessTrans(SdkSuccessTransEvent event) {
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

    @Override
    public void onUpdateVersion(SdkUpVersionMessage pMessage) {
        super.onUpdateVersion(pMessage);
        if (pMessage.forceupdate) {
            try {
                getViewOrThrow().terminate();
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    public String getQuitMessage() {
        return mPaymentInfoHelper.getQuitMessByTrans(GlobalData.getAppContext());
    }

    private void trackingPaymentChannel(int pmcid) {
        switch (pmcid) {
            case BuildConfig.channel_credit_card:
                ZPAnalytics.trackEvent(ZPEvents.USER_SELECT_PAYMENT_CHANNEL_36);
                break;
            case BuildConfig.channel_bankaccount:
                ZPAnalytics.trackEvent(ZPEvents.USER_SELECT_PAYMENT_CHANNEL_37);
                break;
            case BuildConfig.channel_zalopay:
                ZPAnalytics.trackEvent(ZPEvents.USER_SELECT_PAYMENT_CHANNEL_38);
                break;
            case BuildConfig.channel_atm:
                ZPAnalytics.trackEvent(ZPEvents.USER_SELECT_PAYMENT_CHANNEL_39);
                break;
        }
    }
}
