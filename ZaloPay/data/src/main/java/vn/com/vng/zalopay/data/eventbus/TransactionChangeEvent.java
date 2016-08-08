package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by AnhHieu on 8/8/16.
 */
public class TransactionChangeEvent {
    public boolean typeSuccess;

    public TransactionChangeEvent(boolean typeSuccess) {
        this.typeSuccess = typeSuccess;
    }
}
