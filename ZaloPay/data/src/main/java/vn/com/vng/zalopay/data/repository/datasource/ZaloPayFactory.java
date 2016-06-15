package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;
import android.util.LruCache;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.data.api.response.GetOrderResponse;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.data.cache.BalanceStore;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.TransactionStore;
import vn.com.vng.zalopay.data.cache.helper.ObservableHelper;
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

    private LruCache<Long, GetMerchantUserInfoResponse> mCacheMerchantUser = new LruCache<>(10);

    public ZaloPayFactory(Context context,
                          ZaloPayService service,
                          User user,
                          SqlZaloPayScope sqlZaloPayScope,
                          TransactionStore.LocalStorage transactionLocalStorage,
                          TransactionStore.RequestService transactionRequestService,
                          int payAppId,
                          EventBus eventBus) {

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


    public Observable<GetOrderResponse> getOrder(long appId, String zptranstoken) {
        return zaloPayService.getorder(user.uid, user.accesstoken, appId, zptranstoken);
    }

    public Observable<GetOrderResponse> createwalletorder(long appId, long amount, String transtype, String appUser, String description) {
        return zaloPayService.createwalletorder(user.uid, user.accesstoken, appId, amount, transtype, appUser, description);
    }
}
