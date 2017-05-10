package vn.com.zalopay.wallet.business.channel.zalopay;

import android.text.TextUtils;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterZaloPay extends AdapterBase {
    public AdapterZaloPay(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType);
        mLayoutId = RS.layout.screen__zalopay;
        checkBalanceAndSetPage();
    }

    private void checkBalanceAndSetPage() {
        mPageCode = (GlobalData.getBalance() >= (GlobalData.orderAmountTotal)) ? PAGE_CONFIRM : PAGE_BALANCE_ERROR;
    }

    @Override
    public void init() throws Exception {
        getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zingpaysdk_pmc_name_zalopay));
        showFee();
    }

    protected int getDefaultChannelId() {
        return BuildConfig.channel_zalopay;
    }

    @Override
    public int getChannelID() {
        int channelId = super.getChannelID();
        return channelId != -1 ? channelId : getDefaultChannelId();
    }

    @Override
    public boolean isFinalStep() {
        return mPageCode.equals(PAGE_CONFIRM);
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

        setBalanceView(getConfig());

        getActivity().showConfirmView(true, true, getConfig());

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

    public void setBalanceView(MiniPmcTransType pConfig) {
        getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_zalopay_wallet_method_name));
        getActivity().renderPaymentBalanceContent(pConfig);
    }

    @Override
    public void onProcessPhrase() {
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

    }
}
