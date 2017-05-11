package vn.com.zalopay.wallet.view.component.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;

import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.behavior.view.ChannelStartProcessor;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.injector.BaseChannelInjector;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.datasource.request.SDKReport;
import vn.com.zalopay.wallet.listener.IChannelActivityCallBack;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.listener.IMoveToChannel;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.StringUtil;
import vn.com.zalopay.wallet.view.adapter.GatewayChannelListViewAdapter;
import vn.com.zalopay.wallet.view.custom.overscroll.OverScrollDecoratorHelper;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

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
        public void moveToChannel(PaymentChannel pChannel) {
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
    //region variable
    //injector for channel
    private BaseChannelInjector baseChannelInjector;
    private ListView mChannelListView;
    private GatewayChannelListViewAdapter mChannelListViewAdapter = null;

    //endregion
    //prevent click duplicate
    private boolean mMoreClick = true;
    /***
     * exit click
     */
    private View.OnClickListener mOnClickExitListener = v -> recycleActivity();
    /***
     * click item on channel listview
     */
    private AdapterView.OnItemClickListener mChannelItemClick = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //prevent so many click on channel.
            if (mMoreClick) {
                mMoreClick = false;

                //reset transtype and bank func type
                GlobalData.getTransactionType();
                onSelectedChannel(baseChannelInjector.getChannelAtPosition(position));

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mMoreClick = true;
                    }
                }, 1000);
            }
        }
    };
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

        //this is link acc , go to channel directly
        if (GlobalData.isBankAccountLink()) {
            MiniPmcTransType miniPmcTransType = null;
            String pmcId = GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount);
            try {
                miniPmcTransType = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getPmcConfigByPmcID(pmcId, null), MiniPmcTransType.class);
            } catch (Exception e) {
                Log.d(this, e);
            }
            if (miniPmcTransType == null) {
                onReturnCancel(GlobalData.getStringResource(RS.string.sdk_config_invalid));
            } else {
                startChannelDirect(miniPmcTransType);
                isUniqueChannel = true;
            }
            return;
        }

        //this is link card , go to channel directly
        if (GlobalData.isLinkCardChannel()) {
            MiniPmcTransType miniPmcTransType = null;
            String pmcId = GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm);
            try {
                miniPmcTransType = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getPmcConfigByPmcID(pmcId, null), MiniPmcTransType.class);
                if (miniPmcTransType == null) {
                    pmcId = GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card);
                    miniPmcTransType = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getPmcConfigByPmcID(pmcId, null), MiniPmcTransType.class);
                }
            } catch (Exception e) {
                Log.d(this, e);
            }
            if (miniPmcTransType == null) {
                onReturnCancel(GlobalData.getStringResource(RS.string.sdk_config_invalid));
            } else {
                startChannelDirect(miniPmcTransType);
                isUniqueChannel = true;
            }
            return;
        }

        //clear card number on cache if this's not link card channel
        try {
            SharedPreferencesManager.getInstance().pickCachedCardNumber();
        } catch (Exception e) {
            Log.e(this, e);
        }

        setContentView(RS.getLayout(RS.layout.screen__gateway));

        try {

            mChannelListView = (ListView) findViewById(R.id.zpw_channel_listview);

            //support over swipe to listview
            OverScrollDecoratorHelper.setUpOverScroll(mChannelListView);

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

            //check app info whether this transaction is allowed or not
            checkAppInfo();

        } catch (Exception ex) {
            Log.e(this, ex);
            onReturnCancel(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
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

        if (mChannelListViewAdapter != null) {
            mChannelListViewAdapter.clear();
            mChannelListViewAdapter = null;
        }

        System.gc();

        Log.d(this, "====== onDestroy ======");
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
        ZPAnalyticsTrackerWrapper.getInstance().ZPApptransIDLog(ZPPaymentSteps.OrderStepResult_None, ZPPaymentSteps.OrderStepResult_UserCancel, 0, 1);
        //user is summiting order
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
        Log.d(this, "===show Channel===showChannelListView()");
        if (GlobalData.isForceChannel()) {
            baseChannelInjector.filterForceChannel(GlobalData.getPaymentInfo().forceChannelIds);
        }

        //we don't have any channel now
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

        //we just have 1 channel only
        if (baseChannelInjector.isChannelUnique()) {

            Log.d(this, "===show Channel===showChannelListView() 1 channel");
            showProgress(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_transition_screen));

            PaymentChannel uniqueChannel = baseChannelInjector.getFirstChannel();

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

        populateListView();
    }

    /***
     * fill channel to listview
     */
    private void populateListView() {
        Log.d(this, "populate channel list to view ", baseChannelInjector.getChannelList());
        mChannelListViewAdapter = new GatewayChannelListViewAdapter(this, RS.getLayout(RS.layout.listview__item__channel__gateway), baseChannelInjector.getChannelList());
        mChannelListView.setAdapter(mChannelListViewAdapter);
        mChannelListView.setOnItemClickListener(mChannelItemClick);
        if (baseChannelInjector.isChannelUnique()) {
            isUniqueChannel = true;
        } else {
            isUniqueChannel = false;
        }
        Log.d(this, "===show Channel===populateListView()");
    }

    /***
     * prepare show channel and show header.
     */
    private synchronized void showPaymentChannel() {

        showProgress(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_process_view));
        //show header
        if (GlobalData.isTopupChannel() || GlobalData.isPayChannel() || GlobalData.isWithDrawChannel())
            try {

                showApplicationInfo();
                Log.d(this, "===show Channel===showPaymentChannel()==showApplicationInfo()");

            } catch (Exception e) {
                Log.e(this, e);
            }
        try {
            getPaymentChannel();//get channel from cache for this transaction
            Log.d(this, "===show Channel===showPaymentChannel()== getPaymentChannel()");
        } catch (Exception e) {
            Log.e(this, e);

            onExit(e != null ? e.getMessage() : GlobalData.getStringResource(RS.string.zpw_alert_error_data), true);
        }

    }

    /***
     * get all channel by this app + transaction
     */
    private void getPaymentChannel() throws Exception {
        try {
            Log.d(this, "===show Channel===getPaymentChannel()");
            baseChannelInjector = BaseChannelInjector.createChannelInjector();
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

        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    protected void readyForPayment() {
        showProgress(true, GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        BankLoader.loadBankList(mLoadBankListListener);
        Log.d(this, "===show Channel===readyForPayment()");

    }

    @Override
    public void notifyUpVersionToApp(boolean pForceUpdate, String pVersion, String pMessage) {
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onUpVersion(pForceUpdate, pVersion, pMessage);
        }
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
        Log.d(this, "===recycleActivity===");
        setEnableView(R.id.zpsdk_exit_ctl, false);
        // TrackApptransidEvent AuthenType
        ZPAnalyticsTrackerWrapper.getInstance().ZPApptransIDLog(ZPPaymentSteps.OrderStepResult_None, ZPPaymentSteps.OrderStepResult_UserCancel, 0, 1);
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
                                 if (GlobalData.getPaymentListener() != null) {
                                     GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
                                 }
                                 finish();
                             }

                             @Override
                             public void onOKevent() {
                                 GlobalData.setResultNeedToLinkCard();
                                 if (GlobalData.getPaymentListener() != null) {
                                     GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
                                 }
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

    private void goToChannel(PaymentChannel pChannel) {
        ZPWPaymentInfo paymentInfo = null;
        try {
            paymentInfo = GlobalData.getPaymentInfo();
        } catch (Exception e) {
            Log.e(this, e);
        }

        if (paymentInfo == null || paymentInfo.userInfo == null || !paymentInfo.userInfo.isUserInfoValid() || !paymentInfo.userInfo.isUserProfileValid()) {
            //alert error
            SDKReport.makeReportError(SDKReport.INVALID_PAYMENTINFO, GsonUtils.toJsonString(GlobalData.getPaymentInfo()));
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
        GlobalData.populateOrderFee(pChannel);

        ChannelStartProcessor.getInstance(this).setChannel(pChannel).startGateWay();
        Log.d(this, "===show Channel===goToChannel()");

        // TrackApptransidEvent choose pay method
        ZPAnalyticsTrackerWrapper.getInstance().ZPApptransIDLog(ZPPaymentSteps.OrderStep_ChoosePayMethod, ZPPaymentSteps.OrderStepResult_None, pChannel.hashCode());
    }

    private void startChannelDirect(MiniPmcTransType pMiniPmcTransType) {
        try {
            Intent intent = new Intent(GlobalData.getAppContext(), PaymentChannelActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(PaymentChannelActivity.PMC_CONFIG_EXTRA, pMiniPmcTransType);
            startActivity(intent);

        } catch (Exception e) {
            Log.e(this, e);
            onExit(getResources().getString(R.string.zingpaysdk_alert_context_error), true);
        }
    }

    //item on listview is clicked
    private void onSelectedChannel(PaymentChannel pChannel) {
        //bank is maintenance
        if (pChannel.isMaintenance()) {
            if (GlobalData.getCurrentBankFunction() == EBankFunction.PAY) {
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
            showWarningDialog(() -> notifyToMerchant(), pMessage);

        } else {
            notifyToMerchant();
        }
    }

    public ArrayList<PaymentChannel> getChannelList() {
        if (baseChannelInjector != null) {
            return (ArrayList<PaymentChannel>) baseChannelInjector.getChannelList();
        }
        return null;
    }

    public IMoveToChannel getMoveToChannelListener() {
        return mMoveToChannel;
    }


    //endregion
}
