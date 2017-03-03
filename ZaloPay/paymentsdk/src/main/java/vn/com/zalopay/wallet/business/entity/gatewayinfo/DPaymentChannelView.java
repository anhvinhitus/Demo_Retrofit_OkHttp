package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import android.text.TextUtils;

public class DPaymentChannelView extends DPaymentChannel {
    public String channel_icon;
    public String channel_next_icon;
    public String l4no;
    public String f6no;
    public String bankcode;

    public DPaymentChannelView(DPaymentChannel channel) {
        super(channel);
    }

    public DPaymentChannelView clone() {
        DPaymentChannelView paymentChannel = new DPaymentChannelView(this);

        paymentChannel.channel_icon = this.channel_icon;
        paymentChannel.channel_next_icon = this.channel_next_icon;
        paymentChannel.l4no = this.l4no;
        paymentChannel.f6no = this.f6no;
        paymentChannel.bankcode = this.bankcode;

        return paymentChannel;
    }

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

        if (object != null && object instanceof DPaymentChannelView) {
            DPaymentChannelView other = (DPaymentChannelView) object;

            if (object != null && !TextUtils.isEmpty(((DPaymentChannelView) object).pmcname))
                sameSame = this.pmcname.equals(other.pmcname);
        }

        return sameSame;
    }

    public boolean isCardNumber(String pCardNumber) {
        if (TextUtils.isEmpty(pCardNumber) || pCardNumber.length() < 6) {
            return false;
        }

        String first6cardno = pCardNumber.substring(0, 6);
        String last4cardno = pCardNumber.substring(pCardNumber.length() - 4, pCardNumber.length());

        return !TextUtils.isEmpty(this.l4no) && !TextUtils.isEmpty(this.f6no) &&
                this.f6no.equals(first6cardno) && this.l4no.equals(last4cardno);
    }
}
