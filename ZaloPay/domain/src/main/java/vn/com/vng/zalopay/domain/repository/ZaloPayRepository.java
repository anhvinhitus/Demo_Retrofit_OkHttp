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

    Observable<Order> getOrder(long appId, String zptranstoken);

    Observable<Order> createwalletorder(long appId, long amount, String transtype, String appUser, String description);

    /* Gọi lần mới run app */
//    Observable<Boolean> initialize();


    /*
    *
    * Sửa dụng cho App JS
    * */

}
