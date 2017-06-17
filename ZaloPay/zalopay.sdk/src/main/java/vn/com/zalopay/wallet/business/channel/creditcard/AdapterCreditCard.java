package vn.com.zalopay.wallet.business.channel.creditcard;

import rx.Subscription;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

import static vn.com.zalopay.wallet.constants.Constants.SCREEN_CC;

public class AdapterCreditCard extends AdapterBase {
    public AdapterCreditCard(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType,
                             PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType, paymentInfoHelper, statusResponse);
        mLayoutId = SCREEN_CC;
        GlobalData.cardChannelType = CardChannel.CREDIT;
    }

    @Override
    public String getDefaultPageName() {
        return SCREEN_CC;
    }

    @Override
    public void detectCard(String pCardNumber) {
        getGuiProcessor().getBankCardFinder().reset();
        Subscription subscription = getGuiProcessor()
                .getCardFinder()
                .detectOnAsync(pCardNumber, getGuiProcessor().getOnDetectCardSubscriber());
        getActivity().addSuscription(subscription);
    }

    @Override
    public void init() throws Exception {
        super.init();
        this.mGuiProcessor = new CreditCardGuiProcessor(this);
        if (GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            this.mGuiProcessor.initPager();
        }
        if (mPaymentInfoHelper.isCardLinkTrans()) {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_credit_card_link));
        } else {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_credit_card_method_name));
        }

    }

    private int getDefaultChannelId() {
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
