package vn.com.vng.zalopay.data.cache.mapper;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.AppInfoEntity;
import vn.com.vng.zalopay.data.cache.model.AppInfo;

/**
 * Created by AnhHieu on 5/3/16.
 */

@Singleton
public class AppInfoMapper {

    @Inject
    public AppInfoMapper() {
    }

    public AppInfo transform(AppInfoEntity appInfoEntity) {
        AppInfo info = null;
        if (appInfoEntity != null) {

        }
        return info;
    }

}
