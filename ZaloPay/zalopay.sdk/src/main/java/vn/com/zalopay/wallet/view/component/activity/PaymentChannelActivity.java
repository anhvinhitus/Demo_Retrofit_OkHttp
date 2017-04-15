package vn.com.zalopay.wallet.view.component.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.factory.AdapterFactory;
import vn.com.zalopay.wallet.business.behavior.view.PaymentPassword;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

public class PaymentChannelActivity extends BasePaymentActivity {

    protected PaymentPassword mPaymentPassword;
    protected CountDownTimer mTimer;
    protected boolean mTimerRunning = false;
    private AdapterBase mAdapter = null;
    private boolean mIsRestart = false; //prevent duplicate on some function when activity resume.
    private boolean mIsStart = false;
    private boolean mIsSwitching = false;
    private WeakReference<BankSmsReceiver> mSmsReceiver;
    private WeakReference<UnlockSreenReceiver> mUnLockScreenReceiver;
    private ActivityRendering mActivityRender;
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
        if (getAdapter() != null && getAdapter().isRequirePinPharse()) {
            getAdapter().confirmExitTransWithoutPin();
            return;
        }
        if (getAdapter() != null && getAdapter().isZaloPayFlow() && getAdapter().isBalanceErrorPharse()) {
            GlobalData.setResultFail();
            if (GlobalData.getChannelActivityCallBack() != null) {
                GlobalData.getChannelActivityCallBack().onBackAction();
            }
            finish();
            return;
        }
        if (getAdapter() != null && getAdapter().exitWithoutConfirm() && !isInProgress()) {
            if (getAdapter().isTransactionSuccess()) {
                GlobalData.setResultSuccess();
                if (GlobalData.getChannelActivityCallBack() != null) {
                    GlobalData.getChannelActivityCallBack().onExitAction();
                }
            } else if (getAdapter().isTransactionFail()) {
                GlobalData.setResultFail();
                if (GlobalData.getChannelActivityCallBack() != null) {
                    GlobalData.getChannelActivityCallBack().onExitAction();
                }
            } else {
                if (GlobalData.getChannelActivityCallBack() != null) {
                    GlobalData.getChannelActivityCallBack().onBackAction();
                }
            }
            finish();
            return;
        }

        confirmQuitPayment();
    };

    public PaymentPassword getmPaymentPassword() {
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


    public BankSmsReceiver getSmsReceiver() {
        if (mSmsReceiver != null) {
            return mSmsReceiver.get();
        }
        return null;
    }

    public UnlockSreenReceiver getUnLockScreenReceiver() {
        if (mUnLockScreenReceiver != null) {
            return mUnLockScreenReceiver.get();
        }
        return null;
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (mTimerRunning && !getAdapter().isFinalScreen()) {
            Log.d(this, "===onUserInteraction===startTransactionExpiredTimer");
            startTransactionExpiredTimer();
        }
    }

    /***
     * transaction expired in 7minutes
     */
    protected void initTimer() {
        String sTimeToLiveTrans = GlobalData.getStringResource(RS.string.time_to_live_transaction);
        Long lTimeToLiveTrans = 7L;
        if (!TextUtils.isEmpty(sTimeToLiveTrans)) {
            try {
                lTimeToLiveTrans = Long.parseLong(sTimeToLiveTrans);
            } catch (Exception ex) {
                Log.e(this, ex);
                lTimeToLiveTrans = 7L;
            }
        }

        //convert it to milisecond
        lTimeToLiveTrans *= 60 * 1000;
        mTimer = new CountDownTimer(lTimeToLiveTrans, 1000) {
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

    @Override
    protected void readyForPayment() {
        //render resource again after finishing loading resource.
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
    protected void notifyUpVersionToApp(boolean pForceUpdate, String pVersion, String pMessage) {
        GlobalData.getPaymentListener().onUpVersion(pForceUpdate, pVersion, pMessage);

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

        if (getSmsReceiver() != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(getSmsReceiver());
        }
        if (getUnLockScreenReceiver() != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(getUnLockScreenReceiver());
        }

        if (isFinishing() && getAdapter() != null) {
            getAdapter().onFinish();
            mAdapter = null;
        }

        if (DialogManager.isShowingProgressDialog())
            DialogManager.closeProcessDialog();

        cancelTransactionExpiredTimer();

        Log.d(this, "==== onDestroy ====");

        System.gc();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initTimer();
        mIsRestart = false;
        mAdapter = AdapterFactory.produce(this);
        if (getAdapter() == null) {
            onExit(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
            return;
        }
        renderActivity();
        getAdapter().init();
        if (GlobalData.isChannelHasInputCard()) {
            renderResourceAfterDelay();
        }
    }

    @Override
    public void onBackPressed() {
        //user is summiting order
        if (getVisibilitySupportView()) {
            closeSupportView();
            return;
        }
        if (processingOrder) {
            Log.d(this, "can not back,order still request api");
            return;
        }
        mOnClickExitListener.onClick(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        applyFont();
        updateFontCardNumber();
        if (!mIsStart && (getAdapter().isZaloPayFlow() || GlobalData.isMapCardChannel() || GlobalData.isMapBankAccountChannel())) {
            try {
                getAdapter().moveToConfirmScreen();
                mIsStart = true;
                Log.d(this, "moved to confirm screen");
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(this, "==== onResume ====");
        if (getAdapter() != null && getAdapter().isRequirePinPharse()) {
            showKeyBoardForPin();
        }
        showKeyBoardOnFocusingViewAgain();
        //this is link account and the first call
        if (GlobalData.isBankAccountLink() && !mIsRestart) {
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
        if (GlobalData.isLinkCardChannel() && !mIsRestart) {
            //check profile level permission in table map
            try {
                int allowATM = GlobalData.checkPermissionByChannelMap(Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm)));
                int allowCC = GlobalData.checkPermissionByChannelMap(Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card)));

                if (allowATM == Constants.LEVELMAP_INVALID && allowCC == Constants.LEVELMAP_INVALID) {
                    onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
                    return;
                } else if (allowATM == Constants.LEVELMAP_BAN && allowCC == Constants.LEVELMAP_BAN) {
                    getAdapter().confirmUpgradeLevel();
                    return;
                }
                isAllowLinkCardATM = (allowATM == Constants.LEVELMAP_ALLOW);
                isAllowLinkCardCC = (allowCC == Constants.LEVELMAP_ALLOW);
                //switch to cc adapter if link card just allow cc without atm
                if (!isAllowLinkCardATM && isAllowLinkCardCC && createChannelAdapter(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card))) {
                    initChannel();
                }
                checkAppInfo();

            } catch (Exception ex) {
                Log.e(this, ex);
                onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
                return;
            }
        }
        mIsRestart = true;
        //Register the local broadcast receiver,for otp comming.
        IntentFilter messageFilter = new IntentFilter();
        messageFilter.addAction(Constants.FILTER_ACTION_BANK_SMS_RECEIVER);
        mSmsReceiver = new WeakReference<BankSmsReceiver>(new BankSmsReceiver());
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(getSmsReceiver(), messageFilter);
        // UnLock broadcast receiver change event
        IntentFilter unlockFilter = new IntentFilter();
        unlockFilter.addAction(Constants.ACTION_UNLOCK_SCREEN);
        mUnLockScreenReceiver = new WeakReference<UnlockSreenReceiver>(new UnlockSreenReceiver());
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(getUnLockScreenReceiver(), unlockFilter);
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
        Log.d(this, "==== onRestart ====");
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
        if (!ZPWUtils.isTablet(getApplicationContext())) {
            resizeGridPasswordView();
        }

        if (!GlobalData.isChannelHasInputCard())
            renderByResource();

        setListener();

        getAdapter().setListener();

        //hide header if this is link card.
        if (GlobalData.isLinkCardChannel()) {
            visibleAppInfo(false);
        }

    }

    @Override
    protected void setListener() {
        super.setListener();

        //event exit button
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
            Log.d(this, "++++ PaymentChannelActivity.renderByResource: Total time: " + (System.currentTimeMillis() - time));


        } catch (Exception e) {
            Log.e(this, e);
            onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error), true);
        }
    }

    public void renderPaymentBalanceContent(DPaymentChannel pConfig) {
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

        if (GlobalData.isTopupChannel()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_confirm_topup);
        } else if (GlobalData.isTranferMoneyChannel()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_confirm_tranfer);
        } else if (GlobalData.isWithDrawChannel()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_confirm_withdraw);
        }

        setConfirmTitle(title);
    }

    protected void updateFontCardNumber() {
        //update font
        new Handler().postDelayed(() -> ZPWUtils.applyFont(findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.zpw_font_medium)), 500);
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
            Log.d(this, "===renderByResource===on event layout===");
            //register event finish layout to resize buttons
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
            Log.d(this, "===renderByResource===on handler===");
            new Handler().postDelayed(this::renderByResource, 500);
        }
    }

    protected boolean createChannelAdapter(String pChannelId) {
        try {
            Log.d(this, "====createChannelAdapter===pChannelId=" + pChannelId);
            //release old adapter
            if (getAdapter() != null) {
                getAdapter().onFinish();
                mAdapter = null;
            }

            mAdapter = AdapterFactory.produceChannelByID(this, pChannelId);

            return true;

        } catch (Exception e) {
            Log.e(this, e);

            onExit(e != null ? e.getMessage() : GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
        }

        return false;
    }

    protected void initChannel() {
        getAdapter().init();
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
    public synchronized void switchChannel(String pChannelID, final String pCardNumber) {

        if (getAdapter() != null && getAdapter().isATMFlow() && pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm)))
            return;
        if (getAdapter() != null && getAdapter().isCCFlow() && pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card)))
            return;

        //prevent user move to next if input existed card in link card
        if (getAdapter().getGuiProcessor() != null && getAdapter().getGuiProcessor().preventNextIfLinkCardExisted()) {
            try {
                getAdapter().getGuiProcessor().showHintError(getAdapter().getGuiProcessor().getCardNumberView(), GlobalData.getStringResource(RS.string.zpw_link_card_existed));

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

        mPaymentPassword.setOnEnterPinListener(new PaymentPassword.onEnterPinListener() {
            @Override
            public void onEnterPinComplete() {
                if (getAdapter() != null)
                    getAdapter().onClickSubmission();
            }
        });

        mPaymentPassword.showSoftKeyBoard();

    }

    protected void confirmQuitOrGetStatus() {
        showConfirmDialogWithManyOption(GlobalData.getStringResource(RS.string.zpw_confirm_quit_loadsite), pIndex -> {
            switch (pIndex) {
                case 0:
                    break;
                case 1:
                    if (GlobalData.getChannelActivityCallBack() != null) {
                        GlobalData.getChannelActivityCallBack().onExitAction();
                    }
                    finish();
                    break;
                case 2:
                    getAdapter().onEvent(EEventType.ON_BACK_WHEN_LOADSITE, new Object());
                    break;
            }
        }, GlobalData.getStringResource(RS.string.dialog_khong_button), GlobalData.getStringResource(RS.string.dialog_co_button), GlobalData.getStringResource(RS.string.dialog_getstatus_button));
    }

    protected void confirmQuitPayment() {
        String message = GlobalData.getStringResource(RS.string.zingpaysdk_confirm_quit);

        if (GlobalData.isPayChannel()) {
            message = GlobalData.getStringResource(RS.string.zingpaysdk_confirm_quit_rescan_qrcode);
        } else if (GlobalData.isBankAccountLink()) {
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
                if (GlobalData.getChannelActivityCallBack() != null) {
                    GlobalData.getChannelActivityCallBack().onExitAction();
                }
                finish();
            }
        }, message, GlobalData.getStringResource(RS.string.dialog_co_button), GlobalData.getStringResource(RS.string.dialog_khong_button));

    }

    @Override
    public void recycleActivity() {
        if (GlobalData.getChannelActivityCallBack() != null) {
            GlobalData.getChannelActivityCallBack().onExitAction();
            Log.d(this, "===recycleActivity===GlobalData.getChannelActivityCallBack().onExitAction()");
        } else if (GlobalData.getPaymentListener() != null) {
            //callback to app
            GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
            Log.d(this, "===recycleActivity===GlobalData.getPaymentListener() != null");
        } else {
            Log.e(this, "===recycleActivity===ERROR");
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

    //automatic get otp from comming message from bank and fill to layout
    public class BankSmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Constants.FILTER_ACTION_BANK_SMS_RECEIVER)) {
                String sender = intent.getExtras().getString(Constants.BANK_SMS_RECEIVER_SENDER);
                String body = intent.getExtras().getString(Constants.BANK_SMS_RECEIVER_BODY);
                if (!TextUtils.isEmpty(sender) && !TextUtils.isEmpty(body) && getAdapter() != null) {
                    getAdapter().autoFillOtp(sender, body);
                }
            }
        }
    }

    /***
     * Check user unlock screen .
     * When user unlock screen focus down item
     */
    public class UnlockSreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UNLOCK_SCREEN)) {
                if (getAdapter() != null && mAdapter.isCardFlow()) {
                    getAdapter().getGuiProcessor().moveScrollViewToCurrentFocusView();
                }
            }
        }
    }
}
