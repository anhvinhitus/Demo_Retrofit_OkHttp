package vn.com.zalopay.wallet.view.component.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.factory.AdapterFactory;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkNetworkEvent;
import vn.com.zalopay.wallet.event.SdkSmsMessage;
import vn.com.zalopay.wallet.event.SdkUnlockScreenMessage;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channellist.ChannelListActivity;
import vn.com.zalopay.wallet.ui.channellist.ChannelProxy;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;

import static vn.com.zalopay.wallet.constants.Constants.API;
import static vn.com.zalopay.wallet.constants.Constants.PMC_CONFIG;
import static vn.com.zalopay.wallet.constants.Constants.STATUS_RESPONSE;

public class PaymentChannelActivity extends BasePaymentActivity {
    protected CountDownTimer mExpireTransTimer;
    protected boolean mTimerRunning = false;
    private AdapterBase mAdapter = null;
    private boolean mIsStart = false;
    private boolean mIsSwitching = false;
    private ActivityRendering mActivityRender;
    private MiniPmcTransType mMiniPmcTransType;
    private StatusResponse mStatusResponse;
    /***
     * back pressed
     */
    private View.OnClickListener mOnClickExitListener = v -> {
        //get status again if user back when payment in bank's site
        if (getAdapter() != null && getAdapter().isCardFlowWeb() &&
                (getAdapter().isCCFlow() || (getAdapter().isATMFlow() && ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).isOtpWebProcessing()))) {
            confirmQuitOrGetStatus();
            return;
        }
        if (getAdapter() != null && getAdapter().isATMFlow() && getAdapter().isCanEditCardInfo()) {
            ((BankCardGuiProcessor) getAdapter().getGuiProcessor()).goBackInputCard();
            return;
        }
        if (getAdapter() != null && getAdapter().isZaloPayFlow() && getAdapter().isBalanceErrorPharse()) {
            mPaymentInfoHelper.setResult(PaymentStatus.FAILURE);
            setCallBack(Activity.RESULT_CANCELED);
            return;
        }
        if (getAdapter() != null && getAdapter().exitWithoutConfirm() && !isInProgress()) {
            if (getAdapter().isTransactionSuccess()) {
                mPaymentInfoHelper.setResult(PaymentStatus.SUCCESS);
                callBackThenTerminate();
            } else if (getAdapter().isTransactionFail()) {
                mPaymentInfoHelper.setResult(PaymentStatus.FAILURE);
                callBackThenTerminate();
            } else {
                setCallBack(Activity.RESULT_CANCELED);
            }
        } else {
            confirmQuitPayment();
        }
    };

    public void setCallBack(Intent pIntent) {
        Activity activity = BaseActivity.getCurrentActivity();
        if (activity instanceof ChannelListActivity) {
            setResult(Activity.RESULT_CANCELED, pIntent);
        } else if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
        finish();
    }

    private void setCallBack(int pResultCode) {
        Activity activity = BaseActivity.getCurrentActivity();
        if (activity instanceof ChannelListActivity) {
            setResult(pResultCode, new Intent());
        } else if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
        finish();
    }

    private void fillCardNumberFromCache() {
        String pCardNumber = null;
        try {
            pCardNumber = SharedPreferencesManager.getInstance().pickCachedCardNumber();
        } catch (Exception e) {
            Log.e(this, e);
        }
        if (!TextUtils.isEmpty(pCardNumber)) {
            getAdapter().getGuiProcessor().setCardInfo(pCardNumber);
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (mTimerRunning && !getAdapter().isFinalScreen()) {
            Log.d(this, "user tap on UI restart payment transaction countdown");
            startTransactionExpiredTimer();
        }
    }

    /***
     * transaction expired in 7minutes
     */
    protected void initTimer() {
        int iTimeToLiveTrans = BuildConfig.transaction_expire_time;
        iTimeToLiveTrans *= 60 * 1000;
        mExpireTransTimer = new CountDownTimer(iTimeToLiveTrans, 1000) {
            public void onTick(long millisUntilFinished) {
                mTimerRunning = true;
            }

            public void onFinish() {
                mTimerRunning = false;
                Log.d(this, "Timer is onFinish");
                if (getAdapter() != null && !getAdapter().isFinalScreen()) {
                    DialogManager.closeAllDialog();
                    getAdapter().showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_string_transaction_expired));
                    Log.d(this, "Moving to expired transaction screen because expiration");
                }
            }
        };
    }

    public void startPayment() {
        mAdapter = AdapterFactory.produce(this, mMiniPmcTransType, mPaymentInfoHelper, mStatusResponse);
        if (getAdapter() == null) {
            onExit(GlobalData.getStringResource(RS.string.sdk_config_invalid), true);
            return;
        }
        initTimer();
        renderActivity();
        //hide header if this is link card.
        if (mPaymentInfoHelper.isCardLinkTrans()) {
            visibleOrderInfo(false);
        } else {
            renderOrderInfo();
        }
        try {
            getAdapter().init();
        } catch (Exception e) {
            Log.d(this, e);
            return;
        }
        if (GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            renderResourceAfterDelay();
        }
    }

    @Override
    public void paymentInfoReady() {
        startPayment();
        prepareLink();
    }

    @Override
    protected void readyForPayment() {
        Log.d(this, "ready for payment");
        renderByResource();
        showProgress(false, null);
        /***
         * exception bidv bank
         * use can not pay by card with bidv so he/she need to link before payment.
         * auto fill again card number that he/she input before when direct to link channel
         */
        fillCardNumberFromCache();

        if (mAdapter instanceof AdapterLinkAcc) {
            ((AdapterLinkAcc) mAdapter).startFlow();
        }
    }

    @Override
    public void notifyUpVersionToApp(boolean pForceUpdate, String pVersion, String pMessage) {
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onUpVersion(pForceUpdate, pVersion, pMessage);
        }
        if (pForceUpdate) {
            callBackThenTerminate();
        }
    }

    @Override
    protected void showDialogAndExit(String pMessage, boolean pIsShow) {
        onExit(pMessage, pIsShow);
    }

    @Override
    protected void actionIfPreventApp() {
        onExit(GlobalData.getStringResource(RS.string.zpw_not_allow_payment_app), true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && getAdapter() != null) {
            getAdapter().onFinish();
            mAdapter = null;
        }
        if (DialogManager.isShowingProgressDialog()) {
            DialogManager.closeProcessDialog();
        }
        cancelTransactionExpiredTimer();
        System.gc();
        Log.d(this, "==== onDestroy ====");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (getIntent().getExtras() != null) {
            mMiniPmcTransType = getIntent().getExtras().getParcelable(PMC_CONFIG);
            mStatusResponse = getIntent().getExtras().getParcelable(STATUS_RESPONSE);
        }
        Log.d(this, "start payment channel", mMiniPmcTransType);
        if (mMiniPmcTransType == null) {
            onExit(GlobalData.getStringResource(RS.string.sdk_config_invalid), true);
            return;
        }
        mPaymentInfoHelper = ChannelProxy.get().getPaymentInfoHelper();
        if (mPaymentInfoHelper == null) {
            Log.d(this, "this channel not start from gateway channel list");
            return;
        }
        startPayment();
    }

    @Override
    public void onBackPressed() {
        if (getVisibilitySupportView()) {
            closeSupportView();
            return;
        }
        //user is summiting order
        if (processingOrder) {
            Log.d(this, "can not back, order still request api");
            return;
        }
        mOnClickExitListener.onClick(null);
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.trackUserCancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(this, "onStart");
        updateFontCardNumber();
    }

    protected void prepareLink() {
        //this is link account and the first call
        if (mPaymentInfoHelper.isBankAccountTrans()) {
            try {
                //check static resource whether ready or not
                loadStaticReload();
            } catch (Exception ex) {
                Log.e(this, ex);
                onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
            }
        }
        //this is link card channel and the first call.
        else if (mPaymentInfoHelper.isCardLinkTrans()) {
            //check profile level permission in table map
            try {
                UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
                int allowATM = userInfo.getPermissionByChannelMap(BuildConfig.channel_atm, TransactionType.LINK);
                int allowCC = userInfo.getPermissionByChannelMap(BuildConfig.channel_credit_card, TransactionType.LINK);
                if (allowATM == Constants.LEVELMAP_INVALID && allowCC == Constants.LEVELMAP_INVALID) {
                    onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
                    return;
                }
                if (allowATM == Constants.LEVELMAP_BAN && allowCC == Constants.LEVELMAP_BAN) {
                    getAdapter().confirmUpgradeLevel();
                    return;
                }
                isAllowLinkCardATM = (allowATM == Constants.LEVELMAP_ALLOW);
                isAllowLinkCardCC = (allowCC == Constants.LEVELMAP_ALLOW);
                //switch to cc adapter if link card just allow cc without atm
                if (!isAllowLinkCardATM && isAllowLinkCardCC && createChannelAdapter(BuildConfig.channel_credit_card)) {
                    initChannel();
                }
                long appId = BuildConfig.ZALOAPP_ID;
                loadAppInfo(appId, TransactionType.LINK, userInfo.zalopay_userid, userInfo.accesstoken);
            } catch (Exception ex) {
                Log.e(this, ex);
                onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showKeyBoardOnFocusingViewAgain();
        //capture networking change event on resume
        if (ConnectionUtil.isOnline(this)) {
            PaymentSnackBar.getInstance().dismiss();
            numberOfRetryOpenNetwoking = 0;
        } else if (!getAdapter().isFinalScreen()) {
            showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_offline),
                    GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking), TSnackbar.LENGTH_INDEFINITE, mOnCloseSnackBarListener);
        }
        Log.d(this, "onResume");
    }

    private void showKeyBoardOnFocusingViewAgain() {
        //focus on editting view again after user resume
        if (getAdapter() != null && (getAdapter().isCardFlow() || getAdapter().isLinkAccFlow())) {
            //auto show keyboard
            if (getAdapter().isInputStep() || getAdapter().shouldFocusAfterCloseQuitDialog()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getAdapter().getGuiProcessor().onFocusView();
                            if (!getAdapter().isLinkAccFlow()) {
                                getAdapter().getGuiProcessor().moveScrollViewToCurrentFocusView();//scroll to last view
                            }
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }, 300);
            } else {
                getAdapter().getGuiProcessor().moveScrollViewToCurrentFocusView();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        showKeyBoardOnFocusingViewAgain();
    }

    public void renderActivity() {
        String layoutResID = getAdapter().getLayoutID();
        if (TextUtils.isEmpty(layoutResID)) {
            onExit(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
            return;
        }
        setContentView(RS.getLayout(layoutResID));
        setMarginSubmitButtonTop(false);
        if (!GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            renderByResource();
        }
        setListener();
        getAdapter().setListener();
        applyFont();
    }

    private void renderOrderInfo() {
        double total_amount = 0, fee = 0;
        AbstractOrder order = mPaymentInfoHelper.getOrder();
        if (order != null) {
            total_amount = order.amount_total;
            fee = order.fee;
        }
        renderTotalAmountAndFee(total_amount, fee);
        String desc = null;
        if (order != null) {
            desc = order.description;
        }
        renderDesc(desc);
        //render app info
        String appName = TransactionHelper.getAppNameByTranstype(GlobalData.getAppContext(), mPaymentInfoHelper.getTranstype());
        if (TextUtils.isEmpty(appName)) {
            AppInfo appInfo = SDKApplication.getApplicationComponent()
                    .appInfoInteractor()
                    .get(mPaymentInfoHelper.getAppId());
            appName = appInfo != null ? appInfo.appname : null;
        }
        renderAppInfo(appName);
        renderItemDetail();
    }

    private void renderDesc(String pDesc) {
        //order desc
        boolean hasDesc = !TextUtils.isEmpty(pDesc);
        if (hasDesc) {
            setText(R.id.order_description_txt, pDesc);
        }
        setVisible(R.id.order_description_txt, hasDesc);
    }

    private void renderTotalAmountAndFee(double total_amount, double fee) {
        if (fee > 0) {
            String txtFee = StringUtil.formatVnCurrence(String.valueOf(fee));
            setText(R.id.order_fee_txt, txtFee);
        } else {
            setText(R.id.order_fee_txt, getResources().getString(R.string.sdk_order_fee_free));
        }
        //order amount
        boolean hasAmount = total_amount > 0;
        if (hasAmount) {
            String order_amount = StringUtil.formatVnCurrence(String.valueOf(total_amount));
            setText(R.id.order_amount_total_txt, order_amount);
        }
        setVisible(R.id.order_amount_total_linearlayout, hasAmount);
    }

    private void renderItemDetail() {
        List<NameValuePair> items = mPaymentInfoHelper.getOrder().parseItems();
        renderDynamicItemDetail(findViewById(R.id.orderinfo_module), items);
    }

    private void renderAppInfo(String appName) {
        boolean hasAppName = !TextUtils.isEmpty(appName);
        if (hasAppName) {
            setText(R.id.appname_txt, appName);
        }
        setVisible(R.id.appname_relativelayout, hasAppName);
    }

    private void setListener() {
        View exitView = findViewById(R.id.zpsdk_exit_ctl);
        if (exitView != null) {
            exitView.setOnClickListener(mOnClickExitListener);
        }
    }

    public void renderByResource() {
        try {
            renderByResource(null, null);
        } catch (Exception e) {
            Log.e(this, e);
            onExit(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
        }
    }

    public void renderByResource(DStaticViewGroup pAdditionStaticViewGroup, DDynamicViewGroup pAdditionDynamicViewGroup) {
        try {
            long time = System.currentTimeMillis();
            String pageName = getAdapter().getPageName();
            ResourceManager resourceManager = ResourceManager.getInstance(pageName);
            if (resourceManager != null) {
                mActivityRender = resourceManager.produceRendering(this);
                if (getActivityRender() != null) {
                    getActivityRender().render();
                    getActivityRender().render(pAdditionStaticViewGroup, pAdditionDynamicViewGroup);
                } else {
                    Log.d(this, "PaymentChannelActivity.render activityRendering is null");
                }
            } else {
                Log.d(this, "PaymentChannelActivity.render resourceManager is null");
            }
            Log.d(this, "PaymentChannelActivity.renderByResource: Total time:", (System.currentTimeMillis() - time));
        } catch (Exception e) {
            Log.e(this, e);
            onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error), true);
        }
    }

    public void enableSubmitBtn(boolean pIsEnabled) {
        Log.d(this, "enable button submit", pIsEnabled);
        setEnableButton(findViewById(R.id.zpsdk_btn_submit), pIsEnabled);
    }

    public ActivityRendering getActivityRender() {
        return mActivityRender;
    }

    public AdapterBase getAdapter() {
        return mAdapter;
    }

    public void setEnableButton(View pButtonView, boolean pIsEnabled) {
        if (pButtonView == null)
            return;
        pButtonView.setEnabled(pIsEnabled);
        if (getAdapter() != null && !getAdapter().isFinalScreen()) {
            pButtonView.setVisibility(View.VISIBLE);
        }
        if (pIsEnabled) {
            if (getAdapter() != null && getAdapter().isFinalStep())
                pButtonView.setBackgroundResource(RS.getDrawable(RS.drawable.zpw_bg_button_final));
            else
                pButtonView.setBackgroundResource(RS.getDrawable(RS.drawable.zpw_bg_button));
        } else {
            pButtonView.setBackgroundResource(RS.getDrawable(RS.drawable.zpw_bg_button_disable));
        }
    }

    protected void updateFontCardNumber() {
        new Handler().postDelayed(() -> applyFont(findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.zpw_font_medium)), 500);
    }

    public void startTransactionExpiredTimer() {
        if (mExpireTransTimer != null) {
            mExpireTransTimer.cancel();
            mExpireTransTimer.start();
        }
    }

    public void cancelTransactionExpiredTimer() {
        if (mExpireTransTimer != null) {
            mExpireTransTimer.cancel();
        }
    }

    /***
     * register event finish layout to render resource
     */
    private void renderResourceAfterDelay() {
        final View buttonWrapper = getAdapter().getActivity().findViewById(R.id.zpw_switch_card_button);
        if (buttonWrapper != null) {
            ViewTreeObserver vto = buttonWrapper.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver obs = buttonWrapper.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);
                    renderByResource();
                }
            });
        } else {
            Log.d(this, "reader resource after delaying 500ms");
            new Handler().postDelayed(this::renderByResource, 500);
        }
    }

    protected boolean createChannelAdapter(int pChannelId) {
        try {
            Log.d(this, "create new adapter pmc id = " + pChannelId);
            //release old adapter
            if (getAdapter() != null) {
                getAdapter().onFinish();
                mAdapter = null;
            }
            int transtype = TransactionType.LINK;
            MiniPmcTransType miniPmcTransType = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().
                    getPmcConfigByPmcID(BuildConfig.ZALOAPP_ID, transtype, pChannelId, null), MiniPmcTransType.class);
            if (miniPmcTransType != null) {
                mAdapter = AdapterFactory.produceChannelByPmc(this, miniPmcTransType, mPaymentInfoHelper, mStatusResponse);
                return true;
            }
        } catch (Exception e) {
            Log.e(this, e);
            onExit(GlobalData.getStringResource(RS.string.sdk_config_invalid), true);
        }
        return false;
    }

    protected void initChannel() {
        try {
            getAdapter().init();
        } catch (Exception e) {
            Log.d(this, e);
            return;
        }
        getAdapter().setListener();
        renderResourceAfterDelay();
        updateFontCardNumber();
    }

    public boolean isSwitching() {
        return mIsSwitching;
    }

    public void setIsSwitching(boolean pSwitching) {
        this.mIsSwitching = pSwitching;
    }

    /***
     * switch between atm and creditcard for linkcard channel
     *
     * @param pChannelID
     * @param pCardNumber
     */
    public synchronized void switchChannel(int pChannelID, final String pCardNumber) {
        if (getAdapter() != null && getAdapter().isATMFlow() && pChannelID == BuildConfig.channel_atm)
            return;
        if (getAdapter() != null && getAdapter().isCCFlow() && pChannelID == BuildConfig.channel_credit_card)
            return;
        //prevent user move to next if input existed card in link card
        CardGuiProcessor cardGuiProcessor = getAdapter().getGuiProcessor();
        if (cardGuiProcessor != null && cardGuiProcessor.preventNextIfLinkCardExisted()) {
            try {
                cardGuiProcessor.showHintError(cardGuiProcessor.getCardNumberView(), cardGuiProcessor.warningCardExist());
                return;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (!createChannelAdapter(pChannelID)) {
            return;
        }
        if (getAdapter() != null) {
            setIsSwitching(true);
            initChannel();
            if (getAdapter().isCardFlow()) {
                getAdapter().getGuiProcessor().setCardInfo(pCardNumber);
            }
        }
    }

    protected void confirmQuitOrGetStatus() {
        showConfirmDialogWithManyOption(GlobalData.getStringResource(RS.string.zpw_confirm_quit_loadsite), pIndex -> {
            switch (pIndex) {
                case 0:
                    break;
                case 1:
                    callBackThenTerminate();
                    break;
                case 2:
                    getAdapter().onEvent(EEventType.ON_BACK_WHEN_LOADSITE, new Object());
                    break;
            }
        }, GlobalData.getStringResource(RS.string.dialog_khong_button), GlobalData.getStringResource(RS.string.dialog_co_button), GlobalData.getStringResource(RS.string.dialog_getstatus_button));
    }

    protected void confirmQuitPayment() {
        String message = GlobalData.getStringResource(RS.string.zingpaysdk_confirm_quit);
        if (mPaymentInfoHelper.isPayTrans()) {
            message = GlobalData.getStringResource(RS.string.zingpaysdk_confirm_quit_rescan_qrcode);
        } else if (mPaymentInfoHelper.isBankAccountTrans()) {
            message = GlobalData.getStringResource(RS.string.sdk_confirm_quit_link_account);
        }
        showConfirmDialog(new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                if (getAdapter() != null && (getAdapter().isCardFlow() || getAdapter().isLinkAccFlow())) {
                    //focus on editting view again after user not quit
                    Log.d(this, "should focus again after close dialog " + getAdapter().shouldFocusAfterCloseQuitDialog());
                    if (getAdapter().shouldFocusAfterCloseQuitDialog()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    getAdapter().getGuiProcessor().onFocusView();
                                    if (!getAdapter().isLinkAccFlow()) {
                                        getAdapter().getGuiProcessor().moveScrollViewToCurrentFocusView();//scroll to last view
                                    }
                                } catch (Exception e) {
                                    Log.e(this, e);
                                }
                            }
                        }, 300);
                    } else {
                        getAdapter().getGuiProcessor().moveScrollViewToCurrentFocusView();
                    }
                }
            }

            @Override
            public void onOKevent() {
                mPaymentInfoHelper.setResult(PaymentStatus.FAILURE);
                callBackThenTerminate();
            }
        }, message, GlobalData.getStringResource(RS.string.dialog_co_button), GlobalData.getStringResource(RS.string.dialog_khong_button));

    }

    @Override
    public void callBackThenTerminate() {
        Log.d(this, "call back result and end sdk - status ", mPaymentInfoHelper.getStatus());
        Activity activity = BaseActivity.getCurrentActivity();
        if (activity instanceof ChannelListActivity) {
            setCallBack(Activity.RESULT_OK);
        } else if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
            finish();
        }
    }

    @Override
    protected String getCloseButtonText() {
        return GlobalData.getStringResource(RS.string.dialog_retry_input_card_button);
    }

    @Override
    protected void onCloseDialogSelection() {
        if (getAdapter() != null && getAdapter().getGuiProcessor() != null) {
            getAdapter().getGuiProcessor().clearCardNumberAndShowKeyBoard();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnUnLockScreenEvent(SdkUnlockScreenMessage pUnlockScreenEventMessage) {
        if (getAdapter() != null && mAdapter.isCardFlow()) {
            getAdapter().getGuiProcessor().moveScrollViewToCurrentFocusView();
        }
        mBus.removeStickyEvent(SdkUnlockScreenMessage.class);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnPaymentSmsEvent(SdkSmsMessage pSmsEventMessage) {
        String sender = pSmsEventMessage.sender;
        String body = pSmsEventMessage.message;
        if (!TextUtils.isEmpty(sender) && !TextUtils.isEmpty(body) && getAdapter() != null) {
            getAdapter().autoFillOtp(sender, body);
        }
        mBus.removeStickyEvent(SdkSmsMessage.class);
        Log.d(this, "on payment otp event " + GsonUtils.toJsonString(pSmsEventMessage));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnNetworkEvent(SdkNetworkEvent networkEvent) {
        //user sitting in the result screen
        if (getCurrentActivity() instanceof PaymentChannelActivity && ((PaymentChannelActivity) getCurrentActivity()).getAdapter().isFinalScreen()) {
            Log.d(this, "onNetworkMessageEvent user is on fail screen...");
            return;
        }
        //come from api request fail with handshake
        if (networkEvent.origin == API) {
            showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_not_stable),
                    GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking), TSnackbar.LENGTH_LONG, mOnCloseSnackBarListener);
            Log.d(this, "networking is not stable");
        } else if (!networkEvent.online) {
            showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_offline),
                    GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking),
                    TSnackbar.LENGTH_INDEFINITE, !networkEvent.online ? mOnCloseSnackBarListener : null);
        } else {
            PaymentSnackBar.getInstance().dismiss();
        }
    }
}
