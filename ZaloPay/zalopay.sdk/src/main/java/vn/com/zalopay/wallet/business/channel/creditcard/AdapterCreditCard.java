package vn.com.zalopay.wallet.business.channel.creditcard;

import android.content.Context;

import rx.Subscription;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;

import static vn.com.zalopay.wallet.constants.Constants.SCREEN_CC;

public class AdapterCreditCard extends AdapterBase {
    public AdapterCreditCard(Context pContext, ChannelPresenter pPresenter, MiniPmcTransType pMiniPmcTransType,
                             PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pContext, SCREEN_CC, pPresenter, pMiniPmcTransType, paymentInfoHelper, statusResponse);
        GlobalData.cardChannelType = CardChannel.CREDIT;
    }

    @Override
    public void detectCard(String pCardNumber) {
        getGuiProcessor().getBankCardFinder().reset();
        Subscription subscription = getGuiProcessor()
                .getCardFinder()
                .detectOnAsync(pCardNumber, getGuiProcessor().getOnDetectCardSubscriber());
        try {
            getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    @Override
    public void init() throws Exception {
        super.init();
        this.mGuiProcessor = new CreditCardGuiProcessor(mContext, this);
        if (GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            this.mGuiProcessor.initPager();
        }
        if (TransactionHelper.isSecurityFlow(mStatusResponse)) {
            handleEventGetStatusComplete(mStatusResponse);
            detectCard(mPaymentInfoHelper.getMapBank().getFirstNumber());
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
    public void onProcessPhrase() {
        if (!mPaymentInfoHelper.payByCardMap() && !mPaymentInfoHelper.payByBankAccountMap()) {
            getGuiProcessor().populateCard();
            transformPaymentCard();
        }
        startSubmitTransaction();
    }
}
