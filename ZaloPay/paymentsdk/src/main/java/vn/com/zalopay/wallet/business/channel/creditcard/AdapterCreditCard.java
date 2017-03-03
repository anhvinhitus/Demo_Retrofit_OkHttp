package vn.com.zalopay.wallet.business.channel.creditcard;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardChannelType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterCreditCard extends AdapterBase {
    public AdapterCreditCard(PaymentChannelActivity pOwnerActivity) throws Exception {
        super(pOwnerActivity);

        mLayoutId = SCREEN_CC;

        if (GlobalData.isMapCardChannel() || GlobalData.isMapBankAccountChannel())
            mPageCode = PAGE_CONFIRM;
        else
            mPageCode = SCREEN_CC;

        if (GlobalData.isWithDrawChannel()) {
            mConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(), DPaymentChannel.class);
        }

        GlobalData.cardChannelType = ECardChannelType.CC;
    }

    @Override
    public void detectCard(String pCardNumber) {
        getGuiProcessor().getBankCardFinder().reset();
        getGuiProcessor().getCardFinder().detectOnOtherThread(pCardNumber, getGuiProcessor().getOnDetectCardListener());
    }

    @Override
    public void autoFillOtp(String pSender, String pOtp) {

    }

    @Override
    public DPaymentChannel getChannelConfig() throws Exception {
        return GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getCreditCardChannelConfig(), DPaymentChannel.class);
    }

    @Override
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
        }

        if (GlobalData.isLinkCardChannel())
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_credit_card_link));
        else
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_credit_card_method_name));

    }

    @Override
    public String getChannelID() {
        if (mConfig != null)
            return String.valueOf(mConfig.pmcid);
        return GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card);
    }

    @Override
    public boolean isInputStep() {
        if (getPageName().equals(SCREEN_CC))
            return true;

        return super.isInputStep();
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
    protected int onRequirePin() {
        int requirePin = super.onRequirePin();

        if (requirePin == Constants.REQUIRE_OTP) {
            requirePin = GlobalData.isRequireOtpCreditCard();

            if (requirePin == Constants.REQUIRE_PIN) {
                if (mConfig != null && mConfig.isNeedToCheckTransactionAmount()
                        && GlobalData.orderAmountTotal > mConfig.amountrequireotp)
                    requirePin = Constants.REQUIRE_OTP;
            }
        }

        return requirePin;
    }

    @Override
    public void showTransactionFailView(String pMessage) {
        super.showTransactionFailView(pMessage);

        showProgressBar(false, null);
    }

    @Override
    public void onProcessPhrase() {
        try {
            if (!GlobalData.isMapCardChannel() && !GlobalData.isMapBankAccountChannel()) {
                getGuiProcessor().populateCard();
                tranferPaymentCardToMapCard();
            }

            startSubmitTransaction();
        } catch (Exception e) {
            throw e;
        }
    }

}
