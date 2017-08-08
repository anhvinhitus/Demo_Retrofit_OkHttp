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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.entity.MultiValueMap;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.OrderState;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkNetworkEvent;
import vn.com.zalopay.wallet.event.SdkPaymentInfoReadyMessage;
import vn.com.zalopay.wallet.event.SdkSelectedChannelMessage;
import vn.com.zalopay.wallet.event.SdkSuccessTransEvent;
import vn.com.zalopay.wallet.helper.BankHelper;
import vn.com.zalopay.wallet.helper.ChannelHelper;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.helper.TrackHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.ChannelListInteractor;
import vn.com.zalopay.wallet.interactor.VersionCallback;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.pay.PayProxy;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.repository.bank.BankStore;
import vn.com.zalopay.wallet.repository.voucher.VoucherStore;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.PaymentPresenter;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;
import vn.com.zalopay.wallet.workflow.channelloader.AbstractChannelLoader;


/*
 * Created by chucvv on 6/12/17.
 */

public class ChannelListPresenter extends PaymentPresenter<ChannelListFragment> {
    @Inject
    public BankStore.Interactor mBankInteractor;
    protected PaymentInfoHelper mPaymentInfoHelper;
    @Inject
    EventBus mBus;
    @Inject
    VoucherStore.Interactor mVoucherInteractor;
    @Inject
    Context mContext;

    private ChannelListAdapter mChannelAdapter;
    private PayProxy mPayProxy;
    private List<Object> mChannelList = new ArrayList<>();
    private MultiValueMap<String, Object> mActiveMapChannels = new MultiValueMap<>();
    private MultiValueMap<String, Object> mInActiveMapChannels = new MultiValueMap<>();
    private Map<String, Object> mActiveCCMapChannel = new HashMap<>();
    private Map<String, Object> mInActiveCCMapChannel = new HashMap<>();
    private AbstractChannelLoader mChannelLoader;
    private PaymentChannel mSelectChannel = null;
    private PaymentChannel mZaloPayChannel = null; //temp variable for checking active zalopay channel
    private int mLastSelectPosition = -1;
    private boolean mHasActiveChannel = false;
    private
    @TransactionType
    int tempTranstype;
    private AbstractOrder temOrder;
    private
    @PaymentStatus
    int tempPaymentStatus;
    private onCloseSnackBar mOnCloseSnackBarListener = () -> {
        try {
            getViewOrThrow().showOpenSettingNetwokingDialog(null);
        } catch (Exception e) {
            Timber.w(e);
        }
    };
    private ZPMonitorEventTiming mEventTiming = SDKApplication.getApplicationComponent().monitorEventTiming();

    public ChannelListPresenter() {
        Timber.d("call constructor ChannelListPresenter");
        mPaymentInfoHelper = GlobalData.getPaymentInfoHelper();
        SDKApplication.getApplicationComponent().inject(this);
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_START_CHANNEL_LIST_PRESENTER);
    }

    private void loadAppInfoOnComplete(AppInfo appInfo) {
        try {
            Timber.d("load app info success %s", GsonUtils.toJsonString(appInfo));
            if (appInfo == null || !appInfo.isAllow()) {
                getViewOrThrow().showAppInfoNotFoundDialog();
                return;
            }
            String appName = TransactionHelper.getAppNameByTranstype(mContext, mPaymentInfoHelper.getTranstype());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult resultCode %s", resultCode);
        if (requestCode == Constants.BANK_SELECT_REQUEST_CODE) {
            onStartLinkThenPay(data);
            return;
        }
        if (resultCode == Constants.LINK_ACCOUNT_RESULT_CODE) {
            onStartLinkThenPay(data);
            return;
        }
        //restore the previous info
        if (temOrder != null) {
            Timber.d("restore payment values for payment info");
            mPaymentInfoHelper.setOrder(temOrder);
            mPaymentInfoHelper.setTranstype(tempTranstype);
            mPaymentInfoHelper.setLinkAccountInfo(null);
            mPaymentInfoHelper.setMapCardResult(null);
            mPaymentInfoHelper.setCardTypeLink(null);
            temOrder = null;
            //reload channels list to continue payment if user link success
            if (resultCode == Activity.RESULT_OK
                    && mPaymentInfoHelper.getStatus() == PaymentStatus.SUCCESS) {
                try {
                    mChannelList.clear();
                    mChannelAdapter.clearDataset();
                    loadChannels();
                } catch (Exception e) {
                    Timber.w(e, "Exception reload channel after link success");
                }
            }
            mPaymentInfoHelper.setResult(tempPaymentStatus);
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

    private void onStartLinkThenPay(Intent data) {
        try {
            if (data == null) {
                return;
            }
            String bankCode = data.getStringExtra(Constants.BANKLINK_TYPE_EXTRA);
            Timber.d("onStartLinkThenPay flow %s", bankCode);
            boolean isBankAccount = BankHelper.isBankAccount(bankCode);
            if (mPaymentInfoHelper == null) {
                getViewOrThrow().showError(mContext.getResources().getString(R.string.sdk_error_paymentinfo_empty));
                return;
            }
            //backup data and fake data for link type
            temOrder = mPaymentInfoHelper.takeOrder();
            tempTranstype = mPaymentInfoHelper.getTranstype();
            tempPaymentStatus = mPaymentInfoHelper.getStatus();

            GlobalData.updatePaymentInfo(isBankAccount);
            mPaymentInfoHelper.setMapBank(null);
            mPaymentInfoHelper.setCardTypeLink(bankCode);

            ChannelListInteractor interactor = SDKApplication.getApplicationComponent().channelListInteractor();
            interactor.collectPaymentInfo(mPaymentInfoHelper);

            Intent intent = getChannelIntent();
            int layoutId = isBankAccount ? R.layout.screen__link__acc : R.layout.screen__card;
            intent.putExtra(Constants.CHANNEL_CONST.layout, layoutId);
            getViewOrThrow().startActivityForResult(intent, Constants.CHANNEL_PAYMENT_REQUEST_CODE);
        } catch (Exception e) {
            Timber.w(e, "Exception onStartLinkThenPay");
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

    String getQuitMessage() {
        if (mPaymentInfoHelper == null) {
            return null;
        }
        return mPaymentInfoHelper.getQuitMessByTrans(mContext);
    }

    boolean quitWithoutConfirm() {
        return !mHasActiveChannel;
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
        boolean started = onSelectedChannel(position);
        if (!started) {
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
        if (ConnectionUtil.isOnline(mContext)) {
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

    private boolean onSelectedChannel(int pPosition) {
        Timber.d("select at position %s", pPosition);
        if (mChannelList == null || mChannelList.size() <= 0) {
            Timber.d("channel list is empty");
            return false;
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
            return false;
        }
        if (!changedChannel(channel)) {
            Timber.d("click same channel");
            return false;
        }
        //check networking
        if (networkOffline()) {
            return false;
        }
        if (channel.isLinkChannel()) {
            startBankSelection();
            return true;
        }
        if (!mPayProxy.validate(channel)) {
            return false;
        }
        try {
            markSelectChannel(channel, pPosition);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        return true;
    }

    private boolean changedChannel(PaymentChannel selectChannel) {
        return mSelectChannel == null || mSelectChannel != selectChannel;
    }

    private void markSelectChannel(PaymentChannel channel, int position) throws Exception {
        if (channel == null || mPaymentInfoHelper == null) {
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
        getViewOrThrow().renderOrderAmount(total_amount);
        getViewOrThrow().renderOrderFee(fee);

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
            if (mSelectChannel == null || mPayProxy == null) {
                return;
            }
            mPayProxy
                    .setChannel(mSelectChannel)
                    .start();
            TrackHelper.trackEventConfirm(mPaymentInfoHelper);
        } catch (Exception e) {
            Timber.w(e, "Exception start payment");
        }
    }

    public void startDefaultPayment() {
        try {
            if (mSelectChannel == null || mPayProxy == null) {
                return;
            }
            mPayProxy
                    .setChannel(mSelectChannel)
                    .startDefault();
        } catch (Exception e) {
            Timber.w(e, "Exception start default payment");
        }
    }

    public void startBankSelection() {
        try {
            Intent bankSelectIntent = new Intent(BuildConfig.BANK_SELECT_ACTION);
            getViewOrThrow().startActivityForResult(bankSelectIntent, Constants.BANK_SELECT_REQUEST_CODE);
        } catch (Exception e) {
            Timber.d(e, "Exception start default payment");
        }
    }

    void onUseVouchComplete(VoucherInfo voucherInfo) {
        try {
            Timber.d("response use voucher %s", GsonUtils.toJsonString(voucherInfo));
            getViewOrThrow().hideVoucherCodePopup();
            if (voucherInfo == null || mPaymentInfoHelper == null) {
                return;
            }
            mPaymentInfoHelper.setVoucher(voucherInfo);
            //re calculate order amount
            double total_amount = mPaymentInfoHelper.getAmountTotal();
            if (total_amount <= 0 || voucherInfo.discountamount <= 0) {
                return;
            }
            double paymentAmount = total_amount - voucherInfo.discountamount;
            getViewOrThrow().renderOrderAmount(paymentAmount);
            getViewOrThrow().renderActiveVoucher(voucherInfo.vouchercode, total_amount, voucherInfo.discountamount);
            //save voucher to cache
            String userId = mPaymentInfoHelper.getUserId();
            if (!TextUtils.isEmpty(userId)) {
                mVoucherInteractor.put(userId, voucherInfo);
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void clearVoucher() {
        if (mPaymentInfoHelper == null) {
            return;
        }
        try {
            mPaymentInfoHelper.setVoucher(null);
            getViewOrThrow().renderOrderAmount(mPaymentInfoHelper.getAmountTotal());
        } catch (Exception e) {
            Timber.w(e);
        }

    }

    public void useVoucher(String voucherCode) {
        if (mPaymentInfoHelper == null || mPaymentInfoHelper.getUserInfo() == null) {
            return;
        }
        if (TextUtils.isEmpty(voucherCode)) {
            return;
        }
        if (!ConnectionUtil.isOnline(mContext)) {
            try {
                getViewOrThrow().setVoucherError(mContext.getString(R.string.sdk_error_networking_generic));
            } catch (Exception e) {
                Timber.d(e);
            }
            return;
        }
        String upperVoucherCode = voucherCode.toUpperCase();
        String userId = mPaymentInfoHelper.getUserId();
        String accessToken = mPaymentInfoHelper.getUserInfo().accesstoken;
        String appTrans = mPaymentInfoHelper.getAppTransId();
        long appId = mPaymentInfoHelper.getAppId();
        long amount = mPaymentInfoHelper.getAmount();
        long time = System.currentTimeMillis();
        Subscription subscription = mVoucherInteractor
                .validateVoucher(userId, accessToken, appTrans, appId, amount, time, upperVoucherCode)
                .map(voucherInfo -> {
                    if (voucherInfo != null) {
                        voucherInfo.vouchercode = upperVoucherCode;
                    }
                    return voucherInfo;
                })
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(this::onUseVouchComplete, throwable -> {
                    try {
                        Timber.d(throwable, "Exception use voucher %s", upperVoucherCode);
                        String error = TransactionHelper.getMessage(mContext, throwable);
                        getViewOrThrow().setVoucherError(error);
                    } catch (Exception e) {
                        Timber.d(e);
                    }
                });
        if (mSubscription != null) {
            mSubscription.add(subscription);
        }
    }

    public void onPaymentReady() {
        try {
            mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_ON_PAYMENT_READY);
            if (mPaymentInfoHelper == null) {
                mPaymentInfoHelper = GlobalData.getPaymentInfoHelper();
            }
            if (mPaymentInfoHelper == null) {
                callback();
                getViewOrThrow().terminate();
                return;
            }
            startSubscribePaymentReadyMessage();
            initAdapter();
            getViewOrThrow().setTitle(mPaymentInfoHelper.getTitleByTrans(mContext));
            if (PaymentPermission.allowVoucher()) {
                getViewOrThrow().renderVoucher();
            }
            mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_RENDER_ORDERINFO);
            getViewOrThrow().renderOrderInfo(mPaymentInfoHelper.getOrder());
            renderItemDetail();
            //init channel proxy
            mPayProxy = PayProxy.shared().initialize((BaseActivity) getViewOrThrow().getActivity())
                    .setChannelListPresenter(this)
                    .setPaymentInfo(mPaymentInfoHelper);
        } catch (Exception e) {
            Timber.d(e);
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

    void addToAdapter(PaymentChannel pChannel) {
        ChannelListAdapter.ItemType itemType;
        if (pChannel.isZaloPayChannel()) {
            itemType = ChannelListAdapter.ItemType.ZALOPAY;
            mZaloPayChannel = pChannel;
        } else if (pChannel.isMapCardChannel() || pChannel.isBankAccountMap()) {
            itemType = ChannelListAdapter.ItemType.MAP;
        } else {
            itemType = ChannelListAdapter.ItemType.INPUT;
        }
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_ADD_PAYMENTCHANNEL);
        mChannelAdapter.add(itemType, pChannel);
    }

    private void traverseListAndAddToAdapter(MultiValueMap<String, Object> pMap) {
        if (pMap == null || pMap.size() <= 0) {
            return;
        }
        for (String key : pMap.keySet()) {
            try {
                List<PaymentChannel> channelList = ChannelHelper.getChannels(key, pMap);
                if (channelList == null || channelList.size() <= 0) {
                    continue;
                }
                for (PaymentChannel channel : channelList) {
                    addToAdapter(channel);
                }
            } catch (Exception e) {
                Timber.w(e);
            }
        }
    }

    private void addAllMapChannelListIntoAdapter() {
        if (mActiveCCMapChannel.size() > 0) {
            for (Map.Entry<String, Object> channel : mActiveCCMapChannel.entrySet()) {
                addToAdapter((PaymentChannel) channel.getValue());
            }
        }
        traverseListAndAddToAdapter(mActiveMapChannels);
        if (mInActiveCCMapChannel.size() > 0) {
            for (Map.Entry<String, Object> channel : mInActiveCCMapChannel.entrySet()) {
                addToAdapter((PaymentChannel) channel.getValue());
            }
        }
        traverseListAndAddToAdapter(mInActiveMapChannels);
    }

    private void renderCC(boolean active, String pmcNames) {
        if (TextUtils.isEmpty(pmcNames)) {
            return;
        }
        String[] pmcNameList = pmcNames.split(Constants.COMMA);
        if (pmcNameList.length <= 0) {
            return;
        }
        Map<String, Object> channelList = active ? mActiveCCMapChannel : mInActiveCCMapChannel;
        if (channelList == null || channelList.size() <= 0) {
            return;
        }
        for (String name : pmcNameList) {
            if (TextUtils.isEmpty(name)) {
                continue;
            }
            Object channel = channelList.get(name);
            if (channel != null) {
                addToAdapter((PaymentChannel) channel);
            }
        }
    }

    private void addMapChannelIntoAdapter(boolean active, String... bankCodes) {
        MultiValueMap<String, Object> mapChannels = active ? mActiveMapChannels : mInActiveMapChannels;
        if (mapChannels == null || mapChannels.size() <= 0) {
            return;
        }
        for (String bankCode : bankCodes) {
            if (TextUtils.isEmpty(bankCode)) {
                continue;
            }
            if (BuildConfig.CC_CODE.equals(bankCode)) {
                renderCC(active, (String) mapChannels.get(bankCode));
                continue;
            }
            try {
                List<PaymentChannel> channelList = ChannelHelper.getChannels(bankCode, mapChannels);
                if (channelList == null) {
                    continue;
                }
                for (PaymentChannel channel : channelList) {
                    addToAdapter(channel);
                }
            } catch (Exception e) {
                Timber.w(e);
            }
        }
    }

    private void clearObjects() {
        //release variable after using
        mActiveMapChannels.clear();
        mInActiveMapChannels.clear();
        mActiveCCMapChannel.clear();
        mInActiveCCMapChannel.clear();
        mChannelLoader = null;
        mZaloPayChannel = null;
    }

    private Observable<Boolean> sortChannelsThenAddIntoAdapter(String bankCodes) {
        return Observable.defer(() -> {
            String[] sortedBankCode = bankCodes.split(Constants.COMMA);
            addMapChannelIntoAdapter(true, sortedBankCode);
            addMapChannelIntoAdapter(false, sortedBankCode);
            return Observable.just(true);
        });

    }

    private void collectChannelsToList() {
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.ZALOPAY));
        mChannelList.addAll(mChannelAdapter.getDataSet(ChannelListAdapter.ItemType.MAP));
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
                    alertMessage = mContext.getResources().getString(R.string.sdk_no_channel_warning_mess);
                }
                try {
                    getViewOrThrow().showError(alertMessage);
                } catch (Exception e) {
                    Timber.d(e);
                }
            }
        } else {
            try {
                makeFullLineOnLastItem();
                makeDefaultChannel();
            } catch (Exception e) {
                Timber.w(e.getMessage());
            }
        }
        clearObjects();
        GlobalData.revertVouchersOnStorage(mSubscription);
    }

    void loadChannelOnCompleted() {
        String sortedBankCodes = mBankInteractor.getBankCodeList();
        if (TextUtils.isEmpty(sortedBankCodes)) {
            addAllMapChannelListIntoAdapter();
            collectChannelsToList();
            loadChannelOnDoLast();
        } else {
            sortChannelsThenAddIntoAdapter(sortedBankCodes)
                    .doOnNext(aBoolean -> collectChannelsToList())
                    .subscribe(aBoolean -> loadChannelOnDoLast(), throwable -> {
                        try {
                            getViewOrThrow().showError(mContext.getResources().getString(R.string.zpw_string_error_layout));
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

    /*
     * auto show payment password when
     * has only 1 active map channel or zalopay channel
     */
    private boolean shouldAutoPayment() {
        if (mSelectChannel == null) {
            return false;
        }
        if (mActiveMapChannels == null || mActiveCCMapChannel == null) {
            return false;
        }
        boolean hasZaloPayActive = mZaloPayChannel != null && mZaloPayChannel.meetPaymentCondition();
        int channelActiveCount = mActiveMapChannels.size() + mActiveCCMapChannel.size() + (hasZaloPayActive ? 1 : 0);
        return channelActiveCount == 1;
    }

    private boolean selectLastPaymentChannel() throws Exception {
        PaymentChannel selectChannel = getLastPaymentChannel();
        if (selectChannel != null) {
            selectAndScrollToChannel(selectChannel, selectChannel.position);
        }
        return selectChannel != null;
    }

    private void makeFullLineOnLastItem() {
        if (mChannelList == null || mChannelList.size() <= 0) {
            return;
        }
        try {
            int lastIndex = mChannelList.size() - 1;
            PaymentChannel lastChannel = (PaymentChannel) mChannelList.get(lastIndex);
            lastChannel.fullLine = true;
            mChannelAdapter.notifyBinderItemChanged(lastIndex);
        } catch (Exception e) {
            Timber.d(e, "Exception make full line item");
        }
    }

    private void makeDefaultChannel() throws Exception {
        //auto select recently payment or link bank
        boolean hasLastPaymentChannel = selectLastPaymentChannel();
        if (hasLastPaymentChannel) {
            if (shouldAutoPayment()) {
                startDefaultPayment();
            }
            return;
        }

        PaymentChannel selectChannel = null;
        boolean hasLinkChannel = false;
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
            if (channel.isLinkChannel()) {
                hasLinkChannel = true;
                continue;
            }
            mHasActiveChannel = true;
            pos = position;
            selectChannel = channel;
            break;
        }
        if (selectChannel != null) {
            selectAndScrollToChannel(selectChannel, pos);
        }
        if (shouldAutoPayment()) {
            startDefaultPayment();
        }
        if (!mHasActiveChannel && !hasLinkChannel) {
            getViewOrThrow().disableConfirmButton();
            showSnackBarOnError();
        }
    }

    private void showSnackBarOnError() throws Exception {
        if (mPaymentInfoHelper == null) {
            return;
        }
        if (mPaymentInfoHelper.getBalance() > mPaymentInfoHelper.getAmountTotal()) {
            getViewOrThrow().showSnackBar(mContext.getResources().getString(R.string.sdk_warning_no_channel), null,
                    Snackbar.LENGTH_INDEFINITE, null);
        } else {
            getViewOrThrow().showSnackBar(mContext.getResources().getString(R.string.sdk_warning_no_channel_balance_error),
                    mContext.getResources().getString(R.string.sdk_hyperlink_charge_more),
                    Snackbar.LENGTH_INDEFINITE, () -> {
                        mPaymentInfoHelper.setResult(PaymentStatus.ERROR_BALANCE);
                        try {
                            getViewOrThrow().callbackThenTerminate();
                        } catch (Exception e) {
                            Timber.w(e);
                        }
                    });
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
            getViewOrThrow().enablePaymentButton(btnTextId, btnBgDrawableId);
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
                    getViewOrThrow().showError(mContext.getResources().getString(R.string.sdk_error_init_data));
                } catch (Exception e1) {
                    Timber.d(e);
                }
            }

            @Override
            public void onNext(PaymentChannel channel) {
                if (channel == null) {
                    return;
                }
                Timber.d("load channel on next %s", GsonUtils.toJsonString(channel));
                if (mPaymentInfoHelper.shouldIgnore(channel.pmcid)) {
                    Timber.d("this channel is not in filter list");
                    return;
                }
                if (!TextUtils.isEmpty(channel.bankcode) && channel.isMapValid()) {
                    enqueueChannel(channel);
                } else {
                    addToAdapter(channel);
                }
            }
        };
    }

    /***
     * visa/mastercard has same bankcode 123PCC
     * need another storage for him
     * then key is pmc name instead of bank code
     */
    void enqueueChannel(PaymentChannel channel) {
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
            if (active) {
                mActiveCCMapChannel.put(channel.pmcname, channel);
            } else {
                mInActiveCCMapChannel.put(channel.pmcname, channel);
            }
        }
        putChannel(active, key, object);
    }

    private void putChannel(boolean active, String key, Object object) {
        MultiValueMap<String, Object> map = active ? mActiveMapChannels : mInActiveMapChannels;
        if (map == null) {
            return;
        }
        try {
            map.put(key, object);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    private void loadChannels() throws Exception {
        try {
            Timber.d("preparing channels");
            if (mPaymentInfoHelper == null) {
                getViewOrThrow().showError(mContext.getResources().getString(R.string.sdk_error_paymentinfo_empty));
                return;
            }
            mChannelLoader = AbstractChannelLoader.createChannelInjector(mPaymentInfoHelper.getAppId(),
                    mPaymentInfoHelper.getUserId(), mPaymentInfoHelper.getAmount(), mPaymentInfoHelper.getBalance(),
                    mPaymentInfoHelper.getTranstype());
            mChannelLoader.source.subscribe(getChannelObserver());
            mChannelLoader.getChannels();
        } catch (Exception e) {
            Timber.w(e, "Exception load channels");
            getViewOrThrow().showError(mContext.getResources().getString(R.string.sdk_error_init_data));
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
        String error = TransactionHelper.getMessage(mContext, message.mError);
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
        TrackHelper.trackEventBack(mPaymentInfoHelper);
    }

    @Override
    protected void callback() {
        Timber.d("callback");
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
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
                Timber.w(e);
            }
        }
    }

    private void showNetworkOfflineSnackBar() {
        try {
            getViewOrThrow().showSnackBar(
                    mContext.getResources().getString(R.string.sdk_offline_networking_mess),
                    mContext.getResources().getString(R.string.sdk_turn_on_networking_mess),
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
    }

    public void showResultPayment(StatusResponse pResponse) throws Exception {
        getViewOrThrow().switchToResultScreen(pResponse);
    }
}
