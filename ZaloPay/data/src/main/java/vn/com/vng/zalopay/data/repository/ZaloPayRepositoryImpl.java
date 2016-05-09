package vn.com.vng.zalopay.data.repository;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.mapper.ApplicationEntityDataMapper;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayFactory;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class ZaloPayRepositoryImpl implements ZaloPayRepository {

    private ZaloPayFactory zaloPayFactory;
    private ApplicationEntityDataMapper userEntityDataMapper;

    public ZaloPayRepositoryImpl(ZaloPayFactory zaloPayFactory, ApplicationEntityDataMapper userEntityDataMapper) {
        this.zaloPayFactory = zaloPayFactory;
        this.userEntityDataMapper = userEntityDataMapper;
    }

    @Override
    public Observable<Long> balance() {
        return zaloPayFactory.balance();
    }

    @Override
    public Observable<TransHistory> initializeTransHistory() {
        return null;
    }

    @Override
    public Observable<TransHistory> loadMoreTransHistory() {
        return null;
    }

    @Override
    public Observable<TransHistory> refreshTransHistory() {
        return null;
    }
}
