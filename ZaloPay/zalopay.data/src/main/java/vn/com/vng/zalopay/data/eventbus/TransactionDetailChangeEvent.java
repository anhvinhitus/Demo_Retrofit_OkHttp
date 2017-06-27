package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by khattn on 6/21/17.
 */

public class TransactionDetailChangeEvent {
    public long transid;

    public TransactionDetailChangeEvent(long transid) {
        this.transid = transid;
    }
}
