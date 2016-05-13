package vn.com.vng.zalopay.data.cache.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.AppInfoEntity;
import vn.com.vng.zalopay.data.cache.model.AppInfo;
import vn.com.vng.zalopay.data.util.Lists;

import static java.util.Collections.emptyList;

/**
 * Created by AnhHieu on 5/3/16.
 */

@Singleton
public class AppInfoDaoMapper {

    @Inject
    public AppInfoDaoMapper() {
    }

    public AppInfo transform(AppInfoEntity appInfoEntity) {
        AppInfo info = null;
        if (appInfoEntity != null) {
            info = new AppInfo(appInfoEntity.app_id,
                    appInfoEntity.app_name,
                    appInfoEntity.app_icon_url,
                    appInfoEntity.js_url,
                    appInfoEntity.resource_url,
                    appInfoEntity.base_url,
                    appInfoEntity.app_checksum,
                    appInfoEntity.status,
                    appInfoEntity.app_local_url);

        }
        return info;
    }

    public List<AppInfo> transform(Collection<AppInfoEntity> appInfoEntities) {
        if (Lists.isEmptyOrNull(appInfoEntities))
            return emptyList();

        List<AppInfo> appInfos = new ArrayList<>(appInfoEntities.size());
        for (AppInfoEntity appInfoEntity : appInfoEntities) {
            AppInfo appInfo = transform(appInfoEntity);
            if (appInfo != null) {
                appInfos.add(appInfo);
            }
        }
        return appInfos;
    }

}
