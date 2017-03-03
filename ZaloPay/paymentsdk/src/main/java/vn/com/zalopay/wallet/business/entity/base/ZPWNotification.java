package vn.com.zalopay.wallet.business.entity.base;

/**
 * Created by cpu11843-local on 1/6/17.
 */

public class ZPWNotification {
    private int type;
    private String msg;

    public ZPWNotification(int pType, String pMsg) {
        this.type = pType;
        this.msg = pMsg;
    }

    public int getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }
}
