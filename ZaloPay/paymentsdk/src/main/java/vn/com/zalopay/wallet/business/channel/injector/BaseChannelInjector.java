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
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.CChannelHelper;
import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public abstract class BaseChannelInjector {
    public static final int MIN_VALUE_CHANNEL = 1000000000;
    public static final int MAX_VALUE_CHANNEL = -1;

    protected List<DPaymentChannelView> mChannelList = new ArrayList<>();

    protected ArrayList<String> pmcConfigList = new ArrayList<>();

    private double mMinValue = MIN_VALUE_CHANNEL, mMaxValue = MAX_VALUE_CHANNEL;

    /***
     * adapter create channel injector
     *
     * @return
     */
    public static BaseChannelInjector createChannelInjector() {
        if (GlobalData.isTranferMoneyChannel())
            return new TranferChannelInjector();
        else if (GlobalData.isWithDrawChannel())
            return new WithDrawChannelInjector();
        else
            return new PaymentChannelInjector();
    }

    protected abstract void detectChannel(ZPWOnGetChannelListener pListener) throws Exception;

    /***
     * get channel from pmc list
     */
    protected void getChannelFromConfig() {
        for (String pmcID : pmcConfigList) {
            try {
                DPaymentChannel activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getPmcConfigByPmcID(pmcID), DPaymentChannel.class);

                DPaymentChannelView channel = new DPaymentChannelView(activeChannel);

                //calculate fee of this channel
                channel.calculateFee();

                if (channel.isCreditCardChannel()) {
                    channel.bankcode = Constants.CCCode;
                }
                if (channel.isBankAccount()) {
                    channel.bankcode = GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank);
                }

                //check amount is support or not
                if (channel.isEnable()) {
                    checkSupportAmount(channel);

                    //check maintenance for cc
                    if ((channel.isCreditCardChannel() && isBankMaintenance(channel.bankcode, EBankFunction.PAY_BY_CARD))
                            || (channel.isBankAccount() && isBankMaintenance(channel.bankcode, EBankFunction.PAY_BY_BANK_ACCOUNT))) {
                        channel.setStatus(EPaymentChannelStatus.MAINTENANCE);
                    }
                }

                //get icon
                CChannelHelper.inflatChannelIcon(channel, null);

                //get min/max amount
                findValue(channel);

                //user has linked vietcombank account , no need show bank account channel
                if (channel.isBankAccount() && BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId,
                        GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank))) {
                    continue;
                }

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
        if (mChannelList == null || mChannelList.size() <= 1)
            return;

        ArrayList<DPaymentChannelView> tempArr = new ArrayList<>();

        for (Iterator<DPaymentChannelView> iterator = mChannelList.iterator(); iterator.hasNext(); ) {
            DPaymentChannelView channel = iterator.next();

            if (!channel.isEnable() || !channel.isAllowByAmount() || channel.isMaintenance()) {
                tempArr.add(channel.clone());

                iterator.remove();
            }
        }

        if (tempArr.size() > 0) {
            for (DPaymentChannelView channelView : tempArr) {
                mChannelList.add(channelView);
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
            String mapBankAccountKeyList = SharedPreferencesManager.getInstance().getBankAccountKeyList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);

            if (TextUtils.isEmpty(mapBankAccountKeyList)) {
                Log.d(this, "===getMappedBankAccount===mapBankAccountKeyList=NULL");
                return;
            }
            Log.d(this,"===getMapBankAccount===mapBankAccountKeyList="+mapBankAccountKeyList);
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

                DPaymentChannel activeChannel = null;

                ECardType eCardType = ECardType.fromString(bankAccount.bankcode);

                if (eCardType != ECardType.UNDEFINE && ECardType.isBankAccount(eCardType)) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankAccountChannelConfig(), DPaymentChannel.class);
                }

                Log.d(this, activeChannel != null ? GsonUtils.toJsonString(activeChannel) : "activeChannel is null");

                if (activeChannel != null) {
                    //check this map card/map bankaccount is support or not
                    checkAllowBankAccount(activeChannel);

                    if (isBankMaintenance(bankAccount.bankcode, EBankFunction.PAY_BY_BANKACCOUNT_TOKEN)) {
                        activeChannel.setStatus(EPaymentChannelStatus.MAINTENANCE);
                    }

                    DPaymentChannelView channel = new DPaymentChannelView(activeChannel);

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
                        checkSupportAmount(channel);
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
                Log.d(this,"===getMapCard===mappCardIdList=NULL");
                return;
            }
            Log.d(this,"===getMapCard===mappCardIdList="+mappCardIdList);
            for (String mapCardID : mappCardIdList.split(Constants.COMMA)) {
                if (TextUtils.isEmpty(mapCardID))
                    continue;

                //get card info from cache.
                String strMapCard = SharedPreferencesManager.getInstance().getMapCardByKey(mapCardID);

                if (TextUtils.isEmpty(strMapCard))
                    continue;

                DMappedCard mappCard = GsonUtils.fromJsonString(strMapCard, DMappedCard.class);

                if (mappCard == null)
                    continue;

                DPaymentChannel activeChannel;

                boolean isBankCard = false;

                ECardType eCardType = ECardType.fromString(mappCard.bankcode);

                if (eCardType != ECardType.UNDEFINE) {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getATMChannelConfig(), DPaymentChannel.class);

                    isBankCard = true;
                } else {
                    activeChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getCreditCardChannelConfig(), DPaymentChannel.class);
                }

                Log.d(this, activeChannel != null ? GsonUtils.toJsonString(activeChannel) : "activeChannel is null");

                if (activeChannel != null) {
                    //check this map card is support or not
                    if (isBankCard) {
                        checkAllowMapCardBank(activeChannel);
                    } else {
                        checkAllowMapCardCC(activeChannel);
                    }

                    if (isBankMaintenance(mappCard.bankcode, EBankFunction.PAY_BY_CARD_TOKEN)) {
                        activeChannel.setStatus(EPaymentChannelStatus.MAINTENANCE);
                    }

                    DPaymentChannelView channel = new DPaymentChannelView(activeChannel);

                    channel.l4no = mappCard.last4cardno;
                    channel.f6no = mappCard.first6cardno;
                    channel.bankcode = mappCard.bankcode;

                    //calculate fee
                    channel.calculateFee();

                    //check amount is support or not
                    if (channel.isEnable()) {
                        checkSupportAmount(channel);
                    }

                    //this is bank
                    if (isBankCard) {
                        CChannelHelper.inflatChannelIcon(channel, mappCard.bankcode);

                        BankCardCheck.getInstance().detectCard(channel.f6no);

                        if (BankCardCheck.getInstance().isDetected()) {
                            //populate channel name
                            String bankName = BankCardCheck.getInstance().getDetectedBankName();

                            if (TextUtils.isEmpty(bankName)) {
                                bankName = GlobalData.getStringResource(RS.string.zpw_save_credit_card_default);
                            } else if (bankName.startsWith("NH")) {
                                bankName = bankName.substring(2);

                                bankName = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card_atm), bankName);
                            } else {
                                bankName = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card), bankName);
                            }

                            channel.pmcname = bankName + mappCard.last4cardno;
                        }
                    }
                    //this is cc
                    else {
                        CreditCardCheck.getInstance().detectCard(channel.f6no);

                        if (CreditCardCheck.getInstance().isDetected()) {

                            //populate channel name
                            channel.pmcname = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card), CreditCardCheck.getInstance().getDetectedBankName()) + mappCard.last4cardno;

                            ECardType cardType = ECardType.fromString(CreditCardCheck.getInstance().getCodeBankForVerify());

                            CChannelHelper.inflatChannelIcon(channel, cardType.toString());
                        }

                    }

                    if (!CreditCardCheck.getInstance().isDetected() && !BankCardCheck.getInstance().isDetected()) {
                        //channel name
                        channel.pmcname = GlobalData.getStringResource(RS.string.zpw_save_credit_card_default) + mappCard.last4cardno;
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

    /***
     * load map card for withddraw
     * user just can withdraw to map card atm
     *
     * @throws Exception
     */
    protected void loadMapCardFromCacheForWithDraw() throws Exception {
        //get list of mapped card from cached.
        String mappCardIdList = SharedPreferencesManager.getInstance().getMapCardKeyList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);

        Log.d(this, "===loadMapCardFromCacheForWithDraw===" + mappCardIdList);

        if (TextUtils.isEmpty(mappCardIdList))
            return;

        //get zalopay channel from cache
        DPaymentChannel zaloPayChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(), DPaymentChannel.class);

        //get min/max amount
        findValue(zaloPayChannel);

        for (String mappCardID : mappCardIdList.split(Constants.COMMA)) {
            if (TextUtils.isEmpty(mappCardID)) {
                continue;
            }

            //get card info from cache.
            String strMappedCard = SharedPreferencesManager.getInstance().getMapCardByKey(mappCardID);

            if (TextUtils.isEmpty(strMappedCard))
                continue;

            DMappedCard mappCard = GsonUtils.fromJsonString(strMappedCard, DMappedCard.class);

            if (mappCard == null) {
                Log.d(this, strMappedCard + " is null on cached");

                continue;
            }

            if (zaloPayChannel != null) {
                Log.d(this, "===zalo channel ===" + GsonUtils.toJsonString(zaloPayChannel));

                DPaymentChannelView channel = new DPaymentChannelView(zaloPayChannel);

                channel.l4no = mappCard.last4cardno;
                channel.f6no = mappCard.first6cardno;
                channel.bankcode = mappCard.bankcode;

                //check amount is support or not
                if (channel.isEnable())
                    checkSupportAmount(channel);

                //atm
                if (!mappCard.bankcode.equals(Constants.CCCode)) {
                    channel.pmcid = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm));

                    CChannelHelper.inflatChannelIcon(channel, mappCard.bankcode);

                    BankCardCheck.getInstance().detectCard(channel.f6no);

                    if (BankCardCheck.getInstance().isDetected()) {
                        //populate channel name
                        String bankName = BankCardCheck.getInstance().getDetectedBankName();

                        if (TextUtils.isEmpty(bankName)) {
                            bankName = GlobalData.getStringResource(RS.string.zpw_save_credit_card_default);
                        } else if (bankName.startsWith("NH")) {
                            bankName = bankName.substring(2);

                            bankName = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card_atm), bankName);
                        } else {
                            bankName = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card), bankName);
                        }

                        channel.pmcname = bankName + mappCard.last4cardno;
                    }
                }
                //cc
                else {
                    channel.pmcid = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card));

                    CreditCardCheck.getInstance().detectCard(channel.f6no);

                    if (CreditCardCheck.getInstance().isDetected()) {

                        //populate channel name
                        channel.pmcname = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card), CreditCardCheck.getInstance().getDetectedBankName()) + mappCard.last4cardno;

                        ECardType cardType = ECardType.fromString(CreditCardCheck.getInstance().getCodeBankForVerify());

                        CChannelHelper.inflatChannelIcon(channel, cardType.toString());
                    }

                }

                if (!CreditCardCheck.getInstance().isDetected() && !BankCardCheck.getInstance().isDetected()) {
                    //channel name
                    channel.pmcname = GlobalData.getStringResource(RS.string.zpw_save_credit_card_default) + mappCard.last4cardno;
                }

                if (!mChannelList.contains(channel))
                    mChannelList.add(channel);
            }
        }
    }

    /***
     * load map bank account for withddraw
     * user just can withdraw to map card atm
     *
     * @throws Exception
     */
    protected void loadBankAccountFromCacheForWithDraw() throws Exception {
        //get list of mapped card from cached.
        String bankAccountKeyList = SharedPreferencesManager.getInstance().getBankAccountKeyList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);

        Log.d(this, "===loadBankAccountFromCacheForWithDraw===" + bankAccountKeyList);

        if (TextUtils.isEmpty(bankAccountKeyList)) {
            return;
        }

        //get zalopay channel from cache
        DPaymentChannel zaloPayChannel = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getZaloPayChannelConfig(), DPaymentChannel.class);

        //get min/max amount
        findValue(zaloPayChannel);

        for (String bankAccountKey : bankAccountKeyList.split(Constants.COMMA)) {
            if (TextUtils.isEmpty(bankAccountKey)) {
                continue;
            }

            //get card info from cache.
            String strBankAccount = SharedPreferencesManager.getInstance().getMapCardByKey(bankAccountKey);

            if (TextUtils.isEmpty(strBankAccount)) {
                continue;
            }

            DBankAccount bankAccount = GsonUtils.fromJsonString(strBankAccount, DBankAccount.class);

            if (bankAccount == null) {
                Log.d(this, bankAccount + " is null on cached");

                continue;
            }

            if (zaloPayChannel != null) {
                Log.d(this, "===zalo channel ===" + GsonUtils.toJsonString(zaloPayChannel));

                DPaymentChannelView channel = new DPaymentChannelView(zaloPayChannel);

                channel.f6no = bankAccount.getFirstNumber();
                channel.l4no = bankAccount.getLastNumber();
                channel.bankcode = bankAccount.bankcode;
                channel.pmcname = GlobalData.getStringResource(RS.string.zpw_channelname_vietcombank_mapaccount);
                channel.pmcid = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount));
                channel.isBankAccountMap = true;

                CChannelHelper.inflatChannelIcon(channel, bankAccount.bankcode);
                //check amount is support or not
                if (channel.isEnable())
                    checkSupportAmount(channel);

                if (!mChannelList.contains(channel))
                    mChannelList.add(channel);
            }
        }
    }

    protected void addChannelToList(DPaymentChannelView pChannel) {

        if (pChannel != null) {
            //sometimes network not stable, so 2 channels same in listview, we must exclude it if it existed
            if (mChannelList.contains(pChannel))
                return;

            //add to list
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
     * check amount support and set flag is AlllowByAmount
     *
     * @param pChannel
     * @return
     */
    protected boolean checkSupportAmount(DPaymentChannel pChannel) {
        if (pChannel == null)
            return false;

        boolean isSupportAmount = pChannel.isAmountSupport((long) (GlobalData.getOrderAmount() + pChannel.totalfee));

        pChannel.setAllowByAmount(isSupportAmount);

        return isSupportAmount;
    }

    /***
     * this is atm map card have in pmclist
     *
     * @param pChannel
     * @return
     */
    protected boolean checkAllowMapCardBank(DPaymentChannel pChannel) {
        boolean isSupportAtm = false;

        if (pmcConfigList != null && pmcConfigList.size() > 0 &&
                pmcConfigList.contains(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm))) {
            isSupportAtm = true;
        }

        if (!isSupportAtm)
            pChannel.setStatus(EPaymentChannelStatus.DISABLE);

        return isSupportAtm;
    }

    protected boolean checkAllowBankAccount(DPaymentChannel pChannel) {
        boolean isSupportBankAccount = false;

        if (pmcConfigList != null && pmcConfigList.size() > 0 &&
                pmcConfigList.contains(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount))) {
            isSupportBankAccount = true;
        }

        if (!isSupportBankAccount)
            pChannel.setStatus(EPaymentChannelStatus.DISABLE);

        return isSupportBankAccount;
    }

    /***
     * is this cc map card have in pmclist
     *
     * @param pChannel
     * @return
     */
    protected boolean checkAllowMapCardCC(DPaymentChannel pChannel) {
        boolean isSupportCC = false;

        if (pmcConfigList != null && pmcConfigList.size() > 0
                && pmcConfigList.contains(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card))) {
            isSupportCC = true;
        }

        if (!isSupportCC)
            pChannel.setStatus(EPaymentChannelStatus.DISABLE);

        return isSupportCC;
    }

    /***
     * detect min/max value
     *
     * @param activeChannel
     */
    public void findValue(DPaymentChannel activeChannel) {
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
        for (Iterator<DPaymentChannelView> iterator = mChannelList.iterator(); iterator.hasNext(); ) {
            DPaymentChannelView channelView = iterator.next();

            boolean isExist = false;

            for (int i = 0; i < pChannels.length; i++) {
                if (pChannels[i] == channelView.pmcid) {
                    isExist = true;
                    break;
                }
            }
            //remove it
            if (!isExist) {
                Log.d("filterForceChannel", "===remove channel by force" + GsonUtils.toJsonString(channelView));
                iterator.remove();
            }

        }
    }

    public List<DPaymentChannelView> getChannelList() {
        return mChannelList;
    }

    public boolean isChannelEmpty() {
        return mChannelList == null || mChannelList.size() <= 0;
    }

    public boolean isChannelUnique() {
        return mChannelList != null && mChannelList.size() <= 1;
    }

    public DPaymentChannelView getFirstChannel() {
        if (mChannelList != null && mChannelList.size() >= 1)
            return mChannelList.get(0);

        return null;
    }

    public DPaymentChannelView getChannelAtPosition(int pPosition) {
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
