package vn.com.zalopay.wallet.pay;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.interactor.IBankInteractor;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channellist.ChannelListFragment;
import vn.com.zalopay.wallet.ui.channellist.ChannelListPresenter;

/***
 * pre check before start payment channel
 */
public class ValidationActor extends SingletonBase {
    private PaymentChannel mChannel;
    private PaymentInfoHelper mPaymentInfoHelper;
    private IBankInteractor mBankInteractor;
    private WeakReference<ChannelListPresenter> mChannelListPresenter;
    private ZPWOnEventConfirmDialogListener mUpdateLevelListener = new ZPWOnEventConfirmDialogListener() {
        @Override
        public void onCancelEvent() {
            mChannelListPresenter.get().exitHasOneChannel();
        }

        @Override
        public void onOKevent() {
            if (mChannel.isBankAccount() && !BankAccountHelper.hasBankAccountOnCache(mPaymentInfoHelper.getUserInfo().zalopay_userid, CardType.PVCB)) {
                mPaymentInfoHelper.setResult(PaymentStatus.UPLEVEL_AND_LINK_BANKACCOUNT_AND_PAYMENT);
            } else {
                mPaymentInfoHelper.setResult(PaymentStatus.LEVEL_UPGRADE_PASSWORD);
            }
            try {
                getView().callbackThenTerminate();
            } catch (Exception e) {
                Timber.d(e.getMessage());
            }
        }
    };

    public ValidationActor(PaymentInfoHelper paymentInfoHelper, IBankInteractor iBankInteractor, ChannelListPresenter channelListPresenter) {
        this.mPaymentInfoHelper = paymentInfoHelper;
        this.mBankInteractor = iBankInteractor;
        this.mChannelListPresenter = new WeakReference<>(channelListPresenter);
    }

    private ChannelListFragment getView() throws Exception {
        return mChannelListPresenter.get().getViewOrThrow();
    }

    public boolean validate(PaymentChannel channel) throws Exception {
        if (channel == null) {
            return false;
        }
        mChannel = channel;
        //channel is maintenance
        if (mChannel.isMaintenance()) {
            if (GlobalData.shouldUpdateBankFuncbyPayType()) {
                GlobalData.updateBankFuncByPmc(mChannel);
            }
            int bankFunction = GlobalData.getCurrentBankFunction();
            BankConfig bankConfig = mBankInteractor.getBankConfig(mChannel.bankcode);
            if (bankConfig != null && bankConfig.isBankMaintenence(bankFunction)) {
                getView().showInfoDialog(bankConfig.getMaintenanceMessage(bankFunction));
                return false;
            }
        }
        if (!mChannel.meetPaymentCondition()) {
            Log.d(this, "select channel not support", mChannel);
            return false;
        }
        //check level for payment
        if (!mPaymentInfoHelper.userLevelValid()) {
            getView().showUpdateLevelDialog(
                    GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update),
                    getBtnCloseText(), mUpdateLevelListener);
            return false;
        }
        //check bank future
        if (!mChannel.isVersionSupport(SdkUtils.getAppVersion(GlobalData.getAppContext()))) {
            if (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) {
                BankConfig bankConfig = mBankInteractor.getBankConfig(mChannel.bankcode);
                if (bankConfig != null) {
                    String pMessage = GlobalData.getStringResource(RS.string.sdk_warning_version_support_payment);
                    pMessage = String.format(pMessage, bankConfig.getShortBankName());
                    showSupportBankVersionDialog(pMessage);
                }
                return false;
            } else if (!mChannel.isAtmChannel()) {
                String message = GlobalData.getStringResource(RS.string.sdk_warning_version_support_payment);
                showSupportBankVersionDialog(String.format(message, mChannel.pmcname));
                return false;
            }
        }

        //withdraw
        if (mPaymentInfoHelper.isWithDrawTrans()) {
            return true;
        }

        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        int transtype = mPaymentInfoHelper.getTranstype();
        String warningLevel = GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update);
        //validate in map table
        int iCheck = userInfo.getPermissionByChannelMap(channel.pmcid, transtype);
        if (iCheck == Constants.LEVELMAP_INVALID) {
            getView().showError(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error));
            return false;
        }
        if (iCheck == Constants.LEVELMAP_BAN && mChannel.isBankAccountMap()) {
            warningLevel = GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update_and_before_payby_bankaccount);
        } else if (iCheck == Constants.LEVELMAP_BAN && mChannel.isBankAccount()) {
            warningLevel = GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update_and_linkaccount_before_payment);
        }

        if (iCheck == Constants.LEVELMAP_BAN) {
            getView().showUpdateLevelDialog(warningLevel, getBtnCloseText(), mUpdateLevelListener);
            return false;
        }

        /***
         * user selected bank account channel
         * if user have no linked bank accoount, redirect him to link bank account page
         * if user have some link bank account, he need to select one to continue to payment
         */
        if (channel.isBankAccount() && !channel.isBankAccountMap) {
            //callback bankcode to app , app will direct user to link bank account to right that bank
            BankAccount dBankAccount = new BankAccount();
            dBankAccount.bankcode = CardType.PVCB;
            mPaymentInfoHelper.setMapBank(dBankAccount);
            mPaymentInfoHelper.setResult(PaymentStatus.DIRECT_LINK_ACCOUNT_AND_PAYMENT);
            getView().callbackThenTerminate();
            return false;
        }
        return true;
    }

    private void showSupportBankVersionDialog(String pMessage) {
        try {
            getView().showSupportBankVersionDialog(pMessage);
        } catch (Exception e) {
            Timber.d(e != null ? e.getMessage() : "Exception");
        }
    }

    private String getBtnCloseText() {
        String closeButtonText = GlobalData.getStringResource(RS.string.dialog_choose_again_button);
        if (mChannelListPresenter.get().isUniqueChannel()) {
            closeButtonText = GlobalData.getStringResource(RS.string.dialog_close_button);
        }
        return closeButtonText;
    }
}
