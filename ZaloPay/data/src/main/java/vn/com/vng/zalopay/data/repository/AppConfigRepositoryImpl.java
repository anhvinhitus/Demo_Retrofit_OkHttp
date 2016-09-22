/*
package vn.com.vng.zalopay.data.repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.entity.mapper.ApplicationEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.api.response.PlatformInfoResponse;
import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;

*/
/**
 * Created by AnhHieu on 4/28/16.
 *//*

public class AppConfigRepositoryImpl implements AppConfigRepository {

    private AppConfigFactory appConfigFactory;
    private AppConfigEntityDataMapper mapper;

    public AppConfigRepositoryImpl(AppConfigFactory factory, AppConfigEntityDataMapper mapper) {
        this.appConfigFactory = factory;
        this.mapper = mapper;
    }

    @Override
    public Observable<List<BankCard>> listCardCache() {
        return appConfigFactory.listCardCache().map(cardEntities -> mapper.transform(cardEntities));
    }
}
*/
