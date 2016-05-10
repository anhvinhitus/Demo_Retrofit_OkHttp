package vn.com.vng.zalopay.data.repository;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
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
    public Observable<Long> balance() {
        return zaloPayFactory.balance();
    }

    @Override
    public Observable<List<TransHistory>> initializeTransHistory() {
        return zaloPayFactory.transactionHistorysServer(0, 1)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
    }

    @Override
    public Observable<List<TransHistory>> loadMoreTransHistory() {
        return null;
    }

    @Override
    public Observable<List<TransHistory>> refreshTransHistory() {
        return null;
    }

    @Override
    public Observable<Order> getOrder(String zptranstoken) {
        return zaloPayFactory.getOrder(zptranstoken).map(getOrderResponse -> userEntityDataMapper.transform(getOrderResponse));
    }
}
