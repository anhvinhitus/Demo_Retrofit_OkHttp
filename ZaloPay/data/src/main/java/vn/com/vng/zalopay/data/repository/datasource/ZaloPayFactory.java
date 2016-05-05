package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.BalanceEntity;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
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

    public ZaloPayFactory(Context context, ZaloPayService service,
                          User user, SqlZaloPayScope sqlZaloPayScope) {

        if (context == null || service == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.appConfigService = service;
        this.user = user;
        this.sqlZaloPayScope = sqlZaloPayScope;
    }

    public Observable<List<TransHistoryEntity>> transactionHistorysServer(long timestamp, int count, boolean order) {
        //  return appConfigService.transactionHistorys(user.uid, user.accesstoken, timestamp, count, order).doOnNext(transactionHistoryResponse ->);

        return null;
    }


    public Observable<List<TransHistoryEntity>> transactionHistorysLocal() {

        return null;
    }




    public Observable<Long> balanceServer() {
        return null;
    }

    public Observable<Long> balanceLocal() {
        return null;
    }


}
