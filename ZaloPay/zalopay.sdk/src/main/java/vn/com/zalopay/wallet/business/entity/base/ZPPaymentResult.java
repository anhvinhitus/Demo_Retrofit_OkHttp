package vn.com.zalopay.wallet.business.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentStatus;

/***
 * result payment callback to app
 */
public class ZPPaymentResult implements Parcelable{
    //payment info app tranfer to sdk
    public ZPWPaymentInfo paymentInfo;
    //payment status: success,fail,processing...
    public EPaymentStatus paymentStatus;

    public String channelID;
    public String channelDetail;

    //user pay success,sdk auto map card,return mapped card to app to show linkcard tutorial page
    public DMapCardResult mapCardResult;

    public ZPPaymentResult(ZPWPaymentInfo pPaymentInfo, EPaymentStatus pPaymentStatus) {
        this.paymentInfo = pPaymentInfo;
        this.paymentStatus = pPaymentStatus;
    }

    protected ZPPaymentResult(Parcel in) {
        channelID = in.readString();
        channelDetail = in.readString();
    }

    public static final Creator<ZPPaymentResult> CREATOR = new Creator<ZPPaymentResult>() {
        @Override
        public ZPPaymentResult createFromParcel(Parcel in) {
            return new ZPPaymentResult(in);
        }

        @Override
        public ZPPaymentResult[] newArray(int size) {
            return new ZPPaymentResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(channelID);
        parcel.writeString(channelDetail);
    }
}
