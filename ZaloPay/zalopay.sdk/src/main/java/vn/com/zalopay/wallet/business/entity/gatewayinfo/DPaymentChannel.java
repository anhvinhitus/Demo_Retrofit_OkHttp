package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import com.google.gson.Gson;

import vn.com.zalopay.wallet.business.behavior.view.paymentfee.CBaseCalculateFee;
import vn.com.zalopay.wallet.business.behavior.view.paymentfee.CPaymentCalculateFee;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EFeeCalType;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentChannelStatus;
import vn.com.zalopay.wallet.utils.Log;

/***
 * channel class
 */
public class DPaymentChannel {

    public int pmcid = 0;
    public String pmcname = null;
    public EPaymentChannelStatus status = EPaymentChannelStatus.DISABLE;
    public long minvalue = -1;
    public long maxvalue = -1;
    public double discount = -1;
    public double feerate = -1;
    public double minfee = -1;
    public EFeeCalType feecaltype = EFeeCalType.SUM;

    public double totalfee = 0;

    public int requireotp = 1;

    public long amountrequireotp = 0;

    public boolean isBankAccountMap = false;

    /***
     * rule - still show channel not allow in channel list (status = 0) , each channel have 2 policy to allow or not
     * 1. user level - depend on user table map
     * 2. transaction amount not in range supported by channel
     * 3. withdraw need to check fee + amount <= balance
     */
    private boolean isAllowByAmount = true;
    private boolean isAllowByLevel = true;

    private boolean isAllowByAmountAndFee = true;

    public DPaymentChannel() {
    }

    /***
     * copy constructor
     *
     * @param channel
     */
    public DPaymentChannel(DPaymentChannel channel) {
        this.pmcid = channel.pmcid;
        this.pmcname = channel.pmcname;
        this.status = channel.status;
        this.minvalue = channel.minvalue;
        this.maxvalue = channel.maxvalue;
        this.discount = channel.discount;
        this.feecaltype = channel.feecaltype;
        this.minfee = channel.minfee;

        this.feerate = channel.feerate;
        this.feecaltype = channel.feecaltype;
        this.totalfee = channel.totalfee;

        this.requireotp = channel.requireotp;
        this.amountrequireotp = channel.amountrequireotp;

        this.isAllowByAmount = channel.isAllowByAmount;
        this.isAllowByLevel = channel.isAllowByLevel;
        this.isBankAccountMap = channel.isBankAccountMap;
    }

    /***
     * require otp depend on transaction amount
     *
     * @return
     */
    public boolean isNeedToCheckTransactionAmount() {
        return amountrequireotp > 0 ? true : false;
    }

    /***
     * calculate fee
     */
    public void calculateFee() {
        this.totalfee = CBaseCalculateFee.getInstance().setCalculator(new CPaymentCalculateFee(this)).countFee();
    }

    public boolean hasFee() {
        return totalfee > 0;
    }

    public boolean isRequireOtp() {
        return requireotp == 1;
    }

    public DPaymentChannel fromJsonString(String pJson) {
        if (pJson == null)
            return new DPaymentChannel();

        return (new Gson()).fromJson(pJson, this.getClass());
    }

    public boolean isPromoted() {
        return discount > -1;
    }

    public boolean isEnable() {
        return status == EPaymentChannelStatus.ENABLE;
    }

    public void setStatus(EPaymentChannelStatus pStatus) {
        status = pStatus;
    }

    /***
     * whether transaction amount in range of this channel support
     *
     * @param pAmount
     * @return
     */
    public boolean isAmountSupport(long pAmount) {
        if (pAmount <= 0) {
            return false;
        }

        if (minvalue == -1 && maxvalue == -1)
            return true;
        if (minvalue == -1 && pAmount <= maxvalue)
            return true;
        if (maxvalue == -1 && pAmount >= minvalue)
            return true;
        if (pAmount >= minvalue && pAmount <= maxvalue)
            return true;

        return false;
    }

    public boolean isChannel(String pChannelId) {
        try {
            return this.pmcid == Integer.parseInt(pChannelId);
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return false;
    }

    public boolean isZaloPayChannel() {
        return isChannel(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay));
    }

    public boolean isCreditCardChannel() {
        return isChannel(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card));
    }

    public boolean isBankAccount() {
        return isChannel(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount));
    }

    /***
     * status must be 0
     *
     * @return
     */
    public boolean isAllowByAmount() {
        return isAllowByAmount;
    }

    public void setAllowByAmount(boolean allowByAmount) {
        isAllowByAmount = allowByAmount;
    }

    /***
     * status must be 0
     *
     * @return
     */
    public boolean isAllowByLevel() {
        return isAllowByLevel;
    }

    public void setAllowByLevel(boolean allowByLevel) {
        isAllowByLevel = allowByLevel;
    }

    public boolean isAllowByAmountAndFee() {
        return isAllowByAmountAndFee;
    }

    public void setAllowByAmountAndFee(boolean allowByAmountAndFee) {
        isAllowByAmountAndFee = allowByAmountAndFee;
    }

    public boolean isMaintenance() {
        return status == EPaymentChannelStatus.MAINTENANCE;
    }

    public boolean isBankAccountMap() {
        return isBankAccountMap;
    }

}
