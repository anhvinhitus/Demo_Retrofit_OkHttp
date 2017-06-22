package vn.com.zalopay.wallet.business.channel.zalopay;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channel.PaymentChannelActivity;

import static vn.com.zalopay.wallet.constants.Constants.PAGE_BALANCE_ERROR;

public class AdapterZaloPay extends AdapterBase {
    public AdapterZaloPay(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType,
                          PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType, paymentInfoHelper, statusResponse);
        mLayoutId = RS.layout.screen__zalopay;
    }

    @Override
    public String getDefaultPageName() {
        return PAGE_BALANCE_ERROR;
    }

    @Override
    public void init() throws Exception {
        super.init();
        if (PAGE_BALANCE_ERROR.equals(mPageName)) {
            getActivity().setToolBarTitle();
            getActivity().enableSubmitBtn(true);
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

    @Override
    public void onProcessPhrase() {
        if (isBalanceErrorPharse()) {
            getActivity().callBackThenTerminate();
        }
    }
}
