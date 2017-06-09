package vn.com.zalopay.wallet.business.channel.injector;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.behavior.gateway.AppInfoLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.PaymentChannelStatus;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.ChannelHelper;
import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

public abstract class BaseChannelInjector {
    public static final int MIN_VALUE_CHANNEL = 1000000000;
    public static final int MAX_VALUE_CHANNEL = -1;

    protected List<PaymentChannel> mChannelList = new ArrayList<>();
    protected ArrayList<String> pmcConfigList = new ArrayList<>();
    private double mMinValue = MIN_VALUE_CHANNEL, mMaxValue = MAX_VALUE_CHANNEL;
    protected PaymentInfoHelper mPaymentInfoHelper;

    public BaseChannelInjector(PaymentInfoHelper paymentInfoHelper) {
        mPaymentInfoHelper = paymentInfoHelper;
    }

    /***
     * adapter create channel injector
     * @return
     */
    public static BaseChannelInjector createChannelInjector(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper.isTranferMoneyChannel()) {
            return new TranferChannelInjector(paymentInfoHelper);
        } else if (paymentInfoHelper.isWithDrawChannel()) {
            return new WithDrawChannelInjector(paymentInfoHelper);
        } else {
            return new PaymentChannelInjector(paymentInfoHelper);
        }
    }

    protected abstract void detectChannel(ZPWOnGetChannelListener pListener) throws Exception;

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
                        && BankAccountHelper.hasBankAccountOnCache(mPaymentInfoHelper.getUserId(), GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank))) {
                    continue;//user has linked vietcombank account , no need show bank account channel
                }
                if (channel.isEnable()) {
                    channel.calculateFee(mPaymentInfoHelper.getAmount());//calculate fee of this channel
                    channel.checkPmcOrderAmount(mPaymentInfoHelper.getAmount());//check amount is support or not
                }
                //check maintenance for cc
                if (channel.isEnable() && ((channel.isCreditCardChannel() && isBankMaintenance(channel.bankcode, BankFunctionCode.PAY_BY_CARD))
                        || (channel.isBankAccount() && isBankMaintenance(channel.bankcode, BankFunctionCode.PAY_BY_BANK_ACCOUNT)))) {
                    channel.setStatus(PaymentChannelStatus.MAINTENANCE);
                }
                //get icon
                ChannelHelper.inflatChannelIcon(channel, null);
                findValue(channel); //get min/max amount
                addChannelToList(channel);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    /***
     * sort channel list
     * disable channel will be the last of list
     */
    protected void sortChannels() {
        if (mChannelList == null || mChannelList.size() <= 1) {
            return;
        }
        ArrayList<PaymentChannel> tempArr = new ArrayList<>();
        for (Iterator<PaymentChannel> iterator = mChannelList.iterator(); iterator.hasNext(); ) {
            PaymentChannel channel = iterator.next();
            if (!channel.isEnable() || !channel.isAllowByAmount() || channel.isMaintenance()) {
                tempArr.add(channel.clone());
                iterator.remove();
            }
        }
        if (tempArr.size() > 0) {
            mChannelList.addAll(tempArr);
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
            String mapBankAccountKeyList = SharedPreferencesManager.getInstance().getBankAccountKeyList(mPaymentInfoHelper.getUserId());
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
                String mapObject = SharedPreferencesManager.getInstance().getMapCardByKey(mPaymentInfoHelper.getUserId(), mapCardID);
                if (TextUtils.isEmpty(mapObject)) {
                    continue;
                }
                BankAccount bankAccount = GsonUtils.fromJsonString(mapObject, BankAccount.class);
                if (bankAccount == null) {
                    continue;
                }

                MiniPmcTransType activeChannel = null;
                if (mPaymentInfoHelper.isWithDrawChannel()) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(mPaymentInfoHelper.getAppId(), mPaymentInfoHelper.getTranstype(), bankAccount.bankcode), MiniPmcTransType.class);
                } else if (BankAccountHelper.isBankAccount(bankAccount.bankcode)) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankAccountChannelConfig(mPaymentInfoHelper.getAppId(), mPaymentInfoHelper.getTranstype(), bankAccount.bankcode), MiniPmcTransType.class);
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
                    channel.calculateFee(mPaymentInfoHelper.getAmount());

                    //check amount is support or not
                    if (channel.isEnable()) {
                        channel.checkPmcOrderAmount(mPaymentInfoHelper.getAmount());//check amount is support or not
                    }
                    //add channel to list
                    if (!mChannelList.contains(channel)) {
                        mChannelList.add(channel);
                    }

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
            String mappCardIdList = SharedPreferencesManager.getInstance().getMapCardKeyList(mPaymentInfoHelper.getUserId());
            if (TextUtils.isEmpty(mappCardIdList)) {
                Log.d(this, "get map card is null");
                return;
            }
            Log.d(this, "map card list " + mappCardIdList);
            for (String mapCardID : mappCardIdList.split(Constants.COMMA)) {
                if (TextUtils.isEmpty(mapCardID)) {
                    continue;
                }
                String strMapCard = SharedPreferencesManager.getInstance().getMapCardByKey(mPaymentInfoHelper.getUserId(), mapCardID); //get card info from cache
                if (TextUtils.isEmpty(strMapCard)) {
                    continue;
                }
                MapCard mapCard = GsonUtils.fromJsonString(strMapCard, MapCard.class);
                if (mapCard == null) {
                    continue;
                }
                Log.d(this, "map card ", mapCard);
                MiniPmcTransType activeChannel;
                if (mPaymentInfoHelper.isWithDrawChannel()) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(mPaymentInfoHelper.getAppId(), mPaymentInfoHelper.getTranstype(), mapCard.bankcode), MiniPmcTransType.class);
                } else if (BuildConfig.CC_CODE.equals(mapCard.bankcode)) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getCreditCardChannelConfig(mPaymentInfoHelper.getAppId(), mPaymentInfoHelper.getTranstype(), mapCard.bankcode), MiniPmcTransType.class);
                } else {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getATMChannelConfig(mPaymentInfoHelper.getAppId(), mPaymentInfoHelper.getTranstype(), mapCard.bankcode), MiniPmcTransType.class);
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
                    channel.calculateFee(mPaymentInfoHelper.getAmount());

                    //check amount is support or not
                    if (channel.isEnable()) {
                        channel.checkPmcOrderAmount(mPaymentInfoHelper.getAmount());//check amount is support or not
                    }

                    if (BuildConfig.CC_CODE.equals(channel.bankcode)) {
                        CreditCardCheck.getInstance().detectOnSync(channel.f6no);
                        if (CreditCardCheck.getInstance().isDetected()) {
                            //populate channel name
                            channel.pmcname = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card), CreditCardCheck.getInstance().getBankName()) + mapCard.last4cardno;
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
                                bankName = GlobalData.getStringResource(RS.string.zpw_save_credit_card_default);
                            } else {
                                bankName = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card_atm), bankName);
                            }
                            channel.pmcname = bankName + mapCard.last4cardno;
                        }
                    }
                    if (!CreditCardCheck.getInstance().isDetected() && !BankCardCheck.getInstance().isDetected()) {
                        channel.pmcname = GlobalData.getStringResource(RS.string.zpw_save_credit_card_default) + mapCard.last4cardno;
                    }
                    //add channel to list
                    if (!mChannelList.contains(channel)) {
                        mChannelList.add(channel);
                    }

                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    protected void addChannelToList(PaymentChannel pChannel) {
        if (pChannel != null) {
            //sometimes network not stable, so 2 channels same in listview, we must exclude it if it existed
            if (mChannelList.contains(pChannel)) {
                return;
            }
            if (pChannel.isEnable() && pChannel.isZaloPayChannel()) {
                //add channel to head
                mChannelList.add(0, pChannel);
            } else
                mChannelList.add(pChannel);
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
        return BankLoader.getInstance().isBankMaintenance(pBankCode, pBankFunction);
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
        if (pmcConfigList == null || (pmcConfigList != null &&
                !pmcConfigList.contains(pChannel.getPmcKey(mPaymentInfoHelper.getAppId(), mPaymentInfoHelper.getTranstype(), pChannel.pmcid)))) {
            pChannel.setStatus(PaymentChannelStatus.DISABLE);
        }
    }

    /***
     * detect min/max value
     *
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

    public void getChannels(ZPWOnGetChannelListener pListener) throws Exception {
        try {
            pmcConfigList = AppInfoLoader.getChannelsForAppFromCache(String.valueOf(mPaymentInfoHelper.getAppId()), mPaymentInfoHelper.getTranstype());
            detectChannel(pListener);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /***
     * remove channels out list
     * keep some channel to force channel
     *
     * @param pChannels
     */
    public void filterForceChannel(int[] pChannels) {
        for (Iterator<PaymentChannel> iterator = mChannelList.iterator(); iterator.hasNext(); ) {
            PaymentChannel channelView = iterator.next();

            boolean isExist = false;

            for (int i = 0; i < pChannels.length; i++) {
                if (pChannels[i] == channelView.pmcid) {
                    isExist = true;
                    break;
                }
            }
            //remove it
            if (!isExist) {
                Log.d("filterForceChannel", "remove channel by force", channelView);
                iterator.remove();
            }

        }
    }

    public List<PaymentChannel> getChannelList() {
        return mChannelList;
    }

    public boolean isChannelEmpty() {
        return mChannelList == null || mChannelList.size() <= 0;
    }

    public boolean isChannelUnique() {
        return mChannelList != null && mChannelList.size() <= 1;
    }

    public PaymentChannel getFirstChannel() {
        if (mChannelList != null && mChannelList.size() >= 1)
            return mChannelList.get(0);

        return null;
    }

    public PaymentChannel getChannelAtPosition(int pPosition) {
        try {
            if (mChannelList != null)
                return mChannelList.get(pPosition);

        } catch (Exception e) {
            Log.e(this, e);
        }
        return null;
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
