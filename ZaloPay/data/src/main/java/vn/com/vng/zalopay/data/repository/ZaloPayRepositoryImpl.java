package vn.com.vng.zalopay.data.repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public Observable<Order> createwalletorder(long appId, long amount, String transtype, String appUser, String description) {
        return zaloPayFactory.createwalletorder(appId, amount, transtype, appUser, description).map(new Func1<GetOrderResponse, Order>() {
            @Override
            public Order call(GetOrderResponse getOrderResponse) {
                getOrderResponse.setAppid(appId);
                getOrderResponse.amount = String.valueOf(amount);
                return zaloPayEntityDataMapper.transform(getOrderResponse);
            }
        });
    }
}
