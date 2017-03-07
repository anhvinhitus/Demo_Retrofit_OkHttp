package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by AnhHieu on 8/8/16.
 */
public class TransactionChangeEvent {
    public int typeSuccess;

    public TransactionChangeEvent(int typeSuccess) {
        this.typeSuccess = typeSuccess;
    }
}
