package vn.com.zalopay.wallet.business.channel.injector;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vn.com.zalopay.wallet.business.behavior.gateway.AppInfoLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentChannelStatus;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.CChannelHelper;
import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public abstract class BaseChannelInjector {
    public static final int MIN_VALUE_CHANNEL = 1000000000;
    public static final int MAX_VALUE_CHANNEL = -1;

    protected List<PaymentChannel> mChannelList = new ArrayList<>();

    protected ArrayList<String> pmcConfigList = new ArrayList<>();

    private double mMinValue = MIN_VALUE_CHANNEL, mMaxValue = MAX_VALUE_CHANNEL;

    /***
     * adapter create channel injector
     *
     * @return
     */
    public static BaseChannelInjector createChannelInjector() {
        if (GlobalData.isTranferMoneyChannel()) {
            return new TranferChannelInjector();
        } else if (GlobalData.isWithDrawChannel()) {
            return new WithDrawChannelInjector();
        } else {
            return new PaymentChannelInjector();
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
                        && BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank))) {
                    continue;//user has linked vietcombank account , no need show bank account channel
                }
                if (channel.isEnable() && !channel.isAtmChannel()) {
                    channel.calculateFee();//calculate fee of this channel
                    channel.checkPmcOrderAmount(GlobalData.getOrderAmount());//check amount is support or not
                }
                //check maintenance for cc
                if (channel.isEnable()
                        && (channel.isCreditCardChannel() && isBankMaintenance(channel.bankcode, EBankFunction.PAY_BY_CARD))
                        || (channel.isBankAccount() && isBankMaintenance(channel.bankcode, EBankFunction.PAY_BY_BANK_ACCOUNT))) {
                    channel.setStatus(EPaymentChannelStatus.MAINTENANCE);
                }
                //get icon
                CChannelHelper.inflatChannelIcon(channel, null);
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
            String mapBankAccountKeyList = SharedPreferencesManager.getInstance().getBankAccountKeyList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
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
                String mapObject = SharedPreferencesManager.getInstance().getMapCardByKey(mapCardID);
                if (TextUtils.isEmpty(mapObject)) {
                    continue;
                }
                DBankAccount bankAccount = GsonUtils.fromJsonString(mapObject, DBankAccount.class);
                if (bankAccount == null) {
                    continue;
                }

                MiniPmcTransType activeChannel = null;
                if (GlobalData.isWithDrawChannel()) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(bankAccount.bankcode), MiniPmcTransType.class);
                } else if (BankAccountHelper.isBankAccount(bankAccount.bankcode)) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankAccountChannelConfig(bankAccount.bankcode), MiniPmcTransType.class);
                }
                Log.d(this, "active channel ", activeChannel);
                if (activeChannel != null) {
                    //check this map card/map bankaccount is support or not
                    allowPaymentChannel(activeChannel);
                    resetPmc(activeChannel);
                    if (isBankMaintenance(bankAccount.bankcode, EBankFunction.PAY_BY_BANKACCOUNT_TOKEN)) {
                        activeChannel.setStatus(EPaymentChannelStatus.MAINTENANCE);
                    }
                    PaymentChannel channel = new PaymentChannel(activeChannel);
                    channel.f6no = bankAccount.firstaccountno;
                    channel.l4no = bankAccount.lastaccountno;
                    channel.bankcode = bankAccount.bankcode;
                    channel.pmcname = GlobalData.getStringResource(RS.string.zpw_channelname_vietcombank_mapaccount);
                    channel.isBankAccountMap = true;

                    CChannelHelper.inflatChannelIcon(channel, bankAccount.bankcode);
                    //calculate fee
                    channel.calculateFee();

                    //check amount is support or not
                    if (channel.isEnable()) {
                        channel.checkPmcOrderAmount(GlobalData.getOrderAmount());//check amount is support or not
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
            pChannel.pmcid = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount));
        } else if (Constants.CCCode.equals(pChannel.bankcode)) {
            pChannel.pmcid = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card));
        } else {
            pChannel.pmcid = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm));
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
            String mappCardIdList = SharedPreferencesManager.getInstance().getMapCardKeyList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
            if (TextUtils.isEmpty(mappCardIdList)) {
                Log.d(this, "get map card is null");
                return;
            }
            Log.d(this, "map card list " + mappCardIdList);
            for (String mapCardID : mappCardIdList.split(Constants.COMMA)) {
                if (TextUtils.isEmpty(mapCardID)) {
                    continue;
                }
                String strMapCard = SharedPreferencesManager.getInstance().getMapCardByKey(mapCardID); //get card info from cache
                if (TextUtils.isEmpty(strMapCard)) {
                    continue;
                }
                DMappedCard mapCard = GsonUtils.fromJsonString(strMapCard, DMappedCard.class);
                if (mapCard == null) {
                    continue;
                }
                Log.d(this, "map card ", mapCard);
                MiniPmcTransType activeChannel;
                if (GlobalData.isWithDrawChannel()) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(mapCard.bankcode), MiniPmcTransType.class);
                } else if (Constants.CCCode.equals(mapCard.bankcode)) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getCreditCardChannelConfig(mapCard.bankcode), MiniPmcTransType.class);
                } else {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getATMChannelConfig(mapCard.bankcode), MiniPmcTransType.class);
                }
                Log.d(this, "active channel is", activeChannel);
                if (activeChannel != null) {
                    //check this map card is support or not
                    allowPaymentChannel(activeChannel);
                    resetPmc(activeChannel);

                    if (isBankMaintenance(mapCard.bankcode, EBankFunction.PAY_BY_CARD_TOKEN)) {
                        activeChannel.setStatus(EPaymentChannelStatus.MAINTENANCE);
                    }
                    PaymentChannel channel = new PaymentChannel(activeChannel);
                    channel.l4no = mapCard.last4cardno;
                    channel.f6no = mapCard.first6cardno;
                    channel.bankcode = mapCard.bankcode;

                    //calculate fee
                    channel.calculateFee();

                    //check amount is support or not
                    if (channel.isEnable()) {
                        channel.checkPmcOrderAmount(GlobalData.getOrderAmount());//check amount is support or not
                    }

                    if (Constants.CCCode.equals(channel.bankcode)) {
                        CreditCardCheck.getInstance().detectCard(channel.f6no);
                        if (CreditCardCheck.getInstance().isDetected()) {
                            //populate channel name
                            channel.pmcname = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card), CreditCardCheck.getInstance().getDetectedBankName()) + mapCard.last4cardno;
                            ECardType cardType = ECardType.fromString(CreditCardCheck.getInstance().getCodeBankForVerify());
                            CChannelHelper.inflatChannelIcon(channel, cardType.toString());
                        }
                    }
                    //this is atm
                    else {
                        CChannelHelper.inflatChannelIcon(channel, mapCard.bankcode);
                        BankCardCheck.getInstance().detectCard(channel.f6no);
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
    protected boolean isBankMaintenance(String pBankCode, EBankFunction pBankFunction) {
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
                !pmcConfigList.contains(pChannel.getPmcKey(GlobalData.appID, GlobalData.getTransactionType().toString(), pChannel.pmcid)))) {
            pChannel.setStatus(EPaymentChannelStatus.DISABLE);
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
            pmcConfigList = AppInfoLoader.getInstance().getChannelsForApp(String.valueOf(GlobalData.appID), GlobalData.getTransactionType().toString());
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
