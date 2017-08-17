package vn.com.zalopay.wallet.entity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;

import vn.com.zalopay.utility.SdkUtils;

/*
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
    public int errorCode;

    public Feedback(byte[] imgByteArray, String description, String category, String transID, int pErrorCode) {
        this.imgByteArray = imgByteArray;
        this.transID = transID;
        this.category = category;
        this.description = description;
        this.errorCode = pErrorCode;
    }

    protected Feedback(Parcel in) {
        this.imgByteArray = in.createByteArray();
        this.transID = in.readString();
        this.category = in.readString();
        this.description = in.readString();
        this.errorCode = in.readInt();
    }

    public static Feedback collectFeedBack(Activity pContext, String transTitle, String transFailReason,
                                           int pErrorCode, String pTransId) throws Exception {
        Bitmap mBitmap = SdkUtils.getScreenShot(pContext);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (mBitmap != null) {
            mBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
        }
        byte[] byteArray = stream.toByteArray();
        return new Feedback(byteArray, transFailReason, transTitle, pTransId, pErrorCode);
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
        dest.writeInt(this.errorCode);
    }
}
