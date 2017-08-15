package vn.com.zalopay.wallet.entity.bank;

public class BankNotification {
    private int type;
    private String msg;

    public BankNotification(int pType, String pMsg) {
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
