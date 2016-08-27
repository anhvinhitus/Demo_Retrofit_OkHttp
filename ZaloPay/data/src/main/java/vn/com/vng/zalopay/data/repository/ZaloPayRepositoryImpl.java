package vn.com.vng.zalopay.data.repository;

import rx.Observable;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class ZaloPayRepositoryImpl implements ZaloPayRepository {

    private ZaloPayEntityDataMapper zaloPayEntityDataMapper;
    private ZaloPayService zaloPayService;
    private User user;


    public ZaloPayRepositoryImpl(ZaloPayEntityDataMapper zaloPayEntityDataMapper, ZaloPayService zaloPayService, User user) {
        this.zaloPayEntityDataMapper = zaloPayEntityDataMapper;
        this.zaloPayService = zaloPayService;
        this.user = user;
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
        return zaloPayService.createwalletorder(user.zaloPayId, user.accesstoken, appId, amount, transtype, appUser, description)
                .map(getOrderResponse -> {
                    getOrderResponse.setAppid(appId);
                    getOrderResponse.amount = amount;
                    return zaloPayEntityDataMapper.transform(getOrderResponse);
                });
    }
}
