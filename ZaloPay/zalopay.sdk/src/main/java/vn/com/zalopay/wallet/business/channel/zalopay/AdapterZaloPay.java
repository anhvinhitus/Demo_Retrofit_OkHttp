package vn.com.zalopay.wallet.business.channel.zalopay;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterZaloPay extends AdapterBase {
    public AdapterZaloPay(PaymentChannelActivity pOwnerActivity) throws Exception {
        super(pOwnerActivity);

        mLayoutId = RS.layout.screen__zalopay;

        checkBalanceAndSetPage();
    }

    private void checkBalanceAndSetPage() {
        if (GlobalData.getBalance() >= (GlobalData.orderAmountTotal)) {
            mPageCode = PAGE_CONFIRM;
        } else {
            mPageCode = PAGE_BALANCE_ERROR;
        }
    }

    @Override
    public DPaymentChannel getChannelConfig() throws Exception {
        return GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(), DPaymentChannel.class);
    }

    @Override
    public void init() {
        getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zingpaysdk_pmc_name_zalopay));

        showFee();
    }

    @Override
    public String getChannelID() {
        if (mConfig != null)
            return String.valueOf(mConfig.pmcid);
        return GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay);
    }

    @Override
    public boolean isFinalStep() {
        if (mPageCode.equals(PAGE_CONFIRM))
            return true;
        return false;
    }

    @Override
    public void autoFillOtp(String pSender, String pOtp) {

    }

    @Override
    public void moveToConfirmScreen() {

        try {
            super.moveToConfirmScreen();
        } catch (Exception e) {
            Log.e(this, e);
        }

        checkBalanceAndSetPage();

        getActivity().renderByResource();

        setBalanceView(mConfig);

        getActivity().showConfirmView(true, true, mConfig);

        getActivity().setToolBarTitle();

        getActivity().enableSubmitBtn(true);

    }

    /***
     * if this is redpackage,then close sdk
     *
     * @return
     */
    @Override
    public boolean processResultForRedPackage() {
        if (GlobalData.isRedPacketChannel()) {
            onClickSubmission();
            return true;
        }

        return false;
    }

    public void setBalanceView(DPaymentChannel pConfig) {
        getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_zalopay_wallet_method_name));
        getActivity().renderPaymentBalanceContent(pConfig);
    }

    @Override
    public void onProcessPhrase() {
        try {
            if (isRequirePinPharse()) {
                if (!TextUtils.isEmpty(GlobalData.getTransactionPin()))
                    startSubmitTransaction();

                return;
            }

            if (isBalanceErrorPharse()) {
                GlobalData.setResultMoneyNotEnough();
                terminate(GlobalData.getStringResource(RS.string.zpw_string_not_enough_money_wallet), false);
                return;
            }

            startSubmitTransaction();

        } catch (Exception e) {
            throw e;
        }
    }
}
