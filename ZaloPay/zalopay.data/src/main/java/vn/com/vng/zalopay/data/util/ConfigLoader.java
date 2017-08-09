package vn.com.vng.zalopay.data.util;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.data.zpc.ZPCConfig;
import vn.com.vng.zalopay.domain.model.Config;
import vn.com.vng.zalopay.domain.model.InternalApp;

/**
 * Created by longlv on 2/14/17.
 * Using to load config from assert or resource app 1.
 */

public class ConfigLoader {
    private final static String CONFIG_FILE_PATH = "config/zalopay_config.json";
    private static Config mConfig;
    private static String FEEDBACK_URL = "https://goo.gl/forms/FCO642guFSbpYwlt2";

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
        } catch (Exception e) {
            Timber.d(e, "Read config from assets throw exception.");
            return false;
        }
    }

    public static boolean loadConfigFromResource(int zalopayAppId) {
        try {

            String filePath = getFileConfigPath(zalopayAppId);

            String jsonConfig = FileUtil.readFileToString(filePath);
            return loadConfig(jsonConfig);
        } catch (Exception e) {
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
                ZPCConfig.sEnableSyncContact = isSyncContact();
                ZPCConfig.sEnableDisplayFavorite = isDisplayFavorite();
                return true;
            }
        } catch (Exception e) {
            Timber.d(e, "Fail to load config with config: %s", jsonConfig);
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
     * Chế độ bật/tắt việc hiển thị danh sách yêu thích trong ZPC
     * Mặc định là TRUE.
     */

    private static boolean isDisplayFavorite() {
        return !(mConfig != null && mConfig.friendConfig != null) || mConfig.friendConfig.enableDisplayFavorite != 0;
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

    public static long getMinMoneyWithdraw() {
        if (mConfig == null || mConfig.minWithdrawMoney <= 0) {
            return 20000L;
        } else {
            return mConfig.minWithdrawMoney;
        }
    }

    public static long getMaxMoneyWithdraw() {
        if (mConfig == null || mConfig.minWithdrawMoney <= 0) {
            return 20000000L;
        } else {
            return mConfig.maxWithdrawMoney;
        }
    }

    public static long getMultipleMoneyWithdraw() {
        if (mConfig == null || mConfig.multipleWithdrawMoney <= 0) {
            return 10000L;
        } else {
            return mConfig.multipleWithdrawMoney;
        }
    }

    public static String getFeedbackUrl() {
        if (mConfig == null || TextUtils.isEmpty(mConfig.mFeedbackUrl)) {
            return FEEDBACK_URL;
        } else {
            return mConfig.mFeedbackUrl;
        }
    }

    public static List<String> getAllowUrls() {
        if (mConfig == null || Lists.isEmptyOrNull(mConfig.allowUrls)) {
            return Arrays.asList("^((.+)\\.)?zalopay\\.vn",
                    "^((.+)\\.)?zalopay\\.com\\.vn",
                    "^((.+)\\.)?zalopay\\.zing\\.vn",
                    "^((.+)\\.)?zpsandbox\\.zing\\.vn");
        } else {
            return mConfig.allowUrls;
        }
    }

    public static List<Integer> getListVibrateNotificationType() {
        if (mConfig == null || Lists.isEmptyOrNull(mConfig.mVibrateNotificationType)) {
            return Arrays.asList(6, 7, 9, 105, 106, 111);
        } else {
            return mConfig.mVibrateNotificationType;
        }
    }

    public static List<InternalApp> listInternalApp() {
        if (mConfig == null || mConfig.mInternalApps == null) {
            String json = "[{\"appId\": -1, \"order\": 2, \"display_name\": \"Chuyển Tiền\", \"icon_name\": \"app_1_transfers\", \"icon_color\": \"#4387f6\"}, " +
                    "{\"appId\": -2, \"order\": 3, \"display_name\": \"Nhận Tiền\", \"icon_name\": \"app_1_receivemoney\", \"icon_color\": \"#4286F6\"}, " +
                    "{\"appId\": -3, \"order\": 6, \"display_name\": \"Nạp Tiền\", \"icon_name\": \"app_recharge\", \"icon_color\": \"#129d5a\"}]";
            List<InternalApp> listInternalApp = new ArrayList<>();
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = (JsonArray) jsonParser.parse(json);
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                long appID = jsonObject.get("appId").getAsLong();
                int pos = jsonObject.get("order").getAsInt();
                String displayName = jsonObject.get("display_name").getAsString();
                String iconName = jsonObject.get("icon_name").getAsString();
                String iconColor = jsonObject.get("icon_color").getAsString();
                listInternalApp.add(new InternalApp(appID, pos, displayName, iconName, iconColor));
            }
            return listInternalApp;
        } else {
            return mConfig.mInternalApps;
        }
    }
    public static boolean isEnableRegisterZalopayID() {
        if (mConfig == null || mConfig.mEnableRegisterZalopayID != 1) {
            return false;
        } else {
            return true;
        }
    }
}

