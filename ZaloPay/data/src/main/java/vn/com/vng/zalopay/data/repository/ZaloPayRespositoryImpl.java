package vn.com.vng.zalopay.data.repository;

import vn.com.vng.zalopay.data.api.entity.mapper.ApplicationEntityDataMapper;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayFactory;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class ZaloPayRespositoryImpl implements ZaloPayRepository {

    private ZaloPayFactory zaloPayFactory;
    private ApplicationEntityDataMapper userEntityDataMapper;

    public ZaloPayRespositoryImpl(ZaloPayFactory zaloPayFactory, ApplicationEntityDataMapper userEntityDataMapper) {
        this.zaloPayFactory = zaloPayFactory;
        this.userEntityDataMapper = userEntityDataMapper;
    }
}
