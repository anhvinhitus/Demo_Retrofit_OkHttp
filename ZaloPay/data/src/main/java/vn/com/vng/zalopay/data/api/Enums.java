package vn.com.vng.zalopay.data.api;

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
}
