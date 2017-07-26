package vn.com.zalopay.wallet.workflow.channelloader;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import rx.subjects.ReplaySubject;
import timber.log.Timber;
import vn.com.zalopay.utility.CurrencyUtil;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.card.CreditCardDetector;
import vn.com.zalopay.wallet.card.BankDetector;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentChannelStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.ChannelHelper;
import vn.com.zalopay.wallet.interactor.ILinkSourceInteractor;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;

public abstract class AbstractChannelLoader {
    public static final int MIN_VALUE_CHANNEL = 1000000000;
    public static final int MAX_VALUE_CHANNEL = -1;
    public ReplaySubject<PaymentChannel> source = ReplaySubject.create();
    protected List<String> pmcConfigList = new ArrayList<>();
    @TransactionType
    int mTranstype;
    AppInfoStore.Interactor mAppinfoInteractor;
    ILinkSourceInteractor mLinkInteractor;
    private double mMinValue = MIN_VALUE_CHANNEL, mMaxValue = MAX_VALUE_CHANNEL;
    private long mAppId;
    private String mUserId;
    private long mAmount;
    private long mBalance;

    public AbstractChannelLoader(long pAppId, String pUserId, long pAmount, long pBalance, @TransactionType int pTranstype) {
        this.mAppId = pAppId;
        this.mUserId = pUserId;
        this.mAmount = pAmount;
        this.mBalance = pBalance;
        this.mTranstype = pTranstype;
        this.mAppinfoInteractor = SDKApplication.getApplicationComponent().appInfoInteractor();
        this.mLinkInteractor = SDKApplication.getApplicationComponent().linkInteractor();
    }

    /***
     * adapter create channel injector
     * @return
     */
    public static AbstractChannelLoader createChannelInjector(long pAppId, String pUserId, long pAmount, long pBalance, @TransactionType int pTranstype) {
        if (pTranstype == TransactionType.WITHDRAW) {
            return new WithDrawChannelLoader(pAppId, pUserId, pAmount, pBalance, pTranstype);
        } else {
            return new PaymentChannelLoader(pAppId, pUserId, pAmount, pBalance, pTranstype);
        }
    }

    /***
     * get min/max for each channel.
     * use for alert if user input amount out of range support
     *
     * @return
     */
    public String getAlertAmount(long amount) {
        String strAlert = "";
        if (hasMinValueChannel() && amount < getMinValueChannel()) {
            strAlert = String.format(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_min_amount_mess),
                    CurrencyUtil.formatCurrency(getMinValueChannel()));
        } else if (hasMaxValueChannel() && amount > getMaxValueChannel()) {
            strAlert = String.format(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_max_amount_mess),
                    CurrencyUtil.formatCurrency(getMaxValueChannel()));
        }
        return strAlert;
    }

    protected abstract void detectChannel() throws Exception;

    /***
     * get channel from pmc list
     */
    protected void getChannelFromConfig() {
        for (String pmcKey : pmcConfigList) {
            try {
                MiniPmcTransType activeChannel = mAppinfoInteractor.getPmcConfigByPmcKey(pmcKey);
                if (activeChannel == null) {
                    continue;
                }
                PaymentChannel channel = new PaymentChannel(activeChannel);
                if (channel.isBankAccount()) {
                    continue;//skip bank account
                }
                if (channel.isEnable()) {
                    channel.calculateFee(mAmount);//calculate fee of this channel
                    channel.checkPmcOrderAmount(mAmount);//check amount is support or not
                }
                //check maintenance for cc
                if (channel.isEnable() && ((channel.isCreditCardChannel() && isBankMaintenance(channel.bankcode, BankFunctionCode.PAY_BY_CARD))
                        || (channel.isBankAccount() && isBankMaintenance(channel.bankcode, BankFunctionCode.PAY_BY_BANK_ACCOUNT)))) {
                    channel.setStatus(PaymentChannelStatus.MAINTENANCE);
                } else if (channel.isZaloPayChannel()) {
                    boolean balanceError = mBalance < mAmount + channel.totalfee;
                    if (balanceError) {
                        channel.setAllowOrderAmount(false);
                    }
                } else if (channel.isAtmChannel()) {
                    StringBuilder keyBuilder = new StringBuilder();
                    keyBuilder.append(mAppId).append(Constants.UNDERLINE).append(mTranstype);
                    long bankMinAmountSupport = mAppinfoInteractor.getBankMinAmountSupport(keyBuilder.toString());
                    if (bankMinAmountSupport > 0 && mAmount < bankMinAmountSupport) {
                        channel.minvalue = bankMinAmountSupport;
                        channel.status = PaymentChannelStatus.DISABLE;
                    }
                }
                //get icon
                ChannelHelper.inflatChannelIcon(channel, null);
                findValue(channel); //get min/max amount
                send(channel);
            } catch (Exception e) {
                Timber.w(e, "Exception get channel from config");
            }
        }
    }

    /***
     * get map bank accounts from cache
     *
     * @throws Exception
     */
    protected void getMapBankAccount() throws Exception {
        try {
            List<BankAccount> bankAccounts = mLinkInteractor.getBankAccountList(mUserId);
            if (bankAccounts == null || bankAccounts.size() <= 0) {
                Timber.d("get map bank account from cache is empty");
                return;
            }
            for (BankAccount bankAccount : bankAccounts) {
                MiniPmcTransType activeChannel = mAppinfoInteractor.getPmcConfig(mAppId, mTranstype, bankAccount.bankcode);
                Log.d(this, "active channel ", activeChannel);
                if (activeChannel != null) {
                    //check this map card/map bankaccount is support or not
                    allowPaymentChannel(activeChannel);
                    resetPmc(activeChannel);
                    if (isBankMaintenance(bankAccount.bankcode, BankFunctionCode.PAY_BY_BANKACCOUNT_TOKEN)) {
                        activeChannel.setStatus(PaymentChannelStatus.MAINTENANCE);
                    }
                    PaymentChannel channel = new PaymentChannel(activeChannel);
                    channel.f6no = bankAccount.firstaccountno;
                    channel.l4no = bankAccount.lastaccountno;
                    channel.bankcode = bankAccount.bankcode;
                    channel.pmcname = GlobalData.getAppContext().getResources().getString(R.string.sdk_bankaccount_name);
                    channel.isBankAccountMap = true;

                    ChannelHelper.inflatChannelIcon(channel, bankAccount.bankcode);
                    //calculate fee
                    channel.calculateFee(mAmount);

                    //check amount is support or not
                    if (channel.isEnable()) {
                        channel.checkPmcOrderAmount(mAmount);//check amount is support or not
                    }
                    send(channel);
                }
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    protected void resetPmc(MiniPmcTransType pChannel) {
        if (pChannel == null) {
            return;
        }
        if (BankAccountHelper.isBankAccount(pChannel.bankcode)) {
            pChannel.pmcid = BuildConfig.channel_bankaccount;
        } else if (BuildConfig.CC_CODE.equals(pChannel.bankcode)) {
            pChannel.pmcid = BuildConfig.channel_credit_card;
        } else {
            pChannel.pmcid = BuildConfig.channel_atm;
        }
    }

    /***
     * get map card from cache
     *
     * @throws Exception
     */
    protected void getMapCard() throws Exception {
        try {
            List<MapCard> mapCards = mLinkInteractor.getMapCardList(mUserId);
            if (mapCards == null || mapCards.size() <= 0) {
                Timber.d("get map card is null");
                return;
            }
            for (MapCard mapCard : mapCards) {
                MiniPmcTransType activeChannel = mAppinfoInteractor.getPmcConfig(mAppId, mTranstype, mapCard.bankcode);
                if (activeChannel != null) {
                    //check this map card is support or not
                    allowPaymentChannel(activeChannel);
                    resetPmc(activeChannel);

                    if (isBankMaintenance(mapCard.bankcode, BankFunctionCode.PAY_BY_CARD_TOKEN)) {
                        activeChannel.setStatus(PaymentChannelStatus.MAINTENANCE);
                    }
                    PaymentChannel channel = new PaymentChannel(activeChannel);
                    channel.l4no = mapCard.last4cardno;
                    channel.f6no = mapCard.first6cardno;
                    channel.bankcode = mapCard.bankcode;

                    //calculate fee
                    channel.calculateFee(mAmount);

                    //check amount is support or not
                    if (channel.isEnable()) {
                        channel.checkPmcOrderAmount(mAmount);//check amount is support or not
                    }

                    if (BuildConfig.CC_CODE.equals(channel.bankcode)) {
                        CreditCardDetector.getInstance().detectOnSync(channel.f6no);
                        if (CreditCardDetector.getInstance().detected()) {
                            //populate channel name
                            channel.pmcname = String.format(GlobalData.getAppContext().getResources().getString(R.string.sdk_card_link_format), CreditCardDetector.getInstance().getBankName()) + mapCard.last4cardno;
                            String cardType = CreditCardDetector.getInstance().getCodeBankForVerifyCC();
                            ChannelHelper.inflatChannelIcon(channel, cardType);
                        }
                    }
                    //this is atm
                    else {
                        ChannelHelper.inflatChannelIcon(channel, mapCard.bankcode);
                        BankDetector.getInstance().detectOnSync(channel.f6no);
                        if (BankDetector.getInstance().detected()) {
                            //populate channel name
                            String bankName = BankDetector.getInstance().getShortBankName();
                            if (TextUtils.isEmpty(bankName)) {
                                bankName = GlobalData.getAppContext().getResources().getString(R.string.sdk_card_link_default_format);
                            } else {
                                bankName = String.format(GlobalData.getAppContext().getResources().getString(R.string.sdk_card_link_format), bankName);
                            }
                            channel.pmcname = bankName + mapCard.last4cardno;
                        }
                    }
                    if (!CreditCardDetector.getInstance().detected() && !BankDetector.getInstance().detected()) {
                        channel.pmcname = GlobalData.getAppContext().getResources().getString(R.string.sdk_card_link_default_format) + mapCard.last4cardno;
                    }
                    send(channel);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    protected void send(PaymentChannel pChannel) {
        if (pChannel != null) {
            updateChannelStatus(pChannel);
            source.onNext(pChannel);
        }
    }

    private void updateChannelStatus(PaymentChannel channel) {
        if (this instanceof WithDrawChannelLoader) {
            processWithDrawCase(channel);
        }
        //update status bank future version
        if (!channel.isVersionSupport(SdkUtils.getAppVersion(GlobalData.getAppContext()))) {
            channel.setAllowBankVersion(false);
        }
    }

    private void processWithDrawCase(PaymentChannel pChannel) {
        BankConfig bankConfig = SDKApplication.getApplicationComponent()
                .bankListInteractor()
                .getBankConfig(pChannel.bankcode);
        if (bankConfig == null) {
            return;
        }
        if (!bankConfig.isWithDrawAllow()) {
            pChannel.setStatus(PaymentChannelStatus.DISABLE);
        } else if (bankConfig.isBankMaintenence(BankFunctionCode.WITHDRAW)) {
            pChannel.setStatus(PaymentChannelStatus.MAINTENANCE);
        }

        //check fee + amount <= balance
        double amount_total = mAmount + pChannel.totalfee;
        if (mBalance < amount_total) {
            pChannel.setAllowOrderAmount(false);
        }
    }

    /***
     * bank or cc is maintenance
     *
     * @param pBankCode
     * @return
     */
    protected boolean isBankMaintenance(String pBankCode, @BankFunctionCode int pBankFunction) {
        if (TextUtils.isEmpty(pBankCode)) {
            return false;
        }
        BankConfig bankConfig = SDKApplication
                .getApplicationComponent()
                .bankListInteractor()
                .getBankConfig(pBankCode);
        return bankConfig != null && bankConfig.isBankMaintenence(pBankFunction);
    }

    /***
     * this is atm map card have in pmclist
     * @param pChannel
     * @return
     */
    protected void allowPaymentChannel(MiniPmcTransType pChannel) {
        if (pChannel == null) {
            return;
        }
        if (pmcConfigList == null || !pmcConfigList.contains(MiniPmcTransType.getPmcKey(mAppId, mTranstype, pChannel.pmcid))) {
            pChannel.setStatus(PaymentChannelStatus.DISABLE);
        }
    }

    /***
     * detect min/max value
     * @param activeChannel
     */
    public void findValue(MiniPmcTransType activeChannel) {
        if (activeChannel == null || !activeChannel.isEnable())
            return;

        if (activeChannel.minvalue > 0 && activeChannel.minvalue < mMinValue)
            mMinValue = activeChannel.minvalue;

        if (activeChannel.maxvalue > 0 && activeChannel.maxvalue > mMaxValue)
            mMaxValue = activeChannel.maxvalue;
    }

    public void getChannels() throws Exception {
        try {
            pmcConfigList = mAppinfoInteractor.getPmcTranstypeKeyList(mAppId, mTranstype);
            detectChannel();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public double getMinValueChannel() {
        return mMinValue;
    }

    public double getMaxValueChannel() {
        return mMaxValue;
    }

    public boolean hasMinValueChannel() {
        return mMinValue != MIN_VALUE_CHANNEL;
    }

    public boolean hasMaxValueChannel() {
        return mMaxValue != MAX_VALUE_CHANNEL;
    }
}
