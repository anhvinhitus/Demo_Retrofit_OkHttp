package vn.com.vng.zalopay.domain;

/**
 * Created by AnhHieu on 3/24/16.
 */
public class Enums {

    public enum Platform {
        IOS(1),
        ANDROID(2),
        WINDOW_PHONE(3);
        private int mId;

        Platform(int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }
    }

    public enum NotificationState {
        UNREAD(1),
        VIEW(2),
        READ(3);

        private int mId;

        NotificationState(int id) {
            this.mId = id;
        }

        public int getId() {
            return mId;
        }
    }
}