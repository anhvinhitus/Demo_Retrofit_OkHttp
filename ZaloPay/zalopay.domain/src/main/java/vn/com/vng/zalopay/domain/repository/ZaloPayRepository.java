package vn.com.vng.zalopay.domain.repository;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.Order;

/**
 * Created by AnhHieu on 5/4/16.
 * <p>
 * Gồm các tính năng của zalo pay : thanh toán, log transition, số dư, order, ...
 */
public interface ZaloPayRepository {

    Observable<Order> getOrder(long appId, String zptranstoken);

    Observable<Order> createwalletorder(long appId, long amount, String transtype, String appUser, String description);

    Observable<Order> createwalletorder(long appId, long amount, String transtype, String appUser, String description, String embeddata);

}
