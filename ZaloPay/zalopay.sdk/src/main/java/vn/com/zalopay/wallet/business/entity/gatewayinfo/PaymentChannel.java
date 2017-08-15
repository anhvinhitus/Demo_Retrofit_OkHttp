package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class PaymentChannel extends MiniPmcTransType {
    @SerializedName("channel_icon")
    public String channel_icon;
    @SerializedName("channel_next_icon")
    public String channel_next_icon;
    @SerializedName("l4no")
    public String l4no;
    @SerializedName("f6no")
    public String f6no;
    @SerializedName("select")
    public boolean select = false;//status select on list view
    @SerializedName("position")
    public int position = -1;
    @SerializedName("fullLine")
    public boolean fullLine = false;

    public PaymentChannel() {
    }

    public PaymentChannel(MiniPmcTransType channel) {
        super(channel);
    }

    public String cardKey() {
        return f6no + l4no;
    }

    public boolean isMapValid() {
        return !TextUtils.isEmpty(f6no) && !TextUtils.isEmpty(l4no);
    }

    public boolean isNewAtmChannel() {
        return isAtmChannel() && !isMapValid();
    }

    @Override
    public boolean isMapCardChannel() {
        return !TextUtils.isEmpty(this.f6no);
    }

    @Override
    public int hashCode() {
        return this.pmcid;
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;
        if (object instanceof PaymentChannel) {
            PaymentChannel other = (PaymentChannel) object;

            if (!TextUtils.isEmpty(((PaymentChannel) object).pmcname)) {
                sameSame = this.pmcname.equals(other.pmcname);
            }
        }
        return sameSame;
    }

    public boolean compareToCardNumber(String pCardNumber) {
        if (TextUtils.isEmpty(pCardNumber) || pCardNumber.length() < 6) {
            return false;
        }
        String first6cardno = pCardNumber.substring(0, 6);
        String last4cardno = pCardNumber.substring(pCardNumber.length() - 4, pCardNumber.length());
        return !TextUtils.isEmpty(this.l4no) && !TextUtils.isEmpty(this.f6no) && this.f6no.equals(first6cardno) && this.l4no.equals(last4cardno);
    }
}
