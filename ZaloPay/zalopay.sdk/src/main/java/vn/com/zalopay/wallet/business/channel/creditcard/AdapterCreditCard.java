package vn.com.zalopay.wallet.business.channel.creditcard;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
<<<<<<< HEAD
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
=======
import vn.com.zalopay.wallet.business.entity.enumeration.ECardChannelType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
>>>>>>> 9fd9a35... [SDK] Apply app info v1
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterCreditCard extends AdapterBase {
    public AdapterCreditCard(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType);
        mLayoutId = SCREEN_CC;
<<<<<<< HEAD

        if (GlobalData.isMapCardChannel() || GlobalData.isMapBankAccountChannel())
            mPageCode = PAGE_CONFIRM;
        else
            mPageCode = SCREEN_CC;

        if (GlobalData.isWithDrawChannel()) {
            mConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(), DPaymentChannel.class);
        }

        GlobalData.cardChannelType = CardChannel.CREDIT;
=======
        mPageCode = (GlobalData.isMapCardChannel() || GlobalData.isMapBankAccountChannel()) ? PAGE_CONFIRM : SCREEN_CC;
        GlobalData.cardChannelType = ECardChannelType.CC;
>>>>>>> 9fd9a35... [SDK] Apply app info v1
    }

    @Override
    public void detectCard(String pCardNumber) {
        getGuiProcessor().getBankCardFinder().reset();
        getGuiProcessor().getCardFinder().detectOnAsync(pCardNumber, getGuiProcessor().getOnDetectCardListener());
    }

    @Override
<<<<<<< HEAD
    public DPaymentChannel getChannelConfig() throws Exception {
        return GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getCreditCardChannelConfig(), DPaymentChannel.class);
    }

    @Override
    public void init() throws Exception {
        this.mGuiProcessor = new CreditCardGuiProcessor(this);
        if (getGuiProcessor() != null && GlobalData.isChannelHasInputCard()) {
            getGuiProcessor().initPager();
=======
    public void init() {
        try {
            this.mGuiProcessor = new CreditCardGuiProcessor(this);
            if (getGuiProcessor() != null && GlobalData.isChannelHasInputCard()) {
                getGuiProcessor().initPager();
            }

        } catch (Exception e) {
            Log.e(this, e);
            terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);

            return;
>>>>>>> 9fd9a35... [SDK] Apply app info v1
        }

        if (GlobalData.isLinkCardChannel()) {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_credit_card_link));
        } else {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_credit_card_method_name));
        }

    }

    protected int getDefaultChannelId() {
        return Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card));
    }

    @Override
<<<<<<< HEAD
    public String getChannelID() {
        if (mConfig != null)
            return String.valueOf(mConfig.pmcid);
        return String.valueOf(BuildConfig.channel_credit_card);
=======
    public int getChannelID() {
        int channelId = super.getChannelID();
        return channelId != -1 ? channelId : getDefaultChannelId();
>>>>>>> 9fd9a35... [SDK] Apply app info v1
    }

    @Override
    public boolean isInputStep() {
        return getPageName().equals(SCREEN_CC) || super.isInputStep();

    }

    @Override
    public void moveToConfirmScreen() {
        try {
            super.moveToConfirmScreen();

            showConfrimScreenForCardChannel();

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
        if (!GlobalData.isMapCardChannel() && !GlobalData.isMapBankAccountChannel()) {
            getGuiProcessor().populateCard();
            tranferPaymentCardToMapCard();
        }

        startSubmitTransaction();
    }

}
