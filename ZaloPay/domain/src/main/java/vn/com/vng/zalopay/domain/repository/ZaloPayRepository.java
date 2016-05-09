package vn.com.vng.zalopay.domain.repository;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.TransHistory;

/**
 * Created by AnhHieu on 5/4/16.
 * <p>
 * Gồm các tính năng của zalo pay : thanh toán, log transition, số dư, order, ...
 */
public interface ZaloPayRepository {

    Observable<Long> balance();

    Observable<List<TransHistory>> initializeTransHistory();

    Observable<List<TransHistory>> loadMoreTransHistory();

    Observable<List<TransHistory>> refreshTransHistory();
}
