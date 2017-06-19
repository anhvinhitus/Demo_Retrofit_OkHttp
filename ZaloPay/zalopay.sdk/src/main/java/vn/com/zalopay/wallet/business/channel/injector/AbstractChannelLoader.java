package vn.com.zalopay.wallet.business.channel.injector;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import rx.subjects.ReplaySubject;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.PaymentChannelStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.ChannelHelper;

public abstract class AbstractChannelLoader {
    public static final int MIN_VALUE_CHANNEL = 1000000000;
    public static final int MAX_VALUE_CHANNEL = -1;
    public ReplaySubject<PaymentChannel> source = ReplaySubject.create();
    protected List<String> pmcConfigList = new ArrayList<>();
    @TransactionType
    int mTranstype;
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
            strAlert = String.format(GlobalData.getStringResource(RS.string.zpw_string_alert_min_amount_input),
                    StringUtil.formatVnCurrence(String.valueOf(getMinValueChannel())));
        } else if (hasMaxValueChannel() && amount > getMaxValueChannel()) {
            strAlert = String.format(GlobalData.getStringResource(RS.string.zpw_string_alert_max_amount_input),
                    StringUtil.formatVnCurrence(String.valueOf(getMaxValueChannel())));
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
                MiniPmcTransType activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getPmcConfigByPmcKey(pmcKey), MiniPmcTransType.class);
                if (activeChannel == null) {
                    continue;
                }
                PaymentChannel channel = new PaymentChannel(activeChannel);
                if (channel.isBankAccount()
                        && BankAccountHelper.hasBankAccountOnCache(mUserId, CardType.PVCB)) {
                    continue;//user has linked vietcom bank account , no need show bank account channel
                }
                if (channel.isEnable()) {
                    channel.calculateFee(mAmount);//calculate fee of this channel
                    channel.checkPmcOrderAmount(mAmount);//check amount is support or not
                }
                //check maintenance for cc
                if (channel.isEnable() && ((channel.isCreditCardChannel() && isBankMaintenance(channel.bankcode, BankFunctionCode.PAY_BY_CARD))
                        || (channel.isBankAccount() && isBankMaintenance(channel.bankcode, BankFunctionCode.PAY_BY_BANK_ACCOUNT)))) {
                    channel.setStatus(PaymentChannelStatus.MAINTENANCE);
                }
                if(channel.isZaloPayChannel()){
                    boolean balanceError = mBalance <= mAmount + channel.totalfee;
                    if(balanceError){
                        channel.setAllowOrderAmount(false);
                    }
                }
                //get icon
                ChannelHelper.inflatChannelIcon(channel, null);
                findValue(channel); //get min/max amount
                send(channel);
            } catch (Exception e) {
                Log.e(this, e);
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
            //get list of mapped bank account from cached.
            String mapBankAccountKeyList = SharedPreferencesManager.getInstance().getBankAccountKeyList(mUserId);
            if (TextUtils.isEmpty(mapBankAccountKeyList)) {
                Log.d(this, "get map bank account from cache is empty");
                return;
            }
            Log.d(this, "get map bank account from cache" + mapBankAccountKeyList);
            for (String mapCardID : mapBankAccountKeyList.split(Constants.COMMA)) {
                if (TextUtils.isEmpty(mapCardID)) {
                    continue;
                }
                //get card info from cache.
                String mapObject = SharedPreferencesManager.getInstance().getMap(mUserId, mapCardID);
                if (TextUtils.isEmpty(mapObject)) {
                    continue;
                }
                BankAccount bankAccount = GsonUtils.fromJsonString(mapObject, BankAccount.class);
                if (bankAccount == null) {
                    continue;
                }

                MiniPmcTransType activeChannel = null;
                if (mTranstype == TransactionType.WITHDRAW) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(mAppId, mTranstype, bankAccount.bankcode), MiniPmcTransType.class);
                } else if (BankAccountHelper.isBankAccount(bankAccount.bankcode)) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankAccountChannelConfig(mAppId, mTranstype, bankAccount.bankcode), MiniPmcTransType.class);
                }
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
                    channel.pmcname = GlobalData.getStringResource(RS.string.zpw_channelname_vietcombank_mapaccount);
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
            //get list of mapped card from cached.
            String mappCardIdList = SharedPreferencesManager.getInstance().getMapCardKeyList(mUserId);
            if (TextUtils.isEmpty(mappCardIdList)) {
                Log.d(this, "get map card is null");
                return;
            }
            Log.d(this, "map card list " + mappCardIdList);
            for (String mapCardID : mappCardIdList.split(Constants.COMMA)) {
                if (TextUtils.isEmpty(mapCardID)) {
                    continue;
                }
                String strMapCard = SharedPreferencesManager.getInstance().getMap(mUserId, mapCardID); //get card info from cache
                if (TextUtils.isEmpty(strMapCard)) {
                    continue;
                }
                MapCard mapCard = GsonUtils.fromJsonString(strMapCard, MapCard.class);
                if (mapCard == null) {
                    continue;
                }
                Log.d(this, "map card ", mapCard);
                MiniPmcTransType activeChannel;
                if (mTranstype == TransactionType.WITHDRAW) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(mAppId, mTranstype, mapCard.bankcode), MiniPmcTransType.class);
                } else if (BuildConfig.CC_CODE.equals(mapCard.bankcode)) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getCreditCardChannelConfig(mAppId, mTranstype, mapCard.bankcode), MiniPmcTransType.class);
                } else {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getATMChannelConfig(mAppId, mTranstype, mapCard.bankcode), MiniPmcTransType.class);
                }
                Log.d(this, "active channel is", activeChannel);
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
                        CreditCardCheck.getInstance().detectOnSync(channel.f6no);
                        if (CreditCardCheck.getInstance().isDetected()) {
                            //populate channel name
                            channel.pmcname = String.format(GlobalData.getStringResource(RS.string.sdk_creditcard_label), CreditCardCheck.getInstance().getBankName()) + mapCard.last4cardno;
                            String cardType = CreditCardCheck.getInstance().getCodeBankForVerify();
                            ChannelHelper.inflatChannelIcon(channel, cardType);
                        }
                    }
                    //this is atm
                    else {
                        ChannelHelper.inflatChannelIcon(channel, mapCard.bankcode);
                        BankCardCheck.getInstance().detectOnSync(channel.f6no);
                        if (BankCardCheck.getInstance().isDetected()) {
                            //populate channel name
                            String bankName = BankCardCheck.getInstance().getShortBankName();
                            if (TextUtils.isEmpty(bankName)) {
                                bankName = GlobalData.getStringResource(RS.string.sdk_card_default_label);
                            } else {
                                bankName = String.format(GlobalData.getStringResource(RS.string.sdk_card_generic_label), bankName);
                            }
                            channel.pmcname = bankName + mapCard.last4cardno;
                        }
                    }
                    if (!CreditCardCheck.getInstance().isDetected() && !BankCardCheck.getInstance().isDetected()) {
                        channel.pmcname = GlobalData.getStringResource(RS.string.sdk_card_default_label) + mapCard.last4cardno;
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
            if (this instanceof WithDrawChannelLoader) {
                processWithDrawCase(pChannel);
            }
            //sometimes network not stable, so 2 channels same in listview, we must exclude it if it existed
            source.onNext(pChannel);
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
        if (pmcConfigList == null || !pmcConfigList.contains(pChannel.getPmcKey(mAppId, mTranstype, pChannel.pmcid))) {
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
            pmcConfigList = SDKApplication.getApplicationComponent()
                    .appInfoInteractor()
                    .getPmcTranstypeKeyList(mAppId, mTranstype);
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
