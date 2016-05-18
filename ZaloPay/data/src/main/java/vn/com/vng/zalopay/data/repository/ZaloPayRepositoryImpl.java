package vn.com.vng.zalopay.data.repository;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.GetOrderResponse;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayFactory;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class ZaloPayRepositoryImpl implements ZaloPayRepository {

    private ZaloPayFactory zaloPayFactory;
    private ZaloPayEntityDataMapper zaloPayEntityDataMapper;

    public ZaloPayRepositoryImpl(ZaloPayFactory zaloPayFactory, ZaloPayEntityDataMapper zaloPayEntityDataMapper) {
        this.zaloPayFactory = zaloPayFactory;
        this.zaloPayEntityDataMapper = zaloPayEntityDataMapper;
    }

    @Override
    public Observable<Long> balance() {
        return zaloPayFactory.balance();
    }

    @Override
    public Observable<List<TransHistory>> initializeTransHistory() {
        return zaloPayFactory.transactionHistorysServer(0, 1)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
    }

    @Override
    public Observable<List<TransHistory>> loadMoreTransHistory() {
        return null;
    }

    @Override
    public Observable<List<TransHistory>> refreshTransHistory() {
        return null;
    }

    @Override
    public Observable<Order> getOrder(long appId, String zptranstoken) {
        return zaloPayFactory.getOrder(appId, zptranstoken).map(new Func1<GetOrderResponse, Order>() {
            @Override
            public Order call(GetOrderResponse getOrderResponse) {
                getOrderResponse.setAppid(appId);
                getOrderResponse.setZptranstoken(zptranstoken);
                return zaloPayEntityDataMapper.transform(getOrderResponse);
            }
        });
    }

    @Override
    public Observable<Order> createwalletorder(long appId, long amount, int transtype) {
        return zaloPayFactory.createwalletorder(appId, amount, transtype).map(new Func1<GetOrderResponse, Order>() {
            @Override
            public Order call(GetOrderResponse getOrderResponse) {
                getOrderResponse.setAppid(appId);
                getOrderResponse.amount = String.valueOf(amount);
                return zaloPayEntityDataMapper.transform(getOrderResponse);
            }
        });
    }

    @Override
    public Observable<List<TransHistory>> getTransactions(int pageIndex, int count) {
        return zaloPayFactory.transactionHistorysLocal(count)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
    }

    @Override
    public Observable<List<TransHistory>> reloadListTransaction(int count) {
        return zaloPayFactory.transactionHistorysLocal(count)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
    }

    @Override
    public void reloadListTransaction(int count, Subscriber<List<TransHistory>> subscriber) {
        //   zaloPayFactory.reloadListTransactionSync(count, subscriber);
    }

    @Override
    public void getTransactions(int pageIndex, int count, Subscriber<List<TransHistory>> subscriber) {
        // zaloPayFactory.getTransactions(pageIndex, count, subscriber);
    }

    public void requestTransactionsHistory() {
        zaloPayFactory.reloadListTransactionSync(30, null);
    }
}
