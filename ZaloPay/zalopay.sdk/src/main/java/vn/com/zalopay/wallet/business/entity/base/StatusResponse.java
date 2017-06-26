package vn.com.zalopay.wallet.business.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptOutput;

public class StatusResponse extends BaseResponse implements Parcelable {
    public static final Parcelable.Creator<StatusResponse> CREATOR = new Parcelable.Creator<StatusResponse>() {
        @Override
        public StatusResponse createFromParcel(Parcel in) {
            return new StatusResponse(in);
        }

        @Override
        public StatusResponse[] newArray(int size) {
            return new StatusResponse[size];
        }
    };
    public boolean isprocessing = false;
    public String data = null;
    public String zptransid;
    public String suggestmessage = null;
    public int[] suggestaction = null;

    public StatusResponse() {
        data = null;
        zptransid = null;
        isprocessing = false;
        returncode = -1;
        returnmessage = null;
        suggestaction = null;
    }

    public StatusResponse(DAtmScriptOutput pScriptOutput) {
        this.data = null;
        this.returncode = pScriptOutput.eventID;
        this.returnmessage = pScriptOutput.message;
        this.suggestmessage = pScriptOutput.message;
        this.isprocessing = !pScriptOutput.shouldStop;
    }

    public StatusResponse(int pCode, String pMessage) {
        this.data = null;
        this.returncode = pCode;
        this.returnmessage = pMessage;
        this.zptransid = "0";
        this.isprocessing = false;
    }

    public StatusResponse(Parcel in) {
        zptransid = in.readString();
        data = in.readString();
        isprocessing = in.readByte() != 0;
        returncode = in.readInt();
        returnmessage = in.readString();
        accesstoken = in.readString();
        suggestmessage = in.readString();
        suggestaction = in.createIntArray();
    }

    public boolean hasSuggestAction() {
        return suggestaction != null && suggestaction.length >= 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(zptransid);
        parcel.writeString(data);
        parcel.writeByte((byte) (isprocessing ? 1 : 0));
        parcel.writeInt(returncode);
        parcel.writeString(returnmessage);
        parcel.writeString(accesstoken);
        parcel.writeString(suggestmessage);
        parcel.writeIntArray(suggestaction);
    }
}
