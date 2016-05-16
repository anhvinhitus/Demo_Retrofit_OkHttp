package vn.com.vng.zalopay.interactor.event;

/**
 * Created by AnhHieu on 5/16/16.
 */
public class ChangeBalanceEvent {

    public long balance;

    public ChangeBalanceEvent(long balance) {
        this.balance = balance;
    }
}
