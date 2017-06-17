package vn.com.zalopay.wallet.business.channel.zalopay;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterZaloPay extends AdapterBase {
    public AdapterZaloPay(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType,
                          PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType, paymentInfoHelper, statusResponse);
        mLayoutId = RS.layout.screen__zalopay;
    }

    @Override
    public void init() throws Exception {
        super.init();
        if (mPageName.equals(Constants.PAGE_BALANCE_ERROR)) {
            showFee();
            moveToConfirmScreen();
        }
    }

    protected int getDefaultChannelId() {
        return BuildConfig.channel_zalopay;
    }

    @Override
    public int getChannelID() {
        int channelId = super.getChannelID();
        return channelId != -1 ? channelId : getDefaultChannelId();
    }

    public void moveToConfirmScreen() {
        //getActivity().renderByResource();
        setBalanceView(getConfig());
        getActivity().showConfirmView(true, true, getConfig());
        getActivity().setToolBarTitle();
        getActivity().enableSubmitBtn(true);
    }

    /***
     * if this is redpackage,then close sdk
     * @return
     */
    @Override
    public boolean processResultForRedPackage() {
        long appId = mPaymentInfoHelper.getAppId();
        boolean isReqPackage = GlobalData.isRedPacketChannel(appId);
        if (isReqPackage) {
            onClickSubmission();
        }
        return isReqPackage;
    }

    public void setBalanceView(MiniPmcTransType pConfig) {
        getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_zalopay_wallet_method_name));
        getActivity().renderPaymentBalanceContent(pConfig);
    }

    @Override
    public void onProcessPhrase() {
        if (isBalanceErrorPharse()) {
            getActivity().callBackThenTerminate();
        }
    }
}
