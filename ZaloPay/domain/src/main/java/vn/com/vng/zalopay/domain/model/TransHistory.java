package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class TransHistory extends AbstractData {

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public TransHistory(Parcel source) {
    }

    public static final Parcelable.Creator<TransHistory> CREATOR = new Parcelable.Creator<TransHistory>() {
        @Override
        public TransHistory createFromParcel(Parcel source) {
            return new TransHistory(source);
        }

        @Override
        public TransHistory[] newArray(int size) {
            return new TransHistory[size];
        }
    };
}
