package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Named;

import rx.Observable;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.response.GetOrderResponse;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class ZaloPayFactory {

    private Context context;

    private ZaloPayService appConfigService;

    private HashMap<String, String> params;

    private User user;

    private SqlZaloPayScope sqlZaloPayScope;

    private final int LENGTH_TRANS_HISTORY = 25;

    private final int payAppId;

    public ZaloPayFactory(Context context, ZaloPayService service,
                          User user, SqlZaloPayScope sqlZaloPayScope, int payAppId) {

        if (context == null || service == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.appConfigService = service;
        this.user = user;
        this.sqlZaloPayScope = sqlZaloPayScope;
        this.payAppId = payAppId;
    }

    public Observable<List<TransHistoryEntity>> transactionHistorysServer(long timestamp, int order) {
        return appConfigService.transactionHistorys(user.uid, user.accesstoken, timestamp, LENGTH_TRANS_HISTORY, order).map(transactionHistoryResponse -> Collections.emptyList());
    }


    public Observable<List<TransHistoryEntity>> transactionHistorysLocal() {
        return null;
    }


    private Observable<Long> balanceServer() {
        return appConfigService.balance(user.uid, user.accesstoken)
                .doOnNext(response -> sqlZaloPayScope.writeBalance(response.zpwbalance))
                .map(balanceResponse1 -> balanceResponse1.zpwbalance);
    }

    private Observable<Long> balanceLocal() {
        return sqlZaloPayScope.balance();
    }

    public Observable<Long> balance() {
        return Observable.merge(balanceLocal(), balanceServer());
    }

    public Observable<GetOrderResponse> getOrder(String zptranstoken) {
        return appConfigService.getorder(zptranstoken);
    }

}
