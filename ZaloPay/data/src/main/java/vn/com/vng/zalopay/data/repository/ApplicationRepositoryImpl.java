/*
package vn.com.vng.zalopay.data.repository;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.mapper.ApplicationEntityDataMapper;
import vn.com.vng.zalopay.data.repository.datasource.AppListFactory;
import vn.com.vng.zalopay.domain.model.AppInfo;
import vn.com.vng.zalopay.domain.repository.ApplicationRepository;

*/
/**
 * Created by AnhHieu on 5/3/16.
 *//*

public class ApplicationRepositoryImpl implements ApplicationRepository {

    private AppListFactory factory;
    private ApplicationEntityDataMapper mapper;

    public ApplicationRepositoryImpl(AppListFactory appListFactory, ApplicationEntityDataMapper mapper) {
        this.factory = appListFactory;
        this.mapper = mapper;
    }


    @Override
    public Observable<List<AppInfo>> getApplicationInfos() {
        return factory.listAppInfoEntity().map(appInfoEntities -> mapper.transform(appInfoEntities));
    }
}
*/
