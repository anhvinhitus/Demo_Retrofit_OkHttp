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
        return PhoneUtil.setPhoneFormat(config.mGeneral.mPhoneFormat);
    }

    private static boolean loadConfigInsideApp(@NonNull Config config) {
        return InsideAppUtil.setInsideApps(config.mSearch.mInsideAppList);
    }

    /**
     * Chế độ bật/tắt việc merge tên hiển thị từ danh bạ điện thoại cho danh sách bạn Zalo
     * Mặc định là TRUE.
     */

    private static boolean isSyncContact() {
        return !(mConfig != null && mConfig.mZpc != null) || mConfig.mZpc.enableMergeContactName != 0;
    }

    /**
     * Chế độ bật/tắt việc hiển thị danh sách yêu thích trong ZPC
     * Mặc định là TRUE.
     */

    private static boolean isDisplayFavorite() {
        return !(mConfig != null && mConfig.mZpc != null) || mConfig.mZpc.enableDisplayFavorite != 0;
    }

    /**
     * Config sử dụng payment connector hoặc https
     * Default sử dụng https
     */
    public static boolean isHttpsRoute() {
        boolean isHttpsRoute = (mConfig == null || mConfig.mApi == null || !"connector".equals(mConfig.mApi.apiRoute));
        Timber.d("Network routing through: [%s]", isHttpsRoute ? "https" : "connector");
        return isHttpsRoute;
    }


    /**
     * apiName trong danh sách api_names sẽ được gọi thông qua Payment Connector
     */
    public static boolean containsApi(String apiName) {
        return !(mConfig == null || mConfig.mApi == null || mConfig.mApi.apiNames == null) && mConfig.mApi.apiNames.contains(apiName);
    }

    public static List<Long> getDenominationWithdraw() {
        if (mConfig == null || mConfig.mWithdraw == null || Lists.isEmptyOrNull(mConfig.mWithdraw.denominationWithdraw)) {
            return Arrays.asList(100000L, 200000L, 500000L, 1000000L, 2000000L, 5000000L);
        } else {
            return mConfig.mWithdraw.denominationWithdraw;
        }
    }

    public static long getMinMoneyWithdraw() {
        if (mConfig == null || mConfig.mWithdraw == null || mConfig.mWithdraw.minWithdrawMoney <= 0) {
            return 20000L;
        } else {
            return mConfig.mWithdraw.minWithdrawMoney;
        }
    }

    public static long getMaxMoneyWithdraw() {
        if (mConfig == null || mConfig.mWithdraw == null || mConfig.mWithdraw.minWithdrawMoney <= 0) {
            return 20000000L;
        } else {
            return mConfig.mWithdraw.maxWithdrawMoney;
        }
    }

    public static long getMultipleMoneyWithdraw() {
        if (mConfig == null || mConfig.mWithdraw == null || mConfig.mWithdraw.multipleWithdrawMoney <= 0) {
            return 10000L;
        } else {
            return mConfig.mWithdraw.multipleWithdrawMoney;
        }
    }

    public static String getFeedbackUrl() {
        if (mConfig == null || mConfig.mTabMe == null || TextUtils.isEmpty(mConfig.mTabMe.mFeedbackUrl)) {
            return "https://goo.gl/forms/FCO642guFSbpYwlt2";
        } else {
            return mConfig.mTabMe.mFeedbackUrl;
        }
    }

    public static List<String> getAllowUrls() {
        if (mConfig == null || mConfig.mWebApp == null || Lists.isEmptyOrNull(mConfig.mWebApp.allowUrls)) {
            return Arrays.asList("^((.+)\\.)?zalopay\\.vn",
                    "^((.+)\\.)?zalopay\\.com\\.vn",
                    "^((.+)\\.)?zalopay\\.zing\\.vn",
                    "^((.+)\\.)?zpsandbox\\.zing\\.vn");
        } else {
            return mConfig.mWebApp.allowUrls;
        }
    }

    public static List<Integer> getListVibrateNotificationType() {
        if (mConfig == null || mConfig.mNotification == null || Lists.isEmptyOrNull(mConfig.mNotification.mVibrateNotificationType)) {
            return Arrays.asList(6, 7, 9, 105, 106, 111);
        } else {
            return mConfig.mNotification.mVibrateNotificationType;
        }
    }

    public static List<InternalApp> listInternalApp() {
        if (mConfig == null || mConfig.mTabHome == null || mConfig.mTabHome.mInternalApps == null) {
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
            return mConfig.mTabHome.mInternalApps;
        }
    }

    public static int getTopRateApp() {
        if (mConfig == null || mConfig.mSearch == null || mConfig.mSearch.mSearchConfig <= 0) {
            return 3;
        } else {
            return mConfig.mSearch.mSearchConfig;
        }
    }

    public static boolean isEnableRegisterZalopayID() {
        return (mConfig != null && mConfig.mTabMe != null && mConfig.mTabMe.mEnableRegisterZalopayID == 1);
    }

    public static int maxCCLinkNum() {
        if (mConfig == null || mConfig.mGeneral == null) {
            return 3;
        }
        return mConfig.mGeneral.max_cc_links;
    }

    public static boolean allowPaymentVoucher() {
        if (mConfig == null || mConfig.mPromotion == null || mConfig.mPromotion.mVoucher == null) {
            return false;
        }
        return mConfig.mPromotion.mVoucher.mAllowPaymentVoucher > 0;
    }
}

