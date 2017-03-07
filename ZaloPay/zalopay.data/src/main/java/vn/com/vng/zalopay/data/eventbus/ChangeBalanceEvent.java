package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by AnhHieu on 5/18/16.
 */
public class ChangeBalanceEvent {
    public long balance;

    public ChangeBalanceEvent(long balance) {
        this.balance = balance;
    }
}
