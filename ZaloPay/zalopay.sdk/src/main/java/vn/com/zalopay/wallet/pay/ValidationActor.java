package vn.com.zalopay.wallet.pay;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.CardType;
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
    PaymentChannel mChannel;
    PaymentInfoHelper mPaymentInfoHelper;
    WeakReference<ChannelListPresenter> mChannelListPresenter;
    private IBankInteractor mBankInteractor;
    private ZPWOnEventConfirmDialogListener mUpdateLevelListener = new ZPWOnEventConfirmDialogListener() {
        @Override
        public void onCancelEvent() {
            mChannelListPresenter.get().exitHasOneChannel();
        }

        @Override
        public void onOKEvent() {
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

    ChannelListFragment getView() throws Exception {
        return mChannelListPresenter.get().getViewOrThrow();
    }

    private void warningSupportVersion() {
        if (mChannel.isMapValid()) {
            BankConfig bankConfig = mBankInteractor.getBankConfig(mChannel.bankcode);
            if (bankConfig != null) {
                String pMessage = GlobalData.getAppContext().getResources().getString(R.string.sdk_warning_version_support_payment);
                pMessage = String.format(pMessage, bankConfig.getShortBankName());
                showSupportBankVersionDialog(pMessage);
                return;
            }
        }
        String message = GlobalData.getAppContext().getResources().getString(R.string.sdk_warning_version_support_payment);
        showSupportBankVersionDialog(String.format(message, mChannel.pmcname));
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
        //check bank future
        if (!mChannel.isVersionSupport(SdkUtils.getAppVersion(GlobalData.getAppContext())) && !mChannel.isNewAtmChannel()) {
            warningSupportVersion();
        }
        if (!mChannel.meetPaymentCondition()) {
            Log.d(this, "select channel not support", mChannel);
            return false;
        }
        return true;
    }

    private void showSupportBankVersionDialog(String pMessage) {
        try {
            getView().showSupportBankVersionDialog(pMessage);
        } catch (Exception e) {
            Timber.d(e, "Exception show bank support version");
        }
    }
}
