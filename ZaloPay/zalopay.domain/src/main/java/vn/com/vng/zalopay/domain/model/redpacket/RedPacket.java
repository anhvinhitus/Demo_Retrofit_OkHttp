package vn.com.vng.zalopay.domain.model.redpacket;

import java.util.List;

import vn.com.vng.zalopay.domain.model.Person;

/**
 * Created by longlv on 14/07/2016.
 * RedPacket save redPackage that user sent
 */
public class RedPacket {
    public long bundleId;
    public int quantity;
    public long totalLuck;
    public long amountEach;
    public long type;
    public String sendMessage;
    public List<Person> friends;
    public long state;

    public enum RedPacketState {
        CREATE(0), SENT(1);
        private int value;

        RedPacketState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public RedPacket() {

    }
//
//    public RedPacket(long bundleId, int quantity, long totalLuck, long amountEach, int type, String sendMessage) {
//        this.bundleId = bundleId;
//        this.quantity = quantity;
//        this.totalLuck = totalLuck;
//        this.amountEach = amountEach;
//        this.type = type;
//        this.sendMessage = sendMessage;
//        this.friends = new ArrayList<>();
//        this.state = RedPacketState.CREATE.getValue();
//    }
//
//    public RedPacket(long bundleId, int quantity, long totalLuck, long amountEach, int type, String sendMessage, List<Person> friends, int state) {
//        this.bundleId = bundleId;
//        this.quantity = quantity;
//        this.totalLuck = totalLuck;
//        this.amountEach = amountEach;
//        this.type = type;
//        this.sendMessage = sendMessage;
//        this.friends = friends;
//        this.state = state;
//    }
}
