package vn.com.zalopay.wallet.view.component.activity;

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

import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.factory.AdapterFactory;
import vn.com.zalopay.wallet.business.behavior.view.ChannelProxy;
import vn.com.zalopay.wallet.business.behavior.view.PaymentPassword;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.message.PaymentEventBus;
import vn.com.zalopay.wallet.message.SdkSmsMessage;
import vn.com.zalopay.wallet.message.SdkUnlockScreenMessage;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;

public class PaymentChannelActivity extends BasePaymentActivity {
    public static final String PMC_CONFIG_EXTRA = "pmc_config";
    protected PaymentPassword mPaymentPassword;
    protected CountDownTimer mTimer;
    protected boolean mTimerRunning = false;
    private AdapterBase mAdapter = null;
    private boolean mIsStart = false;
    private boolean mIsSwitching = false;
    private ActivityRendering mActivityRender;
    private MiniPmcTransType mMiniPmcTransType;
    private View.OnClickListener mOnClickExitListener = v -> {
        Log.d(this, "on exit");
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
        if (getAdapter() != null && getAdapter().isRequirePinPharse()) {
            getAdapter().confirmExitTransWithoutPin();
            return;
        }
        if (getAdapter() != null && getAdapter().isZaloPayFlow() && getAdapter().isBalanceErrorPharse()) {
             mPaymentInfoHelper.setResult(PaymentStatus.FAILURE);
            if (GlobalData.getChannelActivityCallBack() != null) {
                GlobalData.getChannelActivityCallBack().onBackAction();
            }
            finish();
            return;
        }
        if (getAdapter() != null && getAdapter().exitWithoutConfirm() && !isInProgress()) {
            if (getAdapter().isTransactionSuccess()) {
                mPaymentInfoHelper.setResult(PaymentStatus.SUCCESS);
                recycleActivity();
            } else if (getAdapter().isTransactionFail()) {
                mPaymentInfoHelper.setResult(PaymentStatus.FAILURE);
                recycleActivity();
            } else if (GlobalData.getChannelActivityCallBack() != null) {
                GlobalData.getChannelActivityCallBack().onBackAction();
                finish();
            } else {
                recycleActivity();
            }
        } else {
            confirmQuitPayment();
        }
    };

    public PaymentPassword getPaymentPassword() {
        return mPaymentPassword;
    }

    protected void fillCardNumberFromCache() {
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
        //convert it to milisecond
        iTimeToLiveTrans *= 60 * 1000;
        mTimer = new CountDownTimer(iTimeToLiveTrans, 1000) {
            public void onTick(long millisUntilFinished) {
                mTimerRunning = true;
                //Log.d(this,"Timer is onTick "+millisUntilFinished);
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
        mAdapter = AdapterFactory.produce(this, mMiniPmcTransType, mPaymentInfoHelper);
        if (getAdapter() == null) {
            onExit(GlobalData.getStringResource(RS.string.sdk_config_invalid), true);
            return;
        }
        initTimer();
        renderActivity();
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
            recycleActivity();
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
    public void setBackground(int color) {

        if (color == -1)
            color = getResources().getColor(R.color.white);

        super.setBackground(color);

        Log.d(this, "===setBackground=" + color);
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
        Log.d(this, "onCreate");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (getIntent().getExtras() != null) {
            mMiniPmcTransType = getIntent().getExtras().getParcelable(PMC_CONFIG_EXTRA);
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
            Log.d(this, "can not back,order still request api");
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
        if (!mIsStart && (getAdapter() != null && (getAdapter().isZaloPayFlow() || mPaymentInfoHelper.payByCardMap()
                || mPaymentInfoHelper.payByBankAccountMap()))) {
            try {
                setConfirmTitle();
                getAdapter().moveToConfirmScreen(mMiniPmcTransType);
                mIsStart = true;
                Log.d(this, "moved to confirm screen");
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
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
                return;
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
                checkAppInfo(appId, TransactionType.LINK, userInfo.zalopay_userid, userInfo.accesstoken);
            } catch (Exception ex) {
                Log.e(this, ex);
                onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(this, "onResume");
        if (getAdapter() != null && getAdapter().isRequirePinPharse()) {
            showKeyBoardForPin();
        }
        showKeyBoardOnFocusingViewAgain();
    }

    protected void showKeyBoardOnFocusingViewAgain() {
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

    public void showKeyBoardForPin() {
        if (mPaymentPassword != null) {
            mPaymentPassword.showSoftKeyBoard();
        }
    }

    public void resetPin() {
        if (mPaymentPassword != null) {
            mPaymentPassword.reset();
        }
    }

    public void renderActivity() {
        String layoutResID = getAdapter().getLayoutID();
        if (TextUtils.isEmpty(layoutResID)) {
            onExit(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
            return;
        }
        setContentView(RS.getLayout(layoutResID));
        try {
            showApplicationInfo();
        } catch (Exception e) {
            Log.d(this, e);
        }
        try {
            showAmount();
            showDisplayInfo();
        } catch (Exception e) {
            Log.e(this, e);
            onExit(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
        }
        setMarginSubmitButtonTop(false);
        //resize pin layout if this is phone
        if (!SdkUtils.isTablet(getApplicationContext())) {
            resizeGridPasswordView();
        }
        if (!GlobalData.isChannelHasInputCard(mPaymentInfoHelper))
            renderByResource();
        setListener();
        getAdapter().setListener();
        //hide header if this is link card.
        if (mPaymentInfoHelper.isCardLinkTrans()) {
            visibleAppInfo(false);
        }
        applyFont();
    }

    @Override
    protected void setListener() {
        super.setListener();
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
                    Log.d(this, "PaymentChannelActivity.render acctivityRendering=null");
                }
            } else {
                Log.d(this, "PaymentChannelActivity.render resourceManager=null");
            }

            //enableSubmitBtn(false);
            Log.d(this, "++++ PaymentChannelActivity.renderByResource: Total time: " + (System.currentTimeMillis() - time));


        } catch (Exception e) {
            Log.e(this, e);

            onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error), true);
        }
    }

    public void renderPaymentBalanceContent(MiniPmcTransType pConfig) {
        try {
            showBalanceContent(pConfig);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void enableSubmitBtn(boolean pIsEnabled) {
        Log.d(this, "===enableSubmitBtn===" + pIsEnabled);

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

    public void setConfirmTitle() {
        String title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_confirm_pay);

        if (mPaymentInfoHelper.isTopupTrans()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_confirm_topup);
        } else if (mPaymentInfoHelper.isMoneyTranferTrans()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_confirm_tranfer);
        } else if (mPaymentInfoHelper.isWithDrawTrans()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_confirm_withdraw);
        }
        setConfirmTitle(title);
    }

    protected void updateFontCardNumber() {
        new Handler().postDelayed(() -> applyFont(findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.zpw_font_medium)), 500);
    }

    public void startTransactionExpiredTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.start();
        }
    }

    public void cancelTransactionExpiredTimer() {
        if (mTimer != null) {
            mTimer.cancel();
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
            AbstractOrder order = mPaymentInfoHelper.getOrder();
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            int transtype = TransactionType.LINK;
            MiniPmcTransType miniPmcTransType = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().
                    getPmcConfigByPmcID(BuildConfig.ZALOAPP_ID, transtype, pChannelId, null), MiniPmcTransType.class);
            if (miniPmcTransType != null) {
                mAdapter = AdapterFactory.produceChannelByPmc(this, miniPmcTransType, mPaymentInfoHelper);
                return true;
            }
        } catch (Exception e) {
            Log.e(this, e);
            onExit(e != null ? e.getMessage() : GlobalData.getStringResource(RS.string.sdk_config_invalid), true);
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
        if (getAdapter().getGuiProcessor() != null && getAdapter().getGuiProcessor().preventNextIfLinkCardExisted()) {
            try {
                getAdapter().getGuiProcessor().showHintError(getAdapter().getGuiProcessor().getCardNumberView(), getAdapter().getGuiProcessor().warningCardExist());
                return;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (!createChannelAdapter(pChannelID)) {
            return;
        }

        if (getAdapter() != null) {
            Log.d(this, "===preparing to init new adapter===" + getAdapter());

            setIsSwitching(true);

            initChannel();

            if (getAdapter().isCardFlow())
                getAdapter().getGuiProcessor().setCardInfo(pCardNumber);
        }
    }

    public void configureRequirePinPage() {

        if (mPaymentPassword == null) {
            mPaymentPassword = new PaymentPassword(findViewById(R.id.zpw_gridview_pin), findViewById(R.id.zpw_txt_PinError), findViewById(R.id.zpw_switchvisible_textview));
        } else {
            mPaymentPassword.reset();
        }

        mPaymentPassword.setOnEnterPinListener(() -> {
            if (getAdapter() != null)
                getAdapter().onClickSubmission();
        });

        mPaymentPassword.showSoftKeyBoard();

    }

    protected void confirmQuitOrGetStatus() {
        showConfirmDialogWithManyOption(GlobalData.getStringResource(RS.string.zpw_confirm_quit_loadsite), pIndex -> {
            switch (pIndex) {
                case 0:
                    break;
                case 1:
                    recycleActivity();
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
                recycleActivity();
            }
        }, message, GlobalData.getStringResource(RS.string.dialog_co_button), GlobalData.getStringResource(RS.string.dialog_khong_button));

    }

    @Override
    public void recycleActivity() {
        Log.d(this, "recycle activity");
        if (GlobalData.getChannelActivityCallBack() != null) {
            GlobalData.getChannelActivityCallBack().onExitAction();
        } else if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
        finish();
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
        PaymentEventBus.shared().removeStickyEvent(SdkUnlockScreenMessage.class);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnPaymentSmsEvent(SdkSmsMessage pSmsEventMessage) {
        String sender = pSmsEventMessage.sender;
        String body = pSmsEventMessage.message;
        if (!TextUtils.isEmpty(sender) && !TextUtils.isEmpty(body) && getAdapter() != null) {
            getAdapter().autoFillOtp(sender, body);
        }
        PaymentEventBus.shared().removeStickyEvent(SdkSmsMessage.class);
        Log.d(this, "on payment otp event " + GsonUtils.toJsonString(pSmsEventMessage));
    }
}
