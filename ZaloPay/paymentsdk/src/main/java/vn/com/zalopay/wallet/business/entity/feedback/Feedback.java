package vn.com.zalopay.wallet.business.entity.feedback;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lytm on 06/01/2017.
 */
public class Feedback implements Parcelable {
    public static final Creator<Feedback> CREATOR = new Creator<Feedback>() {
        @Override
        public Feedback createFromParcel(Parcel source) {
            return new Feedback(source);
        }

        @Override
        public Feedback[] newArray(int size) {
            return new Feedback[size];
        }
    };
    public byte[] imgByteArray;
    public String transID;
    public String category;
    public String description;

    public Feedback(byte[] imgByteArray, String description, String category, String transID) {
        this.imgByteArray = imgByteArray;
        this.transID = transID;
        this.category = category;
        this.description = description;
    }

    protected Feedback(Parcel in) {
        this.imgByteArray = in.createByteArray();
        this.transID = in.readString();
        this.category = in.readString();
        this.description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.imgByteArray);
        dest.writeString(this.transID);
        dest.writeString(this.category);
        dest.writeString(this.description);
    }
}
