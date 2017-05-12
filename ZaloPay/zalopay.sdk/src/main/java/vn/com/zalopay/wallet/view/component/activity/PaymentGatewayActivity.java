package vn.com.zalopay.wallet.view.component.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.behavior.view.ChannelStartProcessor;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.injector.BaseChannelInjector;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.datasource.task.SDKReportTask;
import vn.com.zalopay.wallet.listener.IChannelActivityCallBack;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.listener.IMoveToChannel;
import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;
import vn.com.zalopay.wallet.message.SdkSelectedChannelMessage;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.StringUtil;
import vn.com.zalopay.wallet.view.adapter.ChannelAdapter;
import vn.com.zalopay.wallet.view.adapter.RecyclerTouchListener;

/***
 * payment channel list screen.
 */
public class PaymentGatewayActivity extends BasePaymentActivity implements IChannelActivityCallBack {
    /***
     * this transaction only have 1 channel.
     * when user back to channel list,close channel list.
     */
    public static boolean isUniqueChannel = false;
    protected IMoveToChannel mMoveToChannel = new IMoveToChannel() {
        @Override
        public void moveToChannel(DPaymentChannelView pChannel) {
            final BasePaymentActivity channelActivity = BasePaymentActivity.getPaymentChannelActivity();
            onSelectedChannel(pChannel);

            //close old channel payment activity
            if (channelActivity != null && !channelActivity.isFinishing()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (channelActivity != null && !channelActivity.isFinishing()) {
                            channelActivity.finish();
                            Log.d(this, "close old channel activity after user select map card or bank account map");
                        }
                    }
                }, 50);

            }
        }
    };
    protected ChannelAdapter mChannelAdapter;
    private BaseChannelInjector baseChannelInjector;
    private RecyclerView mChannelRecyclerView;

    //prevent click duplicate
    private boolean mMoreClick = true;
    /***
     * exit click
     */
    private View.OnClickListener mOnClickExitListener = v -> recycleActivity();
    private ILoadBankListListener mLoadBankListListener = new ILoadBankListListener() {
        @Override
        public void onProcessing() {
        }

        @Override
        public void onComplete() {
            showPaymentChannel();
            Log.d(this, "===show Channel===ILoadBankListListener() onComplete");
        }

        @Override
        public void onError(String pMessage) {
            if (TextUtils.isEmpty(pMessage)) {
                pMessage = GlobalData.getStringResource(RS.string.zpw_alert_error_networking_when_load_banklist);
            }
            onExit(pMessage, true);
        }
    };

    public static boolean isUniqueChannel() {
        return isUniqueChannel;
    }

    //region system functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(this, "===onCreate===");

        if (!ConnectionUtil.isOnline(this)) {
            onReturnCancel(GlobalData.getStringResource(RS.string.zingpaysdk_alert_no_connection));
            return;
        }

        //keep callback listener
        GlobalData.setChannelActivityCallBack(this);
        //clear card number on cache if this's not link card channel
        try {
            SharedPreferencesManager.getInstance().pickCachedCardNumber();
        } catch (Exception e) {
            Log.e(this, e);
        }

        setContentView(RS.getLayout(RS.layout.screen__gateway));

        mChannelRecyclerView = (RecyclerView) findViewById(R.id.channel_recycler_view);

        initializeChannelRecycleView();

        showAmount();

        showDisplayInfo();

        setListener();

        setToolBarTitle();

        setTitle();

        //validate user level
        if (!checkUserLevelValid()) {
            confirmUpgradeLevel();
            return;
        }
        checkAppInfo(); //check app info whether this transaction is allowed or not
    }

    protected void initializeChannelRecycleView() {
        baseChannelInjector = BaseChannelInjector.createChannelInjector();
        mChannelAdapter = new ChannelAdapter(getApplicationContext(), baseChannelInjector.getChannelList(), R.layout.channel_item_recyclerview);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mChannelRecyclerView.setHasFixedSize(true);
        mChannelRecyclerView.setLayoutManager(mLayoutManager);
        mChannelRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mChannelRecyclerView.setAdapter(mChannelAdapter);
        mChannelRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mChannelRecyclerView));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnSelectChannelEvent(SdkSelectedChannelMessage pMessage) {
        if (mMoreClick) {   //prevent so many click on channel.
            mMoreClick = false;
            GlobalData.getTransactionType();//reset transtype and bank func type
            GlobalData.selectBankFunctionByTransactionType();
            onSelectedChannel(baseChannelInjector.getChannelAtPosition(pMessage.position));
            new Handler().postDelayed(() -> mMoreClick = true, 1000);
        }
    }

    //endregion

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(this, "==== onResume ====");
    }

    @Override
    protected void onStart() {
        super.onStart();

        applyFont();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogManager.dismiss();
        //release callback from payment channel activity
        GlobalData.setChannelActivityCallBack(null);

        if (mChannelAdapter != null) {
            mChannelAdapter = null;
        }
        System.gc();
        Log.d(this, "on destroy");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (android.os.Build.VERSION.SDK_INT > 5 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (!isInProgress()) {
            recycleActivity();
            return;
        }
        //CONFIRM BEFORE EXIT
        String message = GlobalData.getStringResource(RS.string.zingpaysdk_confirm_quit);

        showConfirmDialog(new ZPWOnEventConfirmDialogListener() {
                              @Override
                              public void onCancelEvent() {

                              }

                              @Override
                              public void onOKevent() {
                                  recycleActivity();
                              }
                          }, message, GlobalData.getStringResource(RS.string.dialog_agree_button),
                GlobalData.getStringResource(RS.string.dialog_comeback_button));
    }

    //region user functions
    @Override
    protected void setListener() {
        super.setListener();
        findViewById(R.id.zpsdk_exit_ctl).setOnClickListener(this.mOnClickExitListener);
    }

    private void showChannelListView() {
        //force to some channel from client
        Log.d(this, "start show channel to listview");
        if (GlobalData.isForceChannel()) {
            baseChannelInjector.filterForceChannel(GlobalData.getPaymentInfo().forceChannelIds);
        }

        // don't have any channel now
        if (baseChannelInjector.isChannelEmpty()) {
            isUniqueChannel = true;
            String alertMessage = getAmountAlert();
            if (TextUtils.isEmpty(alertMessage)) {
                /***
                 * this is withdraw link card and no mapped card.
                 * need remind user go to link card to can withdraw
                 */
                if (GlobalData.isWithDrawChannel()) {
                    confirmLinkCard();
                    return;
                }
                alertMessage = GlobalData.getStringResource(RS.string.zpw_app_info_exclude_channel);
            }
            onReturnCancel(alertMessage);
            return;
        }

        // just have 1 channel only
        if (baseChannelInjector.isChannelUnique()) {

            Log.d(this, "has only 1 channel");
            showProgress(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_transition_screen));

            DPaymentChannelView uniqueChannel = baseChannelInjector.getFirstChannel();

            if (uniqueChannel != null
                    && uniqueChannel.isEnable()
                    && uniqueChannel.isAllowByAmount()
                    && uniqueChannel.isAllowByLevel()
                    && uniqueChannel.isAllowByAmountAndFee()
                    && !uniqueChannel.isMaintenance()) {
                goToChannel(uniqueChannel);

                isUniqueChannel = true;

                return;
            }
        }

        refreshChannelRecyclerView();
    }

    private void refreshChannelRecyclerView() {
        mChannelAdapter.notifyDataSetChanged();
        isUniqueChannel = baseChannelInjector.isChannelUnique();
    }

    /***
     * prepare show channel and show header.
     */
    private synchronized void showPaymentChannel() {
        showProgress(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_process_view));
        //show header
        if (GlobalData.isTopupChannel() || GlobalData.isPayChannel() || GlobalData.isWithDrawChannel()) {
            Log.d(this, "show app info");
            showApplicationInfo();
        }
        try {
            Log.d(this, "show channels");
            getPaymentChannel();   //get channel from cache for this transaction
        } catch (Exception e) {
            Log.e(this, e);
            onExit(e != null ? e.getMessage() : GlobalData.getStringResource(RS.string.zpw_alert_error_data), true);
        }

    }

    /***
     * get all channel by this app + transaction
     */
    private void getPaymentChannel() throws Exception {
        Log.d(this, "===show Channel===getPaymentChannel()");
        baseChannelInjector.getChannels(new ZPWOnGetChannelListener() {
            @Override
            public void onGetChannelComplete() {
                showChannelListView();

                showProgress(false, GlobalData.getStringResource(RS.string.walletsdk_string_bar_title));
            }

            @Override
            public void onGetChannelError(String pError) {
                onExit(pError, true);
            }
        });

    }

    @Override
    protected void readyForPayment() {
        showProgress(true, GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        BankLoader.loadBankList(mLoadBankListListener);
        Log.d(this, "ready for payment");
    }

    @Override
    protected void notifyUpVersionToApp(boolean pForceUpdate, String pVersion, String pMessage) {
        GlobalData.getPaymentListener().onUpVersion(pForceUpdate, pVersion, pMessage);

        if (pForceUpdate) {
            finish();
        }
    }

    protected void showDialogAndExit(String pMessage, boolean pIsShow) {
        Log.d(this, "===pMessage=" + pMessage + "===pIsShow=" + pIsShow);
        if (pIsShow) {
            onReturnCancel(pMessage);
        } else {
            onExit(pMessage, pIsShow);
        }
    }

    @Override
    protected void actionIfPreventApp() {
        onReturnCancel(GlobalData.getStringResource(RS.string.zpw_not_allow_payment_app));
    }

    @Override
    public void recycleActivity() {
        Log.d(this, "recycle activity");
        setEnableView(R.id.zpsdk_exit_ctl, false);
        finish();
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
        }
    }

    /***
     * get min/max for each channel.
     * use for alert if user input amount out of range support
     *
     * @return
     */
    public String getAmountAlert() {
        String strAlert = "";
        if (baseChannelInjector.hasMinValueChannel() && GlobalData.getOrderAmount() < baseChannelInjector.getMinValueChannel()) {
            strAlert = String.format(GlobalData.getStringResource(RS.string.zpw_string_alert_min_amount_input),
                    StringUtil.formatVnCurrence(String.valueOf(baseChannelInjector.getMinValueChannel())));
        } else if (baseChannelInjector.hasMaxValueChannel() && GlobalData.getOrderAmount() > baseChannelInjector.getMaxValueChannel()) {
            strAlert = String.format(GlobalData.getStringResource(RS.string.zpw_string_alert_max_amount_input),
                    StringUtil.formatVnCurrence(String.valueOf(baseChannelInjector.getMaxValueChannel())));
        }
        return strAlert;
    }

    /***
     * dialog ask for update user level
     */
    private void confirmUpgradeLevel() {

        showProgress(false, GlobalData.getStringResource(RS.string.walletsdk_string_bar_title));

        showNoticeDialog(new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                recycleActivity();
            }

            @Override
            public void onOKevent() {
                GlobalData.setResultUpgrade();
                recycleActivity();
            }
        }, GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update));
    }

    /***
     * dialog ask for link card to withdraw money
     */
    private void confirmLinkCard() {

        showProgress(false, GlobalData.getStringResource(RS.string.walletsdk_string_bar_title));

        showNoticeDialog(new ZPWOnEventConfirmDialogListener() {
                             @Override
                             public void onCancelEvent() {
                                 GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());

                                 finish();
                             }

                             @Override
                             public void onOKevent() {
                                 GlobalData.setResultNeedToLinkCard();

                                 GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());

                                 finish();
                             }
                         }, GlobalData.getStringResource(RS.string.zpw_string_alert_linkcard_channel_withdraw), GlobalData.getStringResource(RS.string.dialog_linkcard_button),
                GlobalData.getStringResource(RS.string.dialog_close_button));
    }

    public void notifyToMerchant() {
        if (ErrorManager.needToTerminateTransaction())
            recycleActivity();
        else
            exitIfUniqueChannel();
    }

    public void exitIfUniqueChannel() {
        if (isUniqueChannel()) {
            recycleActivity();
        }
    }

    //endregion

    //region listeners

    private void goToChannel(DPaymentChannelView pChannel) {
        ZPWPaymentInfo paymentInfo = null;
        try {
            paymentInfo = GlobalData.getPaymentInfo();
        } catch (Exception e) {
            Log.e(this, e);
        }

        if (paymentInfo == null || paymentInfo.userInfo == null || !paymentInfo.userInfo.isUserInfoValid() || !paymentInfo.userInfo.isUserProfileValid()) {
            //alert error
            SDKReportTask.makeReportError(SDKReportTask.INVALID_PAYMENTINFO, GsonUtils.toJsonString(GlobalData.getPaymentInfo()));
            onReturnCancel(GlobalData.getStringResource(RS.string.zpw_error_paymentinfo));
            return;
        }

        //map card channel clicked
        if (!TextUtils.isEmpty(pChannel.f6no) && !TextUtils.isEmpty(pChannel.l4no)) {
            if (pChannel.isBankAccountMap()) {
                paymentInfo.mapBank = new DBankAccount();
            } else {
                paymentInfo.mapBank = new DMappedCard();
            }

            paymentInfo.mapBank.setLastNumber(pChannel.l4no);
            paymentInfo.mapBank.setFirstNumber(pChannel.f6no);
            paymentInfo.mapBank.bankcode = pChannel.bankcode;

            AdapterBase.existedMapCard = true;
        } else {
            paymentInfo.mapBank = null;
            AdapterBase.existedMapCard = false;
        }

        //calculate fee and total amount order
        populateAmout(pChannel);

        ChannelStartProcessor.getInstance(this).setChannel(pChannel).startGateWay();
        Log.d(this, "===show Channel===goToChannel()");
    }

    /***
     * get fee of selected channel
     */
    private void populateAmout(DPaymentChannelView pChannel) {
        if (pChannel != null) {
            GlobalData.orderAmountFee = pChannel.totalfee;
            GlobalData.orderAmountTotal = GlobalData.getOrderAmount() + GlobalData.orderAmountFee;
        }
    }

    private void startChannelDirect(int pChannelID) {
        try {
            Intent intent = new Intent(GlobalData.getAppContext(), PaymentChannelActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(PaymentChannelActivity.PMCID_EXTRA, pChannelID);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(this, e);
            onExit(getResources().getString(R.string.zingpaysdk_alert_context_error), true);
        }
    }

    //item on listview is clicked
    private void onSelectedChannel(DPaymentChannelView pChannel) {
        //bank is maintenance
        if (pChannel.isMaintenance()) {
            if (GlobalData.getCurrentBankFunction() == BankFunctionCode.PAY) {
                GlobalData.getPayBankFunction(pChannel);
            }
            showBankMaintenance(pChannel.bankcode);
            return;
        }
        if (!pChannel.isEnable() || !pChannel.isAllowByAmount() || !pChannel.isAllowByAmountAndFee()) {
            Log.d(this, "===onSelectedChannel===not support===");
            return;
        }
        goToChannel(pChannel);
    }

    @Override
    public void onBackAction() {
        exitIfUniqueChannel();
    }

    @Override
    public void onExitAction() {
        recycleActivity();
    }

    @Override
    public void onCallBackAction(boolean pIsShowDialog, String pMessage) {
        if (pIsShowDialog) {
            showWarningDialog(this::notifyToMerchant, pMessage);
        } else {
            notifyToMerchant();
        }
    }

    public List<DPaymentChannelView> getChannelList() {
        if (baseChannelInjector != null) {
            return baseChannelInjector.getChannelList();
        }
        return null;
    }

    public IMoveToChannel getMoveToChannelListener() {
        return mMoveToChannel;
    }
    //endregion
}
