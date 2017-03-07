package vn.com.vng.zalopay.data.repository;

import android.text.TextUtils;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by AnhHieu on 5/4/16.
 * *
 */
public class ZaloPayRepositoryImpl implements ZaloPayRepository {

    private ZaloPayEntityDataMapper zaloPayEntityDataMapper;
    private ZaloPayService zaloPayService;
    private final User user;


    public ZaloPayRepositoryImpl(ZaloPayEntityDataMapper zaloPayEntityDataMapper, ZaloPayService zaloPayService, User user) {
        this.zaloPayEntityDataMapper = zaloPayEntityDataMapper;
        this.zaloPayService = zaloPayService;
        this.user = user;
        Timber.d("accessToken[%s]", this.user.accesstoken);
    }


    @Override
    public Observable<Order> getOrder(long appId, String zptranstoken) {
        return zaloPayService.getorder(user.zaloPayId, user.accesstoken, appId, zptranstoken)
                .map(getOrderResponse -> {
                    getOrderResponse.setAppid(appId);
                    getOrderResponse.setZptranstoken(zptranstoken);
                    return zaloPayEntityDataMapper.transform(getOrderResponse);
                });
    }

    @Override
    public Observable<Order> createwalletorder(long appId, long amount, String transtype, String appUser, String description) {
        return createwalletorder(appId, amount, transtype, appUser, description, null);
    }

    @Override
    public Observable<Order> createwalletorder(long appId, long amount, String transtype, String appUser, String description, String embeddata) {
        if (appId <= 0 || amount <= 0 || TextUtils.isEmpty(transtype)) {
            Timber.e(new Exception(
                    String.format("Create wallet order with data is invalid, appId[%s] amount[%s] transType[%s]",
                            appId,
                            amount,
                            transtype)));
        }
        return zaloPayService.createwalletorder(user.zaloPayId, user.accesstoken, appId, amount, transtype, appUser, description, embeddata)
                .map(orderResponse -> {
                    orderResponse.setAppid(appId);
                    if (orderResponse.appid <= 0
                            || TextUtils.isEmpty(orderResponse.apptransid)
                            || orderResponse.amount <= 0
                            || orderResponse.apptime <= 0
                            || TextUtils.isEmpty(orderResponse.mac)) {
                        Timber.e(new Exception(
                                String.format("GetOrderResponse is invalid, appId[%s] transId[%s] amount[%s] appTime[%s]  mac[%s]",
                                        orderResponse.appid,
                                        orderResponse.apptransid,
                                        orderResponse.amount,
                                        orderResponse.apptime,
                                        orderResponse.mac)));
                    }
                    return zaloPayEntityDataMapper.transform(orderResponse);
                });
    }
}
