package vn.com.vng.zalopay.data.util;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.InsideApp;

/**
 * Created by khattn on 3/14/17.
 *
 */

public class InsideAppUtil {
    private static List<InsideApp> mInsideAppList;

    public static boolean setInsideApps(List<InsideApp> insideAppList) {
        if (insideAppList == null) {
            return false;
        }
        mInsideAppList = insideAppList;
        return true;
    }

    public static List<InsideApp> getInsideApps() {
        return mInsideAppList;
    }

    public static InsideApp transform(AppResource appResource) {
        if(appResource == null) {
            return null;
        }

        return new InsideApp(appResource.appid, appResource.appType,
                appResource.appname, appResource.iconName, appResource.iconColor, appResource.webUrl);
    }

    public static AppResource transform(InsideApp insideApp) {
        if(insideApp == null) {
            return null;
        }

        return new AppResource(insideApp.appId, insideApp.appType,
                insideApp.appName, insideApp.iconName, insideApp.iconColor);
    }
}
