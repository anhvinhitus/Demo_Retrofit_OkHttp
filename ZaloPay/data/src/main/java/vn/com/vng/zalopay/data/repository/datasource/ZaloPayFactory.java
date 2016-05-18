package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.response.GetOrderResponse;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class ZaloPayFactory {

    private Context context;

    private ZaloPayService zaloPayService;

    private HashMap<String, String> params;

    private User user;

    private SqlZaloPayScope sqlZaloPayScope;

    private final int LENGTH_TRANS_HISTORY = 25;

    private final int payAppId;

    private EventBus eventBus;

    public ZaloPayFactory(Context context, ZaloPayService service,
                          User user, SqlZaloPayScope sqlZaloPayScope, int payAppId, EventBus eventBus) {

        if (context == null || service == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.zaloPayService = service;
        this.user = user;
        this.sqlZaloPayScope = sqlZaloPayScope;
        this.payAppId = payAppId;

        this.eventBus = eventBus;
    }

    public Observable<List<TransHistoryEntity>> transactionHistorysServer(long timestamp, int order) {
        return zaloPayService.transactionHistorys(user.uid, user.accesstoken, timestamp, LENGTH_TRANS_HISTORY, order)
                .map(transactionHistoryResponse -> transactionHistoryResponse.data)


                .doOnNext(transHistoryEntities -> {
                    //(4)
                    if (transHistoryEntities.size() > 0) {
                        sqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, String.valueOf(transHistoryEntities.get(0).transid));
                        sqlZaloPayScope.write(transHistoryEntities);
                    }
                })
                ;
    }


    public Observable<List<TransHistoryEntity>> transactionHistorysLocal() {
        return sqlZaloPayScope.transactionHistorys();
    }

    public Observable<List<TransHistoryEntity>> transactionHistorysLocal(int limit) {
        return sqlZaloPayScope.transactionHistorys(limit);
    }


    public Observable<Long> balanceServer() {
        return zaloPayService.balance(user.uid, user.accesstoken)
                .doOnNext(response -> sqlZaloPayScope.writeBalance(response.zpwbalance))
                .map(balanceResponse1 -> balanceResponse1.zpwbalance)
                .doOnNext(aLong -> eventBus.post(new ChangeBalanceEvent(aLong)))
                ;
    }

    private Observable<Long> balanceLocal() {
        return sqlZaloPayScope.balance();
    }

    public Observable<Long> balance() {
        return Observable.merge(balanceLocal(), balanceServer());
    }

    public Observable<GetOrderResponse> getOrder(long appId, String zptranstoken) {
        return zaloPayService.getorder(user.uid, user.accesstoken, appId, zptranstoken);
    }

    public Observable<GetOrderResponse> createwalletorder(long appId, long amount, int transtype) {
        return zaloPayService.createwalletorder(user.uid, user.accesstoken, appId, amount, transtype);
    }

    public void reloadListTransactionSync(int count, Subscriber<List<TransHistory>> subscriber) {
        if (sqlZaloPayScope.isHaveTransactionInDb()) {
            long lasttime = sqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, 0);
            transactionHistoryServer(lasttime, count, 1, subscriber);
        } else {
            transactionHistoryServer(0, count, 1, subscriber);
        }
    }

    private void transactionHistoryServer(final long timestamp, final int count, final int odder, final Subscriber<List<TransHistory>> subscriber) {
        Timber.d("transactionHistoryServer %s ", timestamp);
        zaloPayService.transactionHistorys(user.uid, user.accesstoken, timestamp, count, odder)
                .doOnNext(response -> writeTransactionResp(response))
                .doOnNext(new Action1<TransactionHistoryResponse>() {
                    @Override
                    public void call(TransactionHistoryResponse response) {
                        if (response.data.size() >= count) {
                            transactionHistoryServer(response.data.get(0).reqdate, count, odder, subscriber);
                        }
                    }
                })
                .subscribe(new DefaultSubscriber<>());
    }




    private void writeTransactionResp(TransactionHistoryResponse response) {
        List<TransHistoryEntity> list = response.data;
        int size = list.size();


        Timber.d("writeTransactionResp %s %s", response.data, Thread.currentThread().getName());
        if (size > 0) {
            sqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, String.valueOf(list.get(0).reqdate));
            sqlZaloPayScope.write(response.data);
        }
    }
}
