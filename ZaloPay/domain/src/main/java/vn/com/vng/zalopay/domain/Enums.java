package vn.com.vng.zalopay.domain;

/**
 * Created by AnhHieu on 3/24/16.
 */
public class Enums {

    public enum Platform {
        IOS((short) 1),
        ANDROID((short) 2);
        private short mId;

        Platform(short id) {
            mId = id;
        }

        public short getId() {
            return mId;
        }
    }

    public enum BankCard {
        MASTERCARD((short) 1),
        VISA((short) 2),
        JCB((short) 3);

        private final short id;

        BankCard(short id) {
            this.id = id;
        }

        public short getId() {
            return id;
        }
    }
}
