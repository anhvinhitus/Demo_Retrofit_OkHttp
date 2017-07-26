package vn.com.zalopay.wallet.workflow;

import android.content.Context;

import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.workflow.ui.CreditCardGuiProcessor;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;

import static vn.com.zalopay.wallet.constants.Constants.SCREEN_CC;

public class CreditCardWorkFlow extends AbstractWorkFlow {
    public CreditCardWorkFlow(Context pContext, ChannelPresenter pPresenter, MiniPmcTransType pMiniPmcTransType,
                              PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pContext, SCREEN_CC, pPresenter, pMiniPmcTransType, paymentInfoHelper, statusResponse);
        GlobalData.cardChannelType = CardChannel.CREDIT;
    }

    @Override
    public void detectCard(String pCardNumber) {
        try {
            getGuiProcessor().getBankCardFinder().reset();
            Subscription subscription = getGuiProcessor()
                    .getCardFinder()
                    .detectOnAsync(pCardNumber)
                    .compose(SchedulerHelper.applySchedulers())
                    .subscribe(detected -> {
                        try {
                            getGuiProcessor().onDetectCardComplete(detected);
                        } catch (Exception e) {
                            Timber.w(e);
                        }
                    }, Timber::d);
            getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    @Override
    public void init() throws Exception {
        super.init();
        if (GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            initializeGuiProcessor();
        }
        if (TransactionHelper.isSecurityFlow(mStatusResponse)) {
            initializeGuiProcessor();
            handleEventGetStatusComplete(mStatusResponse);
            detectCard(mPaymentInfoHelper.getMapBank().getFirstNumber());
        }
    }

    @Override
    protected void initializeGuiProcessor() throws Exception {
        this.mGuiProcessor = new CreditCardGuiProcessor(mContext, this, getPresenter().getViewOrThrow());
        this.mGuiProcessor.initPager();
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
            try {
                mGuiProcessor.populateCard();
                transformPaymentCard();
            } catch (Exception e) {
                Timber.w(e, "Exception populate payment card");
            }
        }
        startSubmitTransaction();
    }

    @Override
    protected void stopLoadWeb() {
        try {
            if (getGuiProcessor() != null) {
                getGuiProcessor().stopWebview();
            }
        } catch (Exception e) {
            Timber.w(e);
        }
    }
}
