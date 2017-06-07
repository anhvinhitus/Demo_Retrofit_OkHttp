package vn.com.zalopay.wallet.business.behavior.view;

import android.content.Intent;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.PlayStoreUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentGatewayActivity;

/***
 * pre check before start payment channel
 */
public class ChannelProxy extends SingletonBase {
    private static ChannelProxy _object;
    private PaymentGatewayActivity mOwnerActivity;
    private PaymentChannel mChannel;
    private PaymentInfoHelper mPaymentInfoHelper;
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

    public ChannelProxy() {
        super();
    }

    public static ChannelProxy get() {
        if (ChannelProxy._object == null) {
            ChannelProxy._object = new ChannelProxy();
        }
        return ChannelProxy._object;
    }

    protected PaymentGatewayActivity getActivity() {
        return mOwnerActivity;
    }

    public ChannelProxy setActivity(PaymentGatewayActivity paymentGatewayActivity) {
        mOwnerActivity = paymentGatewayActivity;
        return this;
    }

    public PaymentChannel getChannel() {
        return mChannel;
    }

    public ChannelProxy setChannel(PaymentChannel pChannel) {
        mChannel = pChannel;
        return this;
    }

    public PaymentInfoHelper getPaymentInfoHelper() {
        return mPaymentInfoHelper;
    }

    public ChannelProxy setPaymentInfo(PaymentInfoHelper paymentInfoHelper) {
        mPaymentInfoHelper = paymentInfoHelper;
        return this;
    }

    /***
     * start payment channel
     */
    public void start() {
        Log.d(this, "user selected channel for payment", mChannel);
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
            //check bank future
            if (!mChannel.isVersionSupport(SdkUtils.getAppVersion(GlobalData.getAppContext()))) {
                if (mPaymentInfoHelper.isMapCardChannel() || mPaymentInfoHelper.isMapBankAccountChannel()) {
                    BankConfig bankConfig = BankLoader.getInstance().getBankByBankCode(mChannel.bankcode);
                    if (bankConfig != null) {
                        String pMessage = GlobalData.getStringResource(RS.string.sdk_warning_version_support_payment);
                        pMessage = String.format(pMessage, bankConfig.getShortBankName());
                        showSupportBankVersionDialog(pMessage, mChannel.minappversion);
                        return;
                    }
                } else if (!mChannel.isAtmChannel()) {
                    String message = GlobalData.getStringResource(RS.string.sdk_warning_version_support_payment);
                    showSupportBankVersionDialog(String.format(message, mChannel.pmcname), mChannel.minappversion);
                    return;
                }
            }
            //withdraw
            if (mPaymentInfoHelper.isWithDrawChannel()) {
                startChannel();
                return;
            }
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            int transtype = mPaymentInfoHelper.getTranstype();
            //validate in maptable
            int iCheck = userInfo.getPermissionByChannelMap(mChannel.pmcid, transtype);
            if (iCheck == Constants.LEVELMAP_INVALID) {
                getActivity().showWarningDialog(() -> getActivity().recycleGateway(), GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error));
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
                if (!BankAccountHelper.hasBankAccountOnCache(userInfo.zalopay_userid, CardType.PVCB) && getActivity() != null) {
                    //callback bankcode to app , app will direct user to link bank account to right that bank
                    BankAccount dBankAccount = new BankAccount();
                    dBankAccount.bankcode = CardType.PVCB;
                    mPaymentInfoHelper.setMapBank(dBankAccount);
                    mPaymentInfoHelper.setResult(PaymentStatus.DIRECT_LINK_ACCOUNT_AND_PAYMENT);
                    getActivity().recycleActivity();
                }
                //use has an bank account list
                else if (getActivity() != null) {
                    getActivity().showSelectionBankAccountDialog();
                }
                return;
            }

            if (mPaymentInfoHelper.isMapCardChannel() || mPaymentInfoHelper.isMapBankAccountChannel()) {
                BankLoader.loadBankList(mLoadBankListListener);//reload bank list
            } else {
                startChannel();
            }

        } catch (Exception ex) {
            Log.e(this, ex);
            getActivity().showWarningDialog(() -> getActivity().recycleGateway(), GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error));
            return;
        }
    }

    private void showSupportBankVersionDialog(String pMessage, String pMinVersion) {
        getActivity().showConfirmDialog(new ZPWOnEventConfirmDialogListener() {
                                            @Override
                                            public void onCancelEvent() {
                                            }

                                            @Override
                                            public void onOKevent() {
                                                PlayStoreUtils.openPlayStoreForUpdate(GlobalData.getMerchantActivity(), BuildConfig.PACKAGE_IN_PLAY_STORE, "Zalo Pay", "force-app-update", "bank-future");
                                                getActivity().recycleActivity();
                                            }
                                        }, pMessage,
                GlobalData.getStringResource(RS.string.dialog_update_versionapp_button), GlobalData.getStringResource(RS.string.dialog_choose_again_button));
    }

    /**
     * Bank Maintenance
     *
     * @return
     */
    private boolean isBankMaintenance() {
        if (GlobalData.getCurrentBankFunction() == BankFunctionCode.PAY) {
            GlobalData.getBankFunctionPay(mPaymentInfoHelper);
        }
        return getActivity().showBankMaintenance(mPaymentInfoHelper.getMapBank().bankcode);
    }

    /**
     * Check bank support
     *
     * @return
     */
    private boolean isBankSupport() {
        return getActivity().showBankSupport(mPaymentInfoHelper.getMapBank().bankcode);
    }

    /**
     * Show dialog confirm upgrade level
     */
    private void confirmUpgradeLevel(final String pMessage) {
        String closeButtonText = GlobalData.getStringResource(RS.string.dialog_choose_again_button);
        if (PaymentGatewayActivity.isUniqueChannel()) {
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
                if (mChannel.isBankAccount() && !BankAccountHelper.hasBankAccountOnCache(mPaymentInfoHelper.getUserInfo().zalopay_userid, CardType.PVCB)) {
                    mPaymentInfoHelper.setResult(PaymentStatus.UPLEVEL_AND_LINK_BANKACCOUNT_AND_PAYMENT);
                } else {
                    mPaymentInfoHelper.setResult(PaymentStatus.LEVEL_UPGRADE_PASSWORD);
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
            activity.showWarningDialog(() -> getActivity().recycleGateway(), GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
        }

    }

    private void startChannel() {
        Intent intent = new Intent(GlobalData.getAppContext(), PaymentChannelActivity.class);
        intent.putExtra(PaymentChannelActivity.PMC_CONFIG_EXTRA, mChannel);
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
