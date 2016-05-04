package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.AppInfoEntity;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.AppInfo;

import static java.util.Collections.emptyList;

/**
 * Created by AnhHieu on 5/3/16.
 */

@Singleton
public class ApplicationEntityDataMapper {

    @Inject
    public ApplicationEntityDataMapper() {
    }

    public AppInfo transform(AppInfoEntity val) {
        AppInfo appInfo = null;
        if (val != null) {
            appInfo = new AppInfo();
            appInfo.app_id = val.app_id;
            appInfo.app_checksum = val.app_checksum;
            appInfo.app_icon_url = val.app_icon_url;
            appInfo.app_local_url = val.app_local_url;
            appInfo.app_name = val.app_name;
            appInfo.js_url = val.js_url;
            appInfo.base_url = val.base_url;
            appInfo.resource_url = val.resource_url;
            appInfo.status = val.status;
        }

        return appInfo;
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
