package vn.com.vng.zalopay.data.util;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by longlv on 10/5/16.
 * *
 */

public class AppResourceUtil {

    public static String toStringListAppId(List<AppResource> listAppResource) {
        if (Lists.isEmptyOrNull(listAppResource)) return "";
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < listAppResource.size(); i++) {

            AppResource appResource = listAppResource.get(i);
            if (appResource == null) {
                continue;
            }
            str.append(str.length() == 0 ? String.valueOf(appResource.appid) :
                    "," + String.valueOf(appResource.appid));
        }
        return str.toString();
    }

    public static String toStringListAppEntitiId(List<AppResourceEntity> listAppResource) {
        if (Lists.isEmptyOrNull(listAppResource)) return "";
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < listAppResource.size(); i++) {

            AppResourceEntity appResource = listAppResource.get(i);
            if (appResource == null) {
                continue;
            }

            str.append(str.length() == 0 ? String.valueOf(appResource.appid) :
                    "," + String.valueOf(appResource.appid));
        }
        return str.toString();
    }
}
