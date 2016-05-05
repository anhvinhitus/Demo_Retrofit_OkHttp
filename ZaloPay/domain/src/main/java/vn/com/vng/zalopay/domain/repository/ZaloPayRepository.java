package vn.com.vng.zalopay.domain.repository;

import rx.Observable;

/**
 * Created by AnhHieu on 5/4/16.
 * <p>
 * Gồm các tính năng của zalo pay : thanh toán, log transition, số dư, order, ...
 */
public interface ZaloPayRepository {

    Observable<Long> balance();
}
