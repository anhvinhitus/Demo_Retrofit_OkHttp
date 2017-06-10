package vn.com.zalopay.wallet.business.channel.creditcard;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterCreditCard extends AdapterBase {
    public AdapterCreditCard(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType, PaymentInfoHelper paymentInfoHelper) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType, paymentInfoHelper);
        mLayoutId = SCREEN_CC;
        mPageCode = (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) ? PAGE_CONFIRM : SCREEN_CC;
        GlobalData.cardChannelType = CardChannel.CREDIT;
    }

    @Override
    public void detectCard(String pCardNumber) {
        getGuiProcessor().getBankCardFinder().reset();
        getGuiProcessor().getCardFinder().detectOnAsync(pCardNumber, getGuiProcessor().getOnDetectCardListener());
    }

    @Override
    public void init() throws Exception {
        this.mGuiProcessor = new CreditCardGuiProcessor(this);
        if (getGuiProcessor() != null && GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            getGuiProcessor().initPager();
        }

        if (mPaymentInfoHelper.isCardLinkTrans()) {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_credit_card_link));
        } else {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_credit_card_method_name));
        }

    }

    protected int getDefaultChannelId() {
        return BuildConfig.channel_credit_card;
    }

    @Override
    public int getChannelID() {
        int channelId = super.getChannelID();
        return channelId != -1 ? channelId : getDefaultChannelId();
    }

    @Override
    public boolean isInputStep() {
        return getPageName().equals(SCREEN_CC) || super.isInputStep();

    }

    @Override
    public void moveToConfirmScreen(MiniPmcTransType pMiniPmcTransType) {
        try {
            super.moveToConfirmScreen(pMiniPmcTransType);
            showConfrimScreenForCardChannel(pMiniPmcTransType);
        } catch (Exception ex) {
            Log.e(this, ex);
        }
    }

    @Override
    public void showTransactionFailView(String pMessage) {
        super.showTransactionFailView(pMessage);
        showProgressBar(false, null);
    }

    @Override
    public void onProcessPhrase() {
        if (!mPaymentInfoHelper.payByCardMap() && !mPaymentInfoHelper.payByBankAccountMap()) {
            getGuiProcessor().populateCard();
            tranferPaymentCardToMapCard();
        }
        startSubmitTransaction();
    }

}
