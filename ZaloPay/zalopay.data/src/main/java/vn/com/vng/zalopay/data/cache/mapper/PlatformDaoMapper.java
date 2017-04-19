package vn.com.vng.zalopay.data.cache.mapper;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.cache.model.AppResourceGD;

/**
 * Created by AnhHieu on 5/18/16.
 * transform data
 */

@Singleton
public class PlatformDaoMapper {

    @Inject
    public PlatformDaoMapper() {
    }

    public AppResourceGD transform(AppResourceEntity appResourceEntity) {
        AppResourceGD appResourceGD = null;
        if (appResourceEntity != null) {
            appResourceGD = new AppResourceGD();
            appResourceGD.appid = (appResourceEntity.appid);
            appResourceGD.appname = (appResourceEntity.appname);
            appResourceGD.checksum = (appResourceEntity.checksum);
            appResourceGD.imageurl = (appResourceEntity.imageurl);
            appResourceGD.jsurl = (appResourceEntity.jsurl);
            appResourceGD.needdownloadrs = (appResourceEntity.needdownloadrs);
            appResourceGD.status = (appResourceEntity.status);
            appResourceGD.apptype = (appResourceEntity.apptype);
            appResourceGD.weburl = (appResourceEntity.weburl);
            appResourceGD.iconname = (appResourceEntity.iconName);
            appResourceGD.iconcolor = (appResourceEntity.iconColor);
            appResourceGD.sortOrder = (appResourceEntity.sortOrder);
            appResourceGD.downloadState = (appResourceEntity.downloadState);
            appResourceGD.retryNumber = (appResourceEntity.retryNumber);
            appResourceGD.downloadTime = (appResourceEntity.downloadTime);
        }
        return appResourceGD;
    }

    public AppResourceEntity transform(AppResourceGD appResourceGD) {
        AppResourceEntity appResourceEntity = null;
        if (appResourceGD != null) {
            appResourceEntity = new AppResourceEntity();
            appResourceEntity.appid = appResourceGD.appid;
            appResourceEntity.appname = appResourceGD.appname;
            appResourceEntity.checksum = appResourceGD.checksum;
            appResourceEntity.imageurl = appResourceGD.imageurl;
            appResourceEntity.needdownloadrs = appResourceGD.needdownloadrs == null ? 0 : 1;
            appResourceEntity.status = appResourceGD.status == null ? 0 : 1;
            appResourceEntity.jsurl = appResourceGD.jsurl == null
                    ? "" : appResourceGD.jsurl;
            appResourceEntity.apptype = appResourceGD.apptype == null
                    ? 0 : appResourceGD.apptype;
            appResourceEntity.weburl = appResourceGD.weburl == null
                    ? "" : appResourceGD.weburl;
            appResourceEntity.iconName = appResourceGD.iconname == null
                    ? "" : appResourceGD.iconname;
            appResourceEntity.iconColor = appResourceGD.iconcolor == null
                    ? "" : appResourceGD.iconcolor;
            appResourceEntity.sortOrder = appResourceGD.sortOrder == null
                    ? 0 : appResourceGD.sortOrder;
            appResourceEntity.downloadState = appResourceGD.downloadState == null
                    ? 0 : appResourceGD.downloadState;
            appResourceEntity.retryNumber = appResourceGD.retryNumber == null
                    ? 0 : appResourceGD.retryNumber;
            appResourceEntity.downloadTime = appResourceGD.downloadTime == null
                    ? 0 : appResourceGD.downloadTime;

        }
        return appResourceEntity;
    }
}
