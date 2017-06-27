package vn.com.vng.zalopay.data.util;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.data.zfriend.FriendConfig;
import vn.com.vng.zalopay.domain.model.Config;

/**
 * Created by longlv on 2/14/17.
 * Using to load config from assert or resource app 1.
 */

public class ConfigUtil {
    private final static String CONFIG_FILE_PATH = "config/zalopay_config.json";
    private static Config mConfig;

    static {
        mConfig = new Config();
    }

    private static String getFileConfigPath(long appId) {
        return String.format(Locale.getDefault(), "%s/%s",
                ResourceHelper.getPath(appId),
                CONFIG_FILE_PATH);
    }

    public static void initConfig(AssetManager assetManager, int zalopayAppId) {
        if (loadConfigFromResource(zalopayAppId)) {
            Timber.d("Load config from resource app 1 successfully.");
            return;
        }

        if (loadConfigFromAssets(assetManager)) {
            Timber.d("Load config from assets successfully.");
        }
    }

    private static boolean loadConfigFromAssets(AssetManager assetManager) {
        try {
            String jsonConfig = FileUtil.readAssetToString(assetManager, CONFIG_FILE_PATH);
            return loadConfig(jsonConfig);
        } catch (IOException e) {
            Timber.e(e, "Read config from assets throw exception.");
            return false;
        }
    }

    public static boolean loadConfigFromResource(int zalopayAppId) {
        try {

            String filePath = getFileConfigPath(zalopayAppId);

            String jsonConfig = FileUtil.readFileToString(filePath);
            return loadConfig(jsonConfig);
        } catch (IOException e) {
            Timber.d(e, "Read config from resource app 1 throw exception.");
            return false;
        }
    }

    private static boolean loadConfig(String jsonConfig) {
        if (TextUtils.isEmpty(jsonConfig)) {
            return false;
        }

        try {
            Gson gson = new Gson();
            Config config = gson.fromJson(jsonConfig, Config.class);
            if (config != null) {
                mConfig = config;
                loadConfigPhoneFormat(config);
                loadConfigInsideApp(config);
                loadConfigSearch(config);
                FriendConfig.sEnableSyncContact = isSyncContact();
                return true;
            }
        } catch (JsonSyntaxException e) {
            Timber.w(e, "Fail to load config with config: %s", jsonConfig);
            return false;
        }

        return false;
    }

    private static boolean loadConfigPhoneFormat(@NonNull Config config) {
        return PhoneUtil.setPhoneFormat(config.mPhoneFormat);
    }

    private static boolean loadConfigInsideApp(@NonNull Config config) {
        return InsideAppUtil.setInsideApps(config.mInsideAppList);
    }

    private static void loadConfigSearch(@NonNull Config config) {
        SearchUtil.setTopRateApp(config.mSearchConfig);
    }

    /**
     * Chế độ bật/tắt việc merge tên hiển thị từ danh bạ điện thoại cho danh sách bạn Zalo
     * Mặc định là TRUE.
     */

    private static boolean isSyncContact() {
        return !(mConfig != null && mConfig.friendConfig != null) || mConfig.friendConfig.enableMergeContactName != 0;
    }

    /**
     * Config sử dụng payment connector hoặc https
     * Default sử dụng https
     */
    public static boolean isHttpsRoute() {
        boolean isHttpsRoute = mConfig == null || !"connector".equals(mConfig.apiRoute);
        Timber.d("Network routing through: [%s]", mConfig == null ? "https" : mConfig.apiRoute);
        return isHttpsRoute;
    }


    /**
     * apiName trong danh sách api_names sẽ được gọi thông qua Payment Connector
     */
    public static boolean containsApi(String apiName) {
        return !(mConfig == null || mConfig.apiNames == null) && mConfig.apiNames.contains(apiName);
    }

    public static List<Long> getDenominationWithdraw() {
        if (mConfig == null || Lists.isEmptyOrNull(mConfig.denominationWithdraw)) {
           return Arrays.asList(100000L, 200000L, 500000L, 1000000L, 2000000L, 5000000L);
        } else {
            return mConfig.denominationWithdraw;
        }
    }
}

