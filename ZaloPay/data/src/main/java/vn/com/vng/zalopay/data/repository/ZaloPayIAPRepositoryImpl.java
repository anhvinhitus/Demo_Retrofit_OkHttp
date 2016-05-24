package vn.com.vng.zalopay.data.repository;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayIAPEntityDataMapper;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayFactory;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayIAPFactory;
import vn.com.vng.zalopay.domain.model.MerChantUserInfo;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;

/**
 * Created by AnhHieu on 5/24/16.
 */
public class ZaloPayIAPRepositoryImpl extends BaseRepository implements ZaloPayIAPRepository {

    final ZaloPayIAPFactory zaloPayIAPFactory;
    final ZaloPayFactory zaloPayFactory;

    final ZaloPayIAPEntityDataMapper mapper;

    public ZaloPayIAPRepositoryImpl(ZaloPayIAPFactory factory, ZaloPayFactory zaloPayFactory, ZaloPayIAPEntityDataMapper mapper) {
        this.zaloPayIAPFactory = factory;
        this.mapper = mapper;
        this.zaloPayFactory = zaloPayFactory;
    }

    @Override
    public Observable<MerChantUserInfo> getMerchantUserInfo(long appId) {
        return zaloPayIAPFactory.getMerchantUserInfo(appId).map(response -> mapper.transform(response));
    }

    @Override
    public Observable<Boolean> verifyMerchantAccessToken(String mUid, String token) {
        return zaloPayIAPFactory.verifyMerchantAccessToken(mUid, token).map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> transactionUpdate() {
       return zaloPayFactory.transactionUpdate();
    }
}
