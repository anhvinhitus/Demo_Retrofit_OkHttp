package vn.com.zalopay.wallet.pay;

import android.content.Context;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.entity.bank.BankConfig;
import vn.com.zalopay.wallet.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.repository.bank.BankStore;
import vn.com.zalopay.wallet.ui.channellist.ChannelListFragment;
import vn.com.zalopay.wallet.ui.channellist.ChannelListPresenter;

/***
 * pre check before start payment channel
 */
public class ValidationActor extends SingletonBase {
    PaymentChannel mChannel;
    PaymentInfoHelper mPaymentInfoHelper;
    WeakReference<ChannelListPresenter> mChannelListPresenter;
    Context mContext;
    private BankStore.Interactor mBankInteractor;

    public ValidationActor(Context pContext, PaymentInfoHelper paymentInfoHelper, BankStore.Interactor iBankInteractor, ChannelListPresenter channelListPresenter) {
        this.mContext = pContext;
        this.mPaymentInfoHelper = paymentInfoHelper;
        this.mBankInteractor = iBankInteractor;
        this.mChannelListPresenter = new WeakReference<>(channelListPresenter);
    }

    ChannelListFragment getView() throws Exception {
        return mChannelListPresenter.get().getViewOrThrow();
    }

    private void warningSupportVersion() {
        if (mChannel == null) {
            Timber.d("Channel null - skip warningSupportVersion");
            return;
        }
        if (mChannel.isMapValid()) {
            BankConfig bankConfig = mBankInteractor.getBankConfig(mChannel.bankcode);
            if (bankConfig != null) {
                String pMessage = mContext.getResources().getString(R.string.sdk_warning_version_support_payment);
                pMessage = String.format(pMessage, bankConfig.getShortBankName());
                showSupportBankVersionDialog(pMessage);
                return;
            }
        }
        String message = mContext.getResources().getString(R.string.sdk_warning_version_support_payment);
        showSupportBankVersionDialog(String.format(message, mChannel.pmcname));
    }

    public boolean validate(PaymentChannel channel) throws Exception {
        if (channel == null) {
            Timber.d("Channel null - skip validate");
            return false;
        }
        mChannel = channel;
        //channel is maintenance
        if (mChannel.isMaintenance()) {
            int bankFunction = GlobalData.updateBankFuncByChannel(mChannel);
            BankConfig bankConfig = mBankInteractor.getBankConfig(mChannel.bankcode);
            if (bankConfig != null && bankConfig.isBankMaintenence(bankFunction)) {
                getView().showInfoDialog(bankConfig.getMaintenanceMessage(bankFunction));
                return false;
            }
        }
        //check bank future
        if (!mChannel.isVersionSupport(SdkUtils.getAppVersion(SDKApplication.getContext()))
                && !mChannel.isNewAtmChannel()) {
            warningSupportVersion();
            return false;
        }
        if (!mChannel.meetPaymentCondition()) {
            Timber.d("select channel not meet condition %s", GsonUtils.toJsonString(mChannel));
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
