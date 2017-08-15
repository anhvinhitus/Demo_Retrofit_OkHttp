package vn.com.vng.zalopay.zpc.model;

/**
 * Created by huuhoa on 8/15/17.
 */

public interface ZPCPickupMode {
    int DEFAULT = 1; // default is not allow User to pickup new number or own number
    int ALLOW_NON_CONTACT_ITEM = 2;
    int ALLOW_OWN_NUMBER = 4;
    int ALLOW_NON_ZALOPAY_USER = 8;
    int ALL = 0xFFFF;
}
