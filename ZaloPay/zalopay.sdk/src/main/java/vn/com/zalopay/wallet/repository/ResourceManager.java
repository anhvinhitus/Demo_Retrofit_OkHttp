package vn.com.zalopay.wallet.repository;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.WrapContentController;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.entity.staticconfig.CardRule;
import vn.com.zalopay.wallet.business.entity.staticconfig.DConfigFromServer;
import vn.com.zalopay.wallet.business.entity.staticconfig.DKeyBoardConfig;
import vn.com.zalopay.wallet.business.entity.staticconfig.DPage;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.exception.SdkResourceException;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
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
    static DConfigFromServer mConfigFromServer = null;
    private static ResourceManager mCommonResourceManager = null;
    private static Map<String, ResourceManager> mResourceManagerMap = null;
    DPage mPageConfig = null;
    private HashMap<String, String> mStringMap = null;

    public ResourceManager() {
        super();
    }

    public static ResourceManager getInstance(String pPageName) {
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

    static String getResourceFolderPath() {
        return SDKApplication
                .getApplicationComponent()
                .platformInfoInteractor()
                .getResourcePath();
    }

    static String loadFile(String pathPrefix, String fileName) throws Exception {
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
     */
    public static Observable<String> loadJsonConfig() {
        return Observable.defer(() -> {
            String jsonConfig = null;
            try {
                StringBuilder path = new StringBuilder();
                path.append(getResourceFolderPath())
                        .append(File.separator)
                        .append(ResourceManager.CONFIG_FILE);
                jsonConfig = loadAbsolutePath(path.toString());
            } catch (Exception e) {
                Observable.error(e);
            }
            return Observable.just(jsonConfig);
        });
    }

    public static boolean isInit() {
        return mConfigFromServer != null;
    }

    public static synchronized void deleteResFolder() {
        try {
            String resPath = getResourceFolderPath();
            if (TextUtils.isEmpty(resPath)) {
                return;
            }
            File file = new File(resPath);
            if (file.exists()) {
                StorageUtil.deleteRecursive(file);
                Timber.d("delete resource %s", resPath);
            }
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    public static synchronized Observable<Boolean> initResource() {
        return loadJsonConfig()
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String jsonConfig) {
                        if (TextUtils.isEmpty(jsonConfig)) {
                            return Observable.error(new SdkResourceException(GlobalData.getAppContext().getString(R.string.sdk_error_load_resource)));
                        }
                        try {
                            mConfigFromServer = (new DConfigFromServer()).fromJsonString(jsonConfig);
                            ResourceManager commonResourceManager = getInstance(null);
                            if (mConfigFromServer.stringMap != null) {
                                commonResourceManager.setString(mConfigFromServer.stringMap);
                            }
                            if (mConfigFromServer.pageList != null) {
                                for (DPage page : mConfigFromServer.pageList) {
                                    getInstance(page.pageName).mPageConfig = page;
                                }
                            }
                        } catch (Exception e) {
                            return Observable.error(new SdkResourceException(GlobalData.getAppContext().getString(R.string.sdk_error_load_resource)));
                        }
                        return Observable.just(true);
                    }
                })
                .doOnError(throwable -> {
                    Timber.d(throwable);
                    deleteResFolder();
                });
    }

    public static Observable<String> getJavascriptContent(String pJsName) {
        return Observable.defer(() -> {
            try {
                return Observable.just(loadFile(PREFIX_JS, pJsName));
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

    public static Observable<Bitmap> getImage(String imageName) {
        return Observable.defer(() -> {
            if (imageName.equals(HIDE_IMG_NAME)) {
                return Observable.empty();
            }
            String imgLocalPath;
            Bitmap bitmap = null;
            try {
                imgLocalPath = String.format("%s%s%s%s%s", getResourceFolderPath(), File.separator, PREFIX_IMG, File.separator, imageName);
                bitmap = BitmapFactory.decodeFile(imgLocalPath);
            } catch (Exception e) {
                Observable.error(e);
            }
            return Observable.just(bitmap);
        });
    }

    public static String getAbsoluteImagePath(String pImageName) {
        try {
            return String.format("file://%s%s%s%s%s", getResourceFolderPath(), File.separator, PREFIX_IMG, File.separator, pImageName);
        } catch (Exception e) {
            Timber.w(e, "Exception getAbsoluteImagePath");
        }
        return null;
    }

    /***
     * load image into SimpleDraweeView
     * use Fresco
     */
    public static void loadLocalSDKImage(View pView, String pImageName) {
        try {
            String iconPath = getAbsoluteImagePath(pImageName);
            if (TextUtils.isEmpty(iconPath)) {
                return;
            }
            loadLocalImage(pView, iconPath);
        } catch (Exception e) {
            Timber.w(e, "Exception load sdk image into View");
        }
    }

    public static void loadLocalImage(View pView, String pImagePath) {
        try {
            if (TextUtils.isEmpty(pImagePath)) {
                return;
            }
            if (pView == null) {
                return;
            }
            if (!(pView instanceof SimpleDraweeView)) {
                return;
            }
            pImagePath = String.format("file://%s", pImagePath);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(pImagePath)
                    .setControllerListener(new WrapContentController((ImageView) pView))
                    .build();
            ((SimpleDraweeView) pView).setController(controller);
        } catch (Exception e) {
            Timber.w(e, "Exception load local image into View");
        }
    }

    public static String getFontFolder() {
        try {
            return getResourceFolderPath() + PREFIX_FONT;
        } catch (Exception e) {
            Timber.w(e, "Exception get font folder");
        }
        return null;
    }

    public static void loadRemoteImage(View pView, String url) {
        try {
            if (TextUtils.isEmpty(url)) {
                return;
            }
            if (pView == null) {
                return;
            }
            if (!(pView instanceof SimpleDraweeView)) {
                return;
            }
            SimpleDraweeView view = (SimpleDraweeView) pView;
            view.setImageURI(Uri.parse(url));
        } catch (Exception e) {
            Timber.w(e, "Exception load remote image into View");
        }
    }

    public String getString(String pKey) {
        if (this.mStringMap != null) {
            return this.mStringMap.get(pKey);
        }
        return null;
    }

    void setString(HashMap<String, String> pMap) {
        if (pMap != null) {
            this.mStringMap = pMap;
        }
    }

    public String getPattern(String pViewID, String pPmcID) {
        if (mConfigFromServer == null || mConfigFromServer.pattern == null) {
            return null;
        }
        HashMap<String, String> patternMap = mConfigFromServer.pattern.get(pViewID);
        if (patternMap != null) {
            return patternMap.get(pPmcID);
        }
        return null;
    }


    public List<CardRule> getCreditCardIdentifier() {
        return mConfigFromServer != null ? mConfigFromServer.CCIdentifier : null;
    }

    public List<CardRule> getBankCardIdentifier() {
        return mConfigFromServer != null ? mConfigFromServer.BankIdentifier : null;
    }

    public List<DBankScript> getBankScripts() {
        return mConfigFromServer != null ? mConfigFromServer.bankScripts : null;
    }

    //get otp pattern for each of bank.
    public ArrayList<DOtpReceiverPattern> getOtpReceiverPattern(String pBankCode) {
        if (mConfigFromServer == null || mConfigFromServer.otpReceiverPattern == null) {
            return null;
        }
        if (TextUtils.isEmpty(pBankCode)) {
            return null;
        }
        ArrayList<DOtpReceiverPattern> otpReceiverPattern = new ArrayList<>();
        for (DOtpReceiverPattern pattern : mConfigFromServer.otpReceiverPattern) {
            if (!pBankCode.equals(pattern.bankcode)) {
                continue;
            }
            otpReceiverPattern.add(pattern);
        }
        return otpReceiverPattern;
    }

    //get otp pattern for each of bank.
    public CardRule getBankIdentifier(String pBankCode) {
        if (mConfigFromServer == null || mConfigFromServer.BankIdentifier == null) {
            Timber.d("ConfigFromServer is null");
            return null;
        }
        if (TextUtils.isEmpty(pBankCode)) {
            return null;
        }
        CardRule cardIdentifier = null;
        for (CardRule item : mConfigFromServer.BankIdentifier) {
            if (pBankCode.equalsIgnoreCase(item.code)) {
                cardIdentifier = item;
                break;
            }
        }
        return cardIdentifier;
    }

    public List<DKeyBoardConfig> getKeyBoardConfig() {
        return mConfigFromServer != null ? mConfigFromServer.keyboard : null;
    }

    public DStaticViewGroup getStaticView() {
        return this.mPageConfig != null ? this.mPageConfig.staticView : null;
    }

    public DDynamicViewGroup getDynamicView() {
        return this.mPageConfig != null ? this.mPageConfig.dynamicView : null;
    }

    public ResourceRender produceRendering(RenderFragment renderFragment) {
        if (this.mPageConfig == null) {
            return null;
        }
        return new ResourceRender(this, renderFragment);
    }
}
