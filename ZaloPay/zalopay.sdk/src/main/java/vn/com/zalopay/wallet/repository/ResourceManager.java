package vn.com.zalopay.wallet.repository;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import timber.log.Timber;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.entity.staticconfig.CardRule;
import vn.com.zalopay.wallet.business.entity.staticconfig.DConfigFromServer;
import vn.com.zalopay.wallet.business.entity.staticconfig.DKeyBoardConfig;
import vn.com.zalopay.wallet.business.entity.staticconfig.DPage;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.exception.SdkResourceException;
import vn.com.zalopay.wallet.ui.channel.RenderFragment;
import vn.com.zalopay.wallet.ui.channel.ResourceRender;

import static vn.com.zalopay.utility.StorageUtil.loadAbsolutePath;

public class ResourceManager extends SingletonBase {
    public static final String CONFIG_FILE = "config.json";
    protected static final String TAG = ResourceManager.class.getSimpleName();
    private static final String PREFIX_JS = "/js/";
    private static final String PREFIX_IMG = "/img/";
    private static final String PREFIX_FONT = "/fonts/";
    private static final String HIDE_IMG_NAME = "0.png";
    private static ResourceManager mCommonResourceManager = null;
    private static Map<String, ResourceManager> mResourceManagerMap = null;
    private static DConfigFromServer mConfigFromServer = null;
    private HashMap<String, String> mStringMap = null;
    private DPage mPageConfig = null;

    public ResourceManager() {
        super();
    }

    public static synchronized ResourceManager getInstance(String pPageName) {
        if (pPageName == null) {
            if (mCommonResourceManager == null) {
                mCommonResourceManager = new ResourceManager();
            }

            return mCommonResourceManager;
        }

        if (mResourceManagerMap == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mResourceManagerMap = new ArrayMap<>();
            } else {
                mResourceManagerMap = new HashMap<>();
            }
        }

        ResourceManager resourceManager = mResourceManagerMap.get(pPageName);
        if (resourceManager != null) {
            return resourceManager;
        }

        resourceManager = new ResourceManager();
        mResourceManagerMap.put(pPageName, resourceManager);
        return resourceManager;
    }

    private static String getResourceFolderPath() {
        return SDKApplication
                .getApplicationComponent()
                .platformInfoInteractor()
                .getResourcePath();
    }

    private static String loadFile(String pathPrefix, String fileName) throws Exception {
        StringBuilder path = new StringBuilder();
        path.append(getResourceFolderPath())
                .append(File.separator);
        if (!TextUtils.isEmpty(pathPrefix)) {
            path.append(pathPrefix);
        }
        path.append(fileName);
        return loadAbsolutePath(path.toString());
    }

    /***
     * load config from config.json
     * @return
     * @throws Exception
     */
    public static String loadJsonConfig() throws Exception {
        StringBuilder path = new StringBuilder();
        path.append(getResourceFolderPath())
                .append(File.separator)
                .append(ResourceManager.CONFIG_FILE);
        return loadAbsolutePath(path.toString());
    }

    public static boolean isInit() {
        return mConfigFromServer != null;
    }

    public static synchronized void deleteResFolder() {
        try {
            String resPath = getResourceFolderPath();
            if (!TextUtils.isEmpty(resPath)) {
                File file = new File(resPath);
                if (file.exists()) {
                    StorageUtil.deleteRecursive(file);
                    Timber.d("delete order resource %s", resPath);
                }
            }
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    public static synchronized Observable<Boolean> initResource() {
        return Observable.defer(() -> {
            try {
                Timber.d("initializing SDK resource");
                String json = loadJsonConfig();
                if (TextUtils.isEmpty(json)) {
                    throw new Exception("Empty resource file config");
                } else {
                    mConfigFromServer = (new DConfigFromServer()).fromJsonString(json);
                    ResourceManager commonResourceManager = getInstance(null);
                    if (mConfigFromServer.stringMap != null) {
                        commonResourceManager.setString(mConfigFromServer.stringMap);
                    }
                    if (mConfigFromServer.pageList != null) {
                        for (DPage page : mConfigFromServer.pageList) {
                            getInstance(page.pageName).mPageConfig = page;
                        }
                    }
                }
            } catch (Exception e) {
                Timber.w(e.getMessage());
                deleteResFolder();
                return Observable.error(new SdkResourceException(GlobalData.getAppContext().getString(R.string.sdk_error_load_resource)));
            }
            return Observable.just(true);
        });
    }

    public static String getJavascriptContent(String pJsName) {
        try {
            String content = loadFile(PREFIX_JS, pJsName);
            return !TextUtils.isEmpty(content) ? content : "";
        } catch (Exception e) {
            Timber.w("getJavascriptContent on error %s", e);
        }
        return null;
    }

   /* public static Single<String> getJavascriptContent(String pJsName) {
        return Single.create(singleSubscriber -> {
            try {
                singleSubscriber.onSuccess(loadJsonConfig(PREFIX_JS, pJsName));
            } catch (Exception e) {
                singleSubscriber.onError(e);
            }
        });
    }*/

    public static Bitmap getImage(String imageName) {
        if (imageName.equals(HIDE_IMG_NAME)) {
            return null;
        }
        String imgLocalPath;
        Bitmap bitmap = null;
        try {
            imgLocalPath = String.format("%s%s%s%s%s", getResourceFolderPath(), File.separator, PREFIX_IMG, File.separator, imageName);
            bitmap = BitmapFactory.decodeFile(imgLocalPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String getAbsoluteImagePath(String pImageName) {
        try {
            return String.format("file://%s%s%s%s%s", getResourceFolderPath(), File.separator, PREFIX_IMG, File.separator, pImageName);
        } catch (Exception e) {
            Log.e("getAbsolutePath", e);
        }
        return null;
    }

    /***
     * load image into SimpleDraweeView
     * use Fresco
     * @param pView
     * @param pImageName
     */
    public static void loadImageIntoView(View pView, String pImageName) {
        try {
            String pFilePath = getAbsoluteImagePath(pImageName);
            if (!TextUtils.isEmpty(pFilePath) && pView != null) {
                ((SimpleDraweeView) pView).setImageURI(pFilePath);
            }
        } catch (Exception e) {
            Log.e("loadImageIntoView", e);
        }
    }

    public static String getPathFont() {
        try {
            return getResourceFolderPath() + PREFIX_FONT;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public String getString(String pKey) {
        if (this.mStringMap != null) {
            return this.mStringMap.get(pKey);
        }
        return null;
    }

    private void setString(HashMap<String, String> pMap) {
        if (pMap != null) {
            this.mStringMap = pMap;
        }
    }

    public String getPattern(String pViewID, String pPmcID) {
        if (mConfigFromServer != null && mConfigFromServer.pattern != null) {
            HashMap<String, String> patternMap = mConfigFromServer.pattern.get(pViewID);
            if (patternMap != null) {
                return patternMap.get(pPmcID);
            }
        }
        return null;
    }


    public List<CardRule> getCreditCardIdentifier() {
        if (mConfigFromServer == null) {
            return null;
        }
        return mConfigFromServer.CCIdentifier;
    }

    public List<CardRule> getBankCardIdentifier() {
        if (mConfigFromServer == null) {
            return null;
        }
        return mConfigFromServer.BankIdentifier;
    }

    public List<DBankScript> getBankScripts() {
        if (mConfigFromServer == null) {
            return null;
        }
        return mConfigFromServer.bankScripts;
    }

    //get otp pattern for each of bank.
    public ArrayList<DOtpReceiverPattern> getOtpReceiverPattern(String pBankCode) {
        if (mConfigFromServer == null || mConfigFromServer.otpReceiverPattern == null) {
            return null;
        }
        ArrayList<DOtpReceiverPattern> otpReceiverPattern = new ArrayList<>();
        for (DOtpReceiverPattern pattern : mConfigFromServer.otpReceiverPattern) {
            if (pattern.bankcode.equals(pBankCode)) {
                otpReceiverPattern.add(pattern);
            }
        }
        return otpReceiverPattern;
    }

    //get otp pattern for each of bank.
    public CardRule getBankIdentifier(String pCode) {
        if (mConfigFromServer == null || mConfigFromServer.BankIdentifier == null) {
            Timber.d("mConfigFromServer is null");
            return null;
        }
        CardRule cardIdentifier = null;
        for (CardRule item : mConfigFromServer.BankIdentifier) {
            if (item.code.equalsIgnoreCase(pCode)) {
                cardIdentifier = item;
                break;
            }
        }
        return cardIdentifier;
    }

    public List<DKeyBoardConfig> getKeyBoardConfig() {
        if (mConfigFromServer == null) {
            Timber.d("mConfigFromServer is null");
            return null;
        }
        return mConfigFromServer.keyboard;
    }

    public DStaticViewGroup getStaticView() {
        return this.mPageConfig.staticView;
    }

    public DDynamicViewGroup getDynamicView() {
        return this.mPageConfig.dynamicView;
    }

    public ResourceRender produceRendering(RenderFragment renderFragment) {
        if (this.mPageConfig == null) {
            return null;
        }
        return new ResourceRender(this, renderFragment);
    }
}
