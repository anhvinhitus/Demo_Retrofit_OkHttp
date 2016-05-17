package vn.com.vng.zalopay.domain.repository;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.TransHistory;

/**
 * Created by AnhHieu on 5/4/16.
 * <p>
 * Gồm các tính năng của zalo pay : thanh toán, log transition, số dư, order, ...
 */
public interface ZaloPayRepository {

    Observable<Long> balance();

    Observable<List<TransHistory>> initializeTransHistory();

    Observable<List<TransHistory>> loadMoreTransHistory();

    Observable<List<TransHistory>> refreshTransHistory();

    Observable<List<TransHistory>> getTransactions(int pageIndex, int count);

    Observable<List<TransHistory>> reloadListTransaction(int count);

    Observable<Order> getOrder(long appId, String zptranstoken);

    Observable<Order> createwalletorder(long appId, long amount, int transtype);

    void reloadListTransaction(int count, Subscriber<List<TransHistory>> subscriber);

    void getTransactions(int pageIndex, int count, Subscriber<List<TransHistory>> subscriber);

    void requestTransactionsHistory();
}
