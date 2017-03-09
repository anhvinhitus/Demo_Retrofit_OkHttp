package vn.com.zalopay.wallet.business.behavior.view;

import android.content.Intent;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentGatewayActivity;

/***
 * pre check before start payment channel
 */
public class ChannelStartProcessor extends SingletonBase {
    private static ChannelStartProcessor _object;
    private PaymentGatewayActivity mOwnerActivity;
    private DPaymentChannelView mChannel;
    private ILoadBankListListener mLoadBankListListener = new ILoadBankListListener() {
        @Override
        public void onProcessing() {
            showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        }
        @Override
        public void onComplete() {

            if (!isBankMaintenance() && isBankSupport()) {
                startChannel();
            }

            showProgressBar(false, null);

        }
        @Override
        public void onError(String pMessage) {
            alertNetworking();
        }
    };

    public ChannelStartProcessor(PaymentGatewayActivity pOwnerActivity) {
        super();
        mOwnerActivity = pOwnerActivity;
    }

    public static ChannelStartProcessor getInstance(PaymentGatewayActivity pOwnerActivity) {

        if (ChannelStartProcessor._object == null)
            ChannelStartProcessor._object = new ChannelStartProcessor(pOwnerActivity);
        return ChannelStartProcessor._object;
    }

    protected PaymentGatewayActivity getActivity() {
        return mOwnerActivity;
    }

    public DPaymentChannelView getChannel() {
        return mChannel;
    }

    public ChannelStartProcessor setChannel(DPaymentChannelView pChannel) {
        mChannel = pChannel;

        return this;
    }

    /***
     * start payment channel
     */
    public void startGateWay() {
        // Lost connection,show alert dialog
        if (getActivity() != null && !ConnectionUtil.isOnline(getActivity())) {
            if (getActivity() != null && !getActivity().isFinishing())
                getActivity().askToOpenSettingNetwoking(null);

            return;
        }

        try {
            //check level for payment
            if (!getActivity().checkUserLevelValid()) {
                confirmUpgradeLevel(GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update));
                return;
            }
            //withdraw
            if (GlobalData.isWithDrawChannel()) {
                startChannel();
                return;
            }

            //validate in maptable
            int iCheck = GlobalData.checkPermissionByChannelMap(mChannel.pmcid);
            if (iCheck == Constants.LEVELMAP_INVALID) {
                getActivity().showWarningDialog(new ZPWOnEventDialogListener() {
                    @Override
                    public void onOKevent() {
                        getActivity().recycleGateway();
                    }
                }, GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error));

                return;
            } else if (iCheck == Constants.LEVELMAP_BAN && getChannel().isBankAccountMap()) {
                confirmUpgradeLevel(GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update_and_before_payby_bankaccount));
                return;
            } else if (iCheck == Constants.LEVELMAP_BAN && getChannel().isBankAccount()) {
                confirmUpgradeLevel(GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update_and_linkaccount_before_payment));
                return;
            } else if (iCheck == Constants.LEVELMAP_BAN) {
                confirmUpgradeLevel(GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update));
                return;
            }

            /***
             * user selected bank account channel
             * if user have no linked bank accoount, redirect him to link bank account page
             * if user have some link bank account, he need to select one to continue to payment
             */
            if (mChannel != null && mChannel.isBankAccount() && !mChannel.isBankAccountMap) {
                //use don't have vietcombank link
                if (!BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank)) && getActivity() != null) {
                    //callback bankcode to app , app will direct user to link bank account to right that bank
                    DBankAccount dBankAccount = new DBankAccount();
                    dBankAccount.bankcode = GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank);
                    GlobalData.getPaymentInfo().mapBank = dBankAccount;

                    GlobalData.setResultNeedToLinkAccountBeforePayment();
                    getActivity().recycleActivity();
                }
                //use has an bank account list
                else if (getActivity() != null) {
                    getActivity().showSelectionBankAccountDialog();
                }
                return;
            }
            //reload bank account
            if (GlobalData.isMapCardChannel() || GlobalData.isMapBankAccountChannel()) {
                try {
                    BankLoader.loadBankList(mLoadBankListListener);
                } catch (Exception e) {
                    alertNetworking();

                    Log.e(this, e);
                }
            } else {
                startChannel();
            }

        } catch (Exception ex) {
            Log.e(this, ex);

            getActivity().showWarningDialog(new ZPWOnEventDialogListener() {
                @Override
                public void onOKevent() {
                    getActivity().recycleGateway();
                }
            }, GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error));
            return;
        }
    }

    /**
     * Bank Maintenance
     *
     * @return
     */
    private boolean isBankMaintenance() {
        if (GlobalData.getCurrentBankFunction() == EBankFunction.PAY) {
            GlobalData.getBankFunctionPay();
        }
        return getActivity().showBankMaintenance(GlobalData.getPaymentInfo().mapBank.bankcode);
    }

    /**
     * Check bank support
     *
     * @return
     */
    private boolean isBankSupport() {
        return getActivity().showBankSupport(GlobalData.getPaymentInfo().mapBank.bankcode);
    }
    /**
     * Show dialog confirm upgrade level
     */
    private void confirmUpgradeLevel(final String pMessage) {
        String closeButtonText = GlobalData.getStringResource(RS.string.dialog_choose_again_button);
        if (getActivity().isUniqueChannel()) {
            closeButtonText = GlobalData.getStringResource(RS.string.dialog_close_button);
        }

        getActivity().showNoticeDialog(new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                if (getActivity() != null)
                    getActivity().exitIfUniqueChannel();
            }

            @Override
            public void onOKevent() {
                if (mChannel.isBankAccount() && !BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank))) {
                    GlobalData.setResultUpLevelLinkAccountAndPayment();
                } else {
                    GlobalData.setResultUpgrade();
                }
                getActivity().notifyToMerchant();
            }
        }, pMessage, GlobalData.getStringResource(RS.string.dialog_upgrade_button), closeButtonText);
    }

    /**
     * networking error dialog
     */
    private void alertNetworking() {
        showProgressBar(false, null);
        BasePaymentActivity activity = (BasePaymentActivity) BasePaymentActivity.getCurrentActivity();
        if (activity != null && activity instanceof PaymentGatewayActivity && !activity.isFinishing()) {
            activity.showWarningDialog(new ZPWOnEventDialogListener() {
                @Override
                public void onOKevent() {
                    getActivity().recycleGateway();
                }
            }, GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
        }

    }

    private void startChannel() {
        Intent intent = new Intent(GlobalData.getAppContext(), PaymentChannelActivity.class);
        intent.putExtra(GlobalData.getStringResource(RS.string.zingpaysdk_intent_key_channel), String.valueOf(mChannel.pmcid));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().startActivity(intent);
    }

    private void showProgressBar(boolean pIsShow, String pStatusMessage) {
        BasePaymentActivity activity = (BasePaymentActivity) BasePaymentActivity.getCurrentActivity();
        if (activity != null && activity instanceof PaymentGatewayActivity) {
            if (!pIsShow)
                activity.showProgress(pIsShow, null);
            else
                activity.showProgress(pIsShow, pStatusMessage);

        }
    }
}
