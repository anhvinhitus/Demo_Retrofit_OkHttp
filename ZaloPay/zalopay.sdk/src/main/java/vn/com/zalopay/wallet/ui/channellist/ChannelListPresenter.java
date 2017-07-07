package vn.com.zalopay.wallet.ui.channellist;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.injector.AbstractChannelLoader;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.OrderState;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkInvalidDataMessage;
import vn.com.zalopay.wallet.event.SdkNetworkEvent;
import vn.com.zalopay.wallet.event.SdkPaymentInfoReadyMessage;
import vn.com.zalopay.wallet.event.SdkSelectedChannelMessage;
import vn.com.zalopay.wallet.event.SdkSuccessTransEvent;
import vn.com.zalopay.wallet.helper.ChannelHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.IBankInteractor;
import vn.com.zalopay.wallet.interactor.VersionCallback;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.pay.PayProxy;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.PaymentPresenter;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;


/**
 * Created by chucvv on 6/12/17.
 */

public class ChannelListPresenter extends PaymentPresenter<ChannelListFragment> {
    @Inject
    public IBankInteractor mBankInteractor;
    protected PaymentInfoHelper mPaymentInfoHelper;
    @Inject
    EventBus mBus;

    private ChannelListAdapter mChannelAdapter;
    private PayProxy mPayProxy;
    private List<Object> mChannelList = new ArrayList<>();
    private Map<String, Object> mActiveMapChannels = new HashMap<>();
    private Map<String, Object> mInActiveMapChannels = new HashMap<>();
    private Map<String, Object> mCCMapChannel = new HashMap<>();
    private AbstractChannelLoader mChannelLoader;
    private PaymentChannel mSelectChannel = null;
    private boolean mSetInputMethodTitle = false;
    private int mLastSelectPosition = -1;
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
    private ZPMonitorEventTiming mEventTiming = SDKApplication.getApplicationComponent().monitorEventTiming();

    public ChannelListPresenter() {
        Timber.d("call constructor ChannelListPresenter");
        mPaymentInfoHelper = GlobalData.paymentInfoHelper;
        SDKApplication.getApplicationComponent().inject(this);
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_START_CHANNEL_LIST_PRESENTER);
    }

    private void loadAppInfoOnComplete(AppInfo appInfo) {
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
        if (mPaymentInfoHelper == null) {
            return true;
        }
        int status = mPaymentInfoHelper.getStatus();
        return status == PaymentStatus.DIRECT_LINKCARD
                || status == PaymentStatus.DIRECT_LINKCARD_AND_PAYMENT
                || status == PaymentStatus.DIRECT_LINK_ACCOUNT
                || status == PaymentStatus.DIRECT_LINK_ACCOUNT_AND_PAYMENT;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult resultCode %s", resultCode);
        if (requestCode != Constants.CHANNEL_PAYMENT_REQUEST_CODE) {
            return;
        }
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
                    OnActivityResultPaymentRequestCanceled(data);
                }
                break;
            case Constants.MAP_POPUP_RESULT_CODE:
                selectChannelFromPopup(data);
                break;
        }
    }

    private void OnActivityResultPaymentRequestCanceled(Intent data) {
        boolean showDialog = data.getBooleanExtra(Constants.SHOW_DIALOG, false);
        String message = data.getStringExtra(Constants.MESSAGE);
        if (!showDialog || TextUtils.isEmpty(message)) {
            exitHasOneChannel();
            return;
        }

        try {
            getViewOrThrow().showInfoDialog(message);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public String getQuitMessage() {
        if (mPaymentInfoHelper == null) {
            return null;
        }
        return mPaymentInfoHelper.getQuitMessByTrans(GlobalData.getAppContext());
    }

    public boolean onBackPressed() {
        Timber.d("onBackPressed");
        if (mPayProxy == null) {
            return false;
        }

        int orderState = mPayProxy.orderProcessing();
        return orderState == OrderState.SUBMIT || orderState == OrderState.QUERY_STATUS;
    }

    private void selectChannelFromPopup(Intent data) {
        if (data == null) {
            return;
        }

        int position = data.getIntExtra(Constants.SELECTED_PMC_POSITION, -1);
        PaymentChannel channel = onSelectedChannel(position);
        if (channel == null) {
            return;
        }
        //delay waiting for destroy popup
        new Handler().postDelayed(this::startPayment, 300);
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
            markSelectChannel(channel, pPosition);
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

        if (mLastSelectPosition != -1) {
            mChannelAdapter.notifyBinderItemChanged(mLastSelectPosition);
        }
        mLastSelectPosition = position;
        mChannelAdapter.notifyBinderItemChanged(position);
        updateButton(channel);
        trackingPaymentChannel(channel.pmcid);
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
            if (mPaymentInfoHelper == null) {
                callback();
                return;
            }
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

            //init channel proxy
            mPayProxy = PayProxy.shared().initialize((BaseActivity) getViewOrThrow().getActivity())
                    .setChannelListPresenter(this)
                    .setPaymentInfo(mPaymentInfoHelper);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    private void initAdapter() throws Exception {
        long amount = mPaymentInfoHelper.getAmount();
        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        int userLevel = mPaymentInfoHelper.getLevel();
        @TransactionType int transtype = mPaymentInfoHelper.getTranstype();
        mChannelAdapter = getViewOrThrow().initChannelListAdapter(amount, userInfo, userLevel, transtype);
    }

    private void renderItemDetail() throws Exception {
        List<NameValuePair> items = mPaymentInfoHelper.getOrder().parseItems();
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_RENDER_DYNAMICITEMDETAIL);
        getViewOrThrow().renderDynamicItemDetail(items);
    }

    void send(PaymentChannel pChannel) {
        ChannelListAdapter.ItemType itemType;
        if (pChannel.isZaloPayChannel()) {
            itemType = ChannelListAdapter.ItemType.ZALOPAY;
        } else if (pChannel.isMapCardChannel() || pChannel.isBankAccountMap()) {
            itemType = ChannelListAdapter.ItemType.MAP;
        } else {
            itemType = ChannelListAdapter.ItemType.INPUT;
            if (!mSetInputMethodTitle) {
                mChannelAdapter.setTitle(mPaymentInfoHelper.getPaymentMethodTitleByTrans());
                mSetInputMethodTitle = true;
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
        String[] pmcNameList = pmcNames.split(Constants.COMMA);
        if (pmcNameList.length <= 0) {
            return;
        }

        for (String name : pmcNameList) {
            if (TextUtils.isEmpty(name)) {
                continue;
            }
            send((PaymentChannel) mCCMapChannel.get(name));
        }
    }

    private void renderMapChannels(Map<String, Object> channels, String... bankCodes) {
        for (String bankCode : bankCodes) {
            if (TextUtils.isEmpty(bankCode)) {
                continue;
            }
            if (BuildConfig.CC_CODE.equals(bankCode)) {
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
            String[] sortedBankCode = bankCodes.split(Constants.COMMA);
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
            /*
              this is withdraw link card and no mapp card.
              need remind user go to link card to can withdraw
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
            selectAndScrollToChannel(selectChannel, selectChannel.position);
            return;
        }
        boolean hasActiveChannel = false;
        int pos = -1;
        for (int position = 0; position < mChannelList.size(); position++) {
            Object object = mChannelList.get(position);
            if (!(object instanceof PaymentChannel)) {
                continue;
            }
            PaymentChannel channel = (PaymentChannel) object;
            if (!channel.meetPaymentCondition()) {
                continue;
            }
            hasActiveChannel = true;
            pos = position;
            selectChannel = channel;
            break;
        }
        if (selectChannel != null) {
            selectAndScrollToChannel(selectChannel, pos);
        }
        if (!hasActiveChannel) {
            getViewOrThrow().disableConfirmButton();
            getViewOrThrow().showSnackBar(GlobalData.getAppContext().getString(R.string.sdk_warning_no_channel), null,
                    Snackbar.LENGTH_INDEFINITE, null);
        }
    }

    private void selectAndScrollToChannel(PaymentChannel selectChannel, int position) throws Exception {
        markSelectChannel(selectChannel, position);
        getViewOrThrow().scrollToPos(position);
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
                Timber.d("load channels on complete");
                loadChannelOnCompleted();
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
    void queueChannel(PaymentChannel channel) {
        String key = channel.bankcode;
        Object object = channel;
        boolean active = channel.meetPaymentCondition();
        if (BuildConfig.CC_CODE.equals(key)) {
            Object value = active ? mActiveMapChannels.get(key) : mInActiveMapChannels.get(key);
            object = channel.pmcname;
            if (value != null) {
                StringBuilder valueBuilder = new StringBuilder();
                valueBuilder.append(object)
                        .append(Constants.COMMA)
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

    @Override
    protected void onProcessPaymentInfo(SdkPaymentInfoReadyMessage message) throws Exception {
        if (message == null) {
            callback();
            return;
        }
        if (message.mPlatformInfoCallback instanceof VersionCallback) {
            VersionCallback versionCallback = (VersionCallback) message.mPlatformInfoCallback;
            onProcessUpVersionMessage(versionCallback);
            if (versionCallback.forceupdate) {
                getViewOrThrow().terminate();
                return;
            }
        }
        if (message.mErrorType == SdkPaymentInfoReadyMessage.ErrorType.SUCCESS) {
            loadAppInfoOnComplete(message.mAppInfo);
            loadChannels();
            return;
        }
        Timber.d("payment info on error %s", message.mError.getMessage());
        String error = TransactionHelper.getMessage(message.mError);
        boolean showDialog = ErrorManager.shouldShowDialog(mPaymentInfoHelper.getStatus());
        if (showDialog) {
            getViewOrThrow().showError(error);
        } else {
            getViewOrThrow().callbackThenTerminate();
        }
    }

    public void setPaymentStatusAndCallback(@PaymentStatus int pStatus) {
        mPaymentInfoHelper.setResult(pStatus);
        if (pStatus == PaymentStatus.USER_CLOSE && GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.trackUserCancel();
        }
        callback();
    }

    @Override
    protected void callback() {
        Timber.d("callback");
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
        if (manualRelease()) {
            SingletonLifeCircleManager.disposeAll();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvalidData(SdkInvalidDataMessage eventMessge) {
        try {
            mPaymentInfoHelper.setResult(PaymentStatus.INVALID_DATA);
            getViewOrThrow().showError(eventMessge.message);
        } catch (Exception e) {
            Timber.w(e, "onInvalidData on error");
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

    private void trackingPaymentChannel(int pmcid) {
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper
                    .step(ZPPaymentSteps.OrderStep_ChoosePayMethod)
                    .pmcId(pmcid)
                    .track();
        }
        Timber.d("track channel id %s", pmcid);
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
