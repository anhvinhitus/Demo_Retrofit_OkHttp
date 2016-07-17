package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longlv on 14/07/2016.
 * RedPackage save redPackage that user sent
 */
public class RedPackage extends AbstractData {
    public long bundleId;
    public int quantity;
    public long totalLuck;
    public long amountEach;
    public int type;
    public String sendMessage;
    public List<Person> friends;
    public int state;

    public enum RedPackageState {
        CREATE(0), SENT(1);
        private int value;

        RedPackageState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public RedPackage(long bundleId, int quantity, long totalLuck, long amountEach, int type, String sendMessage) {
        this.bundleId = bundleId;
        this.quantity = quantity;
        this.totalLuck = totalLuck;
        this.amountEach = amountEach;
        this.type = type;
        this.sendMessage = sendMessage;
        this.friends = new ArrayList<>();
        this.state = RedPackageState.CREATE.getValue();
    }

    public RedPackage(long bundleId, int quantity, long totalLuck, long amountEach, int type, String sendMessage, List<Person> friends, int state) {
        this.bundleId = bundleId;
        this.quantity = quantity;
        this.totalLuck = totalLuck;
        this.amountEach = amountEach;
        this.type = type;
        this.sendMessage = sendMessage;
        this.friends = friends;
        this.state = state;
    }

    public RedPackage(Parcel source) {
        source.writeLong(bundleId);
        source.writeInt(quantity);
        source.writeLong(totalLuck);
        source.writeLong(amountEach);
        source.writeInt(type);
        source.writeString(sendMessage);
        source.writeList(friends);
        source.writeInt(state);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        bundleId = dest.readLong();
        quantity = dest.readInt();
        totalLuck = dest.readInt();
        amountEach = dest.readLong();
        type = dest.readInt();
        sendMessage = dest.readString();
        dest.readList(friends, null);
    }

    public final Parcelable.Creator<RedPackage> CREATOR = new Parcelable.Creator<RedPackage>() {
        @Override
        public RedPackage createFromParcel(Parcel source) {
            return new RedPackage(source);
        }

        @Override
        public RedPackage[] newArray(int size) {
            return new RedPackage[size];
        }
    };
}
