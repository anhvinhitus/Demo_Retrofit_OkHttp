package vn.com.zalopay.wallet.view.component.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.VisibleForTesting;
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

import rx.functions.Action1;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.ui.channellist.ChannelProxy;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.injector.AbstractChannelLoader;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.listener.IChannelActivityCallBack;
import vn.com.zalopay.wallet.listener.IMoveToChannel;
import vn.com.zalopay.wallet.event.SdkSelectedChannelMessage;
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
    protected ChannelAdapter mChannelAdapter;
    private AbstractChannelLoader baseChannelInjector;
    private RecyclerView mChannelRecyclerView;

    //prevent click duplicate
    private boolean mMoreClick = true;
    /***
     * exit click
     */
    private View.OnClickListener mOnClickExitListener = v -> recycleActivity();

    public static boolean isUniqueChannel() {
        return isUniqueChannel;
    }

    //region system functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this, "onCreate");
        //keep callback listener
        GlobalData.setChannelActivityCallBack(this);
        setContentView(RS.getLayout(RS.layout.screen__gateway));
        mChannelRecyclerView = (RecyclerView) findViewById(R.id.channel_recycler_view);
        initializeChannelRecycleView();
        setListener();
    }

    @Override
    public void paymentInfoReady() {
        showAmount();
        showDisplayInfo();
        setToolBarTitle();
        setTitle();
        initializeChannelAdapter();
        //validate user level
        if (!checkUserLevelValid()) {
            confirmUpgradeLevel();
            return;
        }
        long appId = mPaymentInfoHelper.getAppId();
        @TransactionType int transtype = mPaymentInfoHelper.getTranstype();
        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        loadAppInfo(appId, transtype, userInfo.zalopay_userid, userInfo.accesstoken); //check app info whether this transaction is allowed or not
    }

    protected void initializeChannelRecycleView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mChannelRecyclerView.setHasFixedSize(true);
        mChannelRecyclerView.setLayoutManager(mLayoutManager);
        mChannelRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mChannelRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mChannelRecyclerView));
    }

    protected void initializeChannelAdapter() {
        //baseChannelInjector = AbstractChannelLoader.createChannelInjector(mPaymentInfoHelper);
        mChannelRecyclerView.setAdapter(mChannelAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnSelectChannelEvent(SdkSelectedChannelMessage pMessage) {
        if (mMoreClick) {   //prevent so many click on channel.
            mMoreClick = false;
            //onSelectedChannel(baseChannelInjector.getChannelAtPosition(pMessage.position));
            new Handler().postDelayed(() -> mMoreClick = true, 1000);
        }
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
        Log.d(this, "onDestroy");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.trackUserCancel();
        }
        //user is summiting order
        if (!isInProgress()) {
            recycleActivity();
            return;
        }
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
        /*int[] forceChannels = mPaymentInfoHelper.getForceChannels();
        if (forceChannels != null && forceChannels.length >= 1) {
            //baseChannelInjector.filterForceChannel(forceChannels);
        }

        // don't have any channel now
        if (baseChannelInjector.isChannelEmpty()) {
            isUniqueChannel = true;
            String alertMessage = getAmountAlert();
            if (TextUtils.isEmpty(alertMessage)) {
                *//***
                 * this is withdraw link card and no mapped card.
                 * need remind user go to link card to can withdraw
                 *//*
                if (mPaymentInfoHelper.isWithDrawTrans()) {
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

        refreshChannelRecyclerView();*/
    }

    private void refreshChannelRecyclerView() {
        mChannelAdapter.notifyDataSetChanged();
        //isUniqueChannel = baseChannelInjector.isChannelUnique();
    }

    /***
     * prepare show channel and show header.
     */
    private synchronized void showPaymentChannel() {
        showProgress(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_process_view));
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
        /*baseChannelInjector.getChannels(new ZPWOnGetChannelListener() {
            @Override
            public void onGetChannelComplete() {
                showChannelListView();

                showProgress(false, GlobalData.getStringResource(RS.string.walletsdk_string_bar_title));
            }

            @Override
            public void onGetChannelError(String pError) {
                onExit(pError, true);
            }
        });*/

    }

    @Override
    protected void readyForPayment() {
        showProgress(true, GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        loadBankList(new Action1<BankConfigResponse>() {
            @Override
            public void call(BankConfigResponse bankConfigResponse) {
                showPaymentChannel();
                Log.d(this, "load bank list finish");
            }
        }, bankListException);
        Log.d(this, "ready for payment");
    }

    @Override
    protected void notifyUpVersionToApp(boolean pForceUpdate, String pVersion, String pMessage) {
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
        Log.d(this, "recycle activity");
        setEnableView(R.id.zpsdk_exit_ctl, false);
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.trackUserCancel();
        }
        finish();
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
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
        long amount = mPaymentInfoHelper.getAmount();
        if (baseChannelInjector.hasMinValueChannel() && amount < baseChannelInjector.getMinValueChannel()) {
            strAlert = String.format(GlobalData.getStringResource(RS.string.zpw_string_alert_min_amount_input),
                    StringUtil.formatVnCurrence(String.valueOf(baseChannelInjector.getMinValueChannel())));
        } else if (baseChannelInjector.hasMaxValueChannel() && amount > baseChannelInjector.getMaxValueChannel()) {
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
                mPaymentInfoHelper.setResult(PaymentStatus.LEVEL_UPGRADE_PASSWORD);
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
                                     GlobalData.getPaymentListener().onComplete();
                                 }
                                 finish();
                             }

                             @Override
                             public void onOKevent() {
                                 mPaymentInfoHelper.setResult(PaymentStatus.DIRECT_LINKCARD);
                                 if (GlobalData.getPaymentListener() != null) {
                                     GlobalData.getPaymentListener().onComplete();
                                 }
                                 finish();
                             }
                         }, GlobalData.getStringResource(RS.string.zpw_string_alert_linkcard_channel_withdraw), GlobalData.getStringResource(RS.string.dialog_linkcard_button),
                GlobalData.getStringResource(RS.string.dialog_close_button));
    }

    public void notifyToMerchant() {
        if (ErrorManager.needToTerminateTransaction(mPaymentInfoHelper.getStatus()))
            recycleActivity();
        else
            exitIfUniqueChannel();
    }

    public void exitIfUniqueChannel() {
        if (isUniqueChannel()) {
            recycleActivity();
        }
    }

    private void goToChannel(PaymentChannel pChannel) {
        //map card channel clicked
        if (!TextUtils.isEmpty(pChannel.f6no) && !TextUtils.isEmpty(pChannel.l4no)) {
            BaseMap mapBank = pChannel.isBankAccountMap ? new BankAccount() : new MapCard();
            mapBank.setLastNumber(pChannel.l4no);
            mapBank.setFirstNumber(pChannel.f6no);
            mapBank.bankcode = pChannel.bankcode;
            mPaymentInfoHelper.paymentInfo.setMapBank(mapBank);
            AdapterBase.existedMapCard = true;
        } else {
            mPaymentInfoHelper.paymentInfo.setMapBank(null);
            AdapterBase.existedMapCard = false;
        }
        //calculate fee and total amount order
        pChannel.calculateFee(mPaymentInfoHelper.getAmount());
        mPaymentInfoHelper.getOrder().populateFee(pChannel);
        ChannelProxy.get()
                //.setActivity(this)
                .setPaymentInfo(mPaymentInfoHelper)
                .setChannel(pChannel)
                .start();
        // TrackApptransidEvent choose pay method
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_ChoosePayMethod, ZPPaymentSteps.OrderStepResult_None, pChannel.pmcid);
        }
    }

    //item on listview is clicked
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public void onSelectedChannel(PaymentChannel pChannel) {
        if (pChannel == null) {
            return;
        }
        //bank is maintenance
        if (pChannel.isMaintenance()) {
            if (GlobalData.getCurrentBankFunction() == BankFunctionCode.PAY) {
                GlobalData.getPayBankFunction(pChannel);
            }
            showBankMaintenance(pChannel.bankcode);
            return;
        }
        if (!pChannel.isEnable() || !pChannel.isAllowByAmount() || !pChannel.isAllowByAmountAndFee()) {
            Log.d(this, "select channel not support", pChannel);
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

    public List<PaymentChannel> getChannelList() {
        if (baseChannelInjector != null) {
            //return baseChannelInjector.getChannelList();
        }
        return null;
    }

    public IMoveToChannel getMoveToChannelListener() {
        return mMoveToChannel;
    }
    //endregion
}
