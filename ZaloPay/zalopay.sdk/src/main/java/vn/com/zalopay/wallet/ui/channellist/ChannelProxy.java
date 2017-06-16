package vn.com.zalopay.wallet.ui.channellist;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.text.TextUtils;

import com.zalopay.ui.widget.password.interfaces.IPinCallBack;
import com.zalopay.ui.widget.password.managers.PasswordManager;

import java.lang.ref.WeakReference;

import rx.functions.Action1;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

import static vn.com.zalopay.wallet.ui.channellist.ChannelListPresenter.REQUEST_CODE;

/***
 * pre check before start payment channel
 */
public class ChannelProxy extends SingletonBase {
    private static ChannelProxy _object;
    protected DialogFragment mFingerPrintDialog = null;
    private ChannelPreValidation mChannelPreValidation;
    private PasswordManager mPassword;
    private PaymentChannel mChannel;
    private PaymentInfoHelper mPaymentInfoHelper;
    private IBank mBankInteractor;
    private WeakReference<ChannelListPresenter> mChannelListPresenter;
    private IPinCallBack mPasswordCallback = new IPinCallBack() {
        @Override
        public void onError(String pError) {
            Log.e(this, pError);
        }

        @Override
        public void onCheckedFingerPrint(boolean pChecked) {
            Log.d(this, "on changed check", pChecked);
        }

        @Override
        public void onCancel() {
            Log.d(this, "user canceled password");
        }

        @Override
        public void onComplete(String pHashPin) {
            Log.d(this, "password", pHashPin);
            if (!TextUtils.isEmpty(pHashPin)) {
                Log.d(this, "start submit trans pw", pHashPin);
            } else {
                Log.e(this, "empty password");
            }
        }
    };
    private final IFPCallback mFingerPrintCallback = new IFPCallback() {
        @Override
        public void onError(FPError pError) {
            dismissFingerPrintDialog();
            try {
                getView().showInfoDialog(GlobalData.getStringResource(RS.string.zpw_error_authen_pin));
            } catch (Exception e) {
                Log.d(this, e);
            }
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onComplete(String pHashPin) {
            dismissFingerPrintDialog();
            //user don't setting use fingerprint for payment
            if (TextUtils.isEmpty(pHashPin)) {
                Activity activity = BaseActivity.getCurrentActivity();
                if (activity != null && !activity.isFinishing()) {
                    showPassword(activity);
                }
            } else {
                //submit password
                Log.d(this, "start submit trans pw", pHashPin);
            }
        }
    };
    private Action1<BankConfigResponse> bankListSubscriber = new Action1<BankConfigResponse>() {
        @Override
        public void call(BankConfigResponse bankConfigResponse) {
            String bankCode = mPaymentInfoHelper.getMapBank().bankcode;
            if (!isBankMaintenance(bankCode) && isBankSupport(bankCode)) {
                startFlow();
            }
            try {
                getView().hideLoading();
            } catch (Exception e) {
                Log.d(this, e);
            }
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

    public ChannelProxy setBankInteractor(IBank pBankInteractor) {
        mBankInteractor = pBankInteractor;
        return this;
    }

    public ChannelProxy setChannel(PaymentChannel pChannel) {
        mChannel = pChannel;
        return this;
    }

    public ChannelListFragment getView() throws Exception {
        return mChannelListPresenter.get().getViewOrThrow();
    }

    public ChannelProxy setChannelListPresenter(ChannelListPresenter presenter) {
        mChannelListPresenter = new WeakReference<>(presenter);
        return this;
    }

    public PaymentInfoHelper getPaymentInfoHelper() {
        return mPaymentInfoHelper;
    }

    public ChannelProxy setPaymentInfo(PaymentInfoHelper paymentInfoHelper) {
        mPaymentInfoHelper = paymentInfoHelper;
        return this;
    }

    public boolean validate(PaymentChannel channel) {
        if (mChannelPreValidation == null) {
            mChannelPreValidation = new ChannelPreValidation(mPaymentInfoHelper, mBankInteractor, mChannelListPresenter.get());
        }
        try {
            return mChannelPreValidation.validate(channel);
        } catch (Exception e) {
            Log.d(this, e);
        }
        return false;
    }

    /***
     * start payment channel
     */
    public void start() throws Exception {
        Log.d(this, "start payment channel", mChannel);
        Activity activity = BaseActivity.getCurrentActivity();
        if (!(activity instanceof BaseActivity)) {
            Log.e(this, "channel list activity is not valid");
            return;
        }
        //map card channel clicked
        if (mChannel.isMapValid()) {
            BaseMap mapBank = mChannel.isBankAccountMap ? new BankAccount() : new MapCard();
            mapBank.setLastNumber(mChannel.l4no);
            mapBank.setFirstNumber(mChannel.f6no);
            mapBank.bankcode = mChannel.bankcode;
            mPaymentInfoHelper.paymentInfo.setMapBank(mapBank);
        } else {
            mPaymentInfoHelper.paymentInfo.setMapBank(null);
        }
        mPaymentInfoHelper.getOrder().populateFee(mChannel);
        if (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) {
            getView().showLoading(GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
            if (mChannelListPresenter.get() != null) {
                mChannelListPresenter.get().loadBankList(bankListSubscriber, mChannelListPresenter.get().mBankListException);
            }
        } else {
            startFlow();
        }
    }

    private boolean isBankMaintenance(String pBankCode) {
        if (GlobalData.getCurrentBankFunction() == BankFunctionCode.PAY) {
            GlobalData.getBankFunctionPay(mPaymentInfoHelper);
        }

        int bankFunction = GlobalData.getCurrentBankFunction();
        BankConfig bankConfig = mBankInteractor.getBankConfig(pBankCode);
        if (bankConfig != null && bankConfig.isBankMaintenence(bankFunction)) {
            try {
                getView().showInfoDialog(bankConfig.getMaintenanceMessage(bankFunction));
                return true;
            } catch (Exception e) {
                Log.d(this, e);
            }
        }
        return false;
    }

    private boolean isBankSupport(String pBankCode) {
        BankConfig bankConfig = SDKApplication.getApplicationComponent().bankListInteractor().getBankConfig(pBankCode);
        if (bankConfig == null || !bankConfig.isActive()) {
            String message = GlobalData.getStringResource(RS.string.zpw_string_bank_not_support);
            try {
                getView().showInfoDialog(message);
            } catch (Exception e) {
                Log.d(this, e);
            }
            return false;
        }
        return true;
    }

    private void startFlow() {
        Activity activity = BaseActivity.getCurrentActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        //password flow
        if (mChannel.isZaloPayChannel() || mChannel.isMapCardChannel() || mChannel.isBankAccountMap()) {
            if (TransactionHelper.needUserPasswordPayment(mChannel, mPaymentInfoHelper.getOrder())) {
                startPasswordFlow(activity);
            } else {
                //
            }
            return;
        }
        //input flow
        Intent intent = new Intent(GlobalData.getAppContext(), PaymentChannelActivity.class);
        intent.putExtra(PaymentChannelActivity.PMC_CONFIG, mChannel);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        try {
            getView().startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            Log.d(this, e);
        }
    }

    private void startPasswordFlow(Activity pActivity) {
        try {
            if (PaymentFingerPrint.isDeviceSupportFingerPrint() && PaymentFingerPrint.isAllowFingerPrintFeature()) {
                showFingerPrint(pActivity);
            } else {
                showPassword(pActivity);
            }
        } catch (Exception ex) {
            showPassword(pActivity);
            Log.e(this, ex);
        }
    }

    private void showPassword(Activity pActivity) {
        String logo_path = ResourceManager.getAbsoluteImagePath(mChannel.channel_icon);
        if (mPassword == null) {
            mPassword = new PasswordManager(pActivity, mChannel.pmcname, logo_path, mPasswordCallback);
        } else {
            mPassword.setContent(mChannel.pmcname, logo_path);
        }
        mPassword.showPinView();
    }

    private void showFingerPrint(Activity pActivity) throws Exception {
        mFingerPrintDialog = PaymentFingerPrint.shared().getDialogFingerprintAuthentication(pActivity, mFingerPrintCallback);
        if (mFingerPrintDialog != null) {
            mFingerPrintDialog.show(pActivity.getFragmentManager(), null);
        } else {
            startPasswordFlow(pActivity);
            Log.d(this, "use password instead of use fingerprint");
        }
    }

    private void dismissFingerPrintDialog() {
        if (mFingerPrintDialog != null && !mFingerPrintDialog.isDetached()) {
            mFingerPrintDialog.dismiss();
            mFingerPrintDialog = null;
            Log.d(this, "dismiss dialog fingerprint");
        }
    }
}
