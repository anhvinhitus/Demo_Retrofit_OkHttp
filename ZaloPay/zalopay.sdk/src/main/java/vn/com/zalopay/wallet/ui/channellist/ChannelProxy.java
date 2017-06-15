package vn.com.zalopay.wallet.ui.channellist;

import android.app.Activity;
import android.content.Intent;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.pinlayout.interfaces.IFPinCallBack;
import com.zalopay.ui.widget.pinlayout.managers.PinManager;

import java.lang.ref.WeakReference;

import rx.functions.Action1;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentGatewayActivity;

/***
 * pre check before start payment channel
 */
public class ChannelProxy extends SingletonBase {
    private static ChannelProxy _object;
    private PinManager mPassword;
    private PaymentChannel mChannel;
    private PaymentInfoHelper mPaymentInfoHelper;
    private IBank mBankInteractor;
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
            getView().callbackThenterminate();
        }
    };
    private IFPinCallBack mIFIfPinCallBack = new IFPinCallBack() {
        @Override
        public void onError(String pError) {

        }

        @Override
        public void onCheckedFingerPrint(boolean pChecked) {
            Log.d(this, "on changed check", pChecked);
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onComplete(String pHashPin) {
        }
    };
    private Action1<BankConfigResponse> bankListSubscriber = new Action1<BankConfigResponse>() {
        @Override
        public void call(BankConfigResponse bankConfigResponse) {
            String bankCode = mPaymentInfoHelper.getMapBank().bankcode;
            if (!isBankMaintenance(bankCode) && isBankSupport(bankCode)) {
                startChannel();
            }
            getView().hideLoading();
        }
    };

    public ChannelProxy() {
        super();
        mBankInteractor = SDKApplication.getApplicationComponent().bankListInteractor();
    }

    public static ChannelProxy get() {
        if (ChannelProxy._object == null) {
            ChannelProxy._object = new ChannelProxy();
        }
        return ChannelProxy._object;
    }

    public PaymentChannel getChannel() {
        return mChannel;
    }

    public ChannelProxy setChannel(PaymentChannel pChannel) {
        mChannel = pChannel;
        return this;
    }

    public ChannelListFragment getView() {
        return mChannelListPresenter.get() != null ? mChannelListPresenter.get().getViewOrThrow() : null;
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

    public boolean validateChannel(PaymentChannel channel) {
        //channel is maintenance
        if (channel.isMaintenance()) {
            if (GlobalData.getCurrentBankFunction() == BankFunctionCode.PAY) {
                GlobalData.getPayBankFunction(channel);
            }
            int bankFunction = GlobalData.getCurrentBankFunction();
            BankConfig bankConfig = mBankInteractor.getBankConfig(channel.bankcode);
            if (bankConfig != null && bankConfig.isBankMaintenence(bankFunction)) {
                getView().showInfoDialog(bankConfig.getMaintenanceMessage(bankFunction));
                return false;
            }
        }
        if (!channel.isEnable() || !channel.isAllowByAmount() || !channel.isAllowByAmountAndFee()) {
            Log.d(this, "select channel not support", channel);
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
        if (!channel.isVersionSupport(SdkUtils.getAppVersion(GlobalData.getAppContext()))) {
            if (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) {
                BankConfig bankConfig = mBankInteractor.getBankConfig(channel.bankcode);
                if (bankConfig != null) {
                    String pMessage = GlobalData.getStringResource(RS.string.sdk_warning_version_support_payment);
                    pMessage = String.format(pMessage, bankConfig.getShortBankName());
                    showSupportBankVersionDialog(pMessage);
                }
                return false;
            } else if (!channel.isAtmChannel()) {
                String message = GlobalData.getStringResource(RS.string.sdk_warning_version_support_payment);
                showSupportBankVersionDialog(String.format(message, channel.pmcname));
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
        if (iCheck == Constants.LEVELMAP_BAN && getChannel().isBankAccountMap()) {
            warningLevel = GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update_and_before_payby_bankaccount);
        } else if (iCheck == Constants.LEVELMAP_BAN && getChannel().isBankAccount()) {
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
        if (channel != null && channel.isBankAccount() && !channel.isBankAccountMap) {
            //use don't have vietcombank link
            if (!BankAccountHelper.hasBankAccountOnCache(userInfo.zalopay_userid, CardType.PVCB)) {
                //callback bankcode to app , app will direct user to link bank account to right that bank
                BankAccount dBankAccount = new BankAccount();
                dBankAccount.bankcode = CardType.PVCB;
                mPaymentInfoHelper.setMapBank(dBankAccount);
                mPaymentInfoHelper.setResult(PaymentStatus.DIRECT_LINK_ACCOUNT_AND_PAYMENT);
                getView().callbackThenterminate();
            }
            //use has an bank account list
            else {
                getView().showSelectionBankAccountDialog();
            }
            return false;
        }
        return true;
    }

    /***
     * start payment channel
     */
    public void start() {
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
            AdapterBase.existedMapCard = true;
        } else {
            mPaymentInfoHelper.paymentInfo.setMapBank(null);
            AdapterBase.existedMapCard = false;
        }
        //calculate fee and total amount order
        mChannel.calculateFee(mPaymentInfoHelper.getAmount());
        mPaymentInfoHelper.getOrder().populateFee(mChannel);
        if (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) {
            getView().showLoading(GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
            mChannelListPresenter.get().loadBankList(bankListSubscriber, mChannelListPresenter.get().mBankListException);
        }else{
            startChannel();
        }
    }

    private void showSupportBankVersionDialog(String pMessage) {
        getView().showSupportBankVersionDialog(pMessage);
    }

    /**
     * Bank Maintenance
     *
     * @return
     */
    private boolean isBankMaintenance(String pBankCode) {
        if (GlobalData.getCurrentBankFunction() == BankFunctionCode.PAY) {
            GlobalData.getBankFunctionPay(mPaymentInfoHelper);
        }

        int bankFunction = GlobalData.getCurrentBankFunction();
        BankConfig bankConfig = mBankInteractor.getBankConfig(pBankCode);
        if (bankConfig != null && bankConfig.isBankMaintenence(bankFunction)) {
            getView().showInfoDialog(bankConfig.getMaintenanceMessage(bankFunction));
            return true;
        }
        return false;
    }

    /**
     * Check bank support
     *
     * @return
     */
    private boolean isBankSupport(String pBankCode) {
        BankConfig bankConfig = SDKApplication.getApplicationComponent().bankListInteractor().getBankConfig(pBankCode);
        if (bankConfig == null || !bankConfig.isActive()) {
            String message = GlobalData.getStringResource(RS.string.zpw_string_bank_not_support);
            getView().showInfoDialog(message);
            return false;
        }
        return true;
    }

    private String getBtnCloseText() {
        String closeButtonText = GlobalData.getStringResource(RS.string.dialog_choose_again_button);
        if (PaymentGatewayActivity.isUniqueChannel()) {
            closeButtonText = GlobalData.getStringResource(RS.string.dialog_close_button);
        }
        return closeButtonText;
    }

    private void startChannel() {
        //password flow
        if (mChannel.isZaloPayChannel() || mChannel.isMapCardChannel() || mChannel.isBankAccountMap()) {
            String logo_path = ResourceManager.getAbsoluteImagePath(mChannel.channel_icon);
            mPassword = new PinManager(getView().getActivity(), mChannel.pmcname, logo_path, mIFIfPinCallBack);
            mPassword.showPinView();
            return;
        }
        //input flow
        Intent intent = new Intent(GlobalData.getAppContext(), PaymentChannelActivity.class);
        intent.putExtra(PaymentChannelActivity.PMC_CONFIG_EXTRA, mChannel);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Activity activity = getView().getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.startActivity(intent);
        }
    }
}
