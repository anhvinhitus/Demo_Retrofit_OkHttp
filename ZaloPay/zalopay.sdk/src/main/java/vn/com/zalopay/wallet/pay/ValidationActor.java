package vn.com.zalopay.wallet.pay;

import android.content.Context;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.controller.SDKApplication;
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
    Context mContext;

    public ValidationActor(Context pContext, PaymentInfoHelper paymentInfoHelper, IBankInteractor iBankInteractor, ChannelListPresenter channelListPresenter) {
        this.mContext = pContext;
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
        if (!mChannel.isVersionSupport(SdkUtils.getAppVersion(SDKApplication.getContext())) && !mChannel.isNewAtmChannel()) {
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
