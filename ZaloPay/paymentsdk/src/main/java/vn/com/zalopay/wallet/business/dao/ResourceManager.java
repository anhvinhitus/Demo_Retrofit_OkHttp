package vn.com.zalopay.wallet.business.dao;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.entity.staticconfig.DCardIdentifier;
import vn.com.zalopay.wallet.business.entity.staticconfig.DConfigFromServer;
import vn.com.zalopay.wallet.business.entity.staticconfig.DKeyBoardConfig;
import vn.com.zalopay.wallet.business.entity.staticconfig.DPage;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.ActivityRendering;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;

public class ResourceManager extends SingletonBase {
    public static final String CONFIG_FILE = "config.json";
    private static final String TAG = ResourceManager.class.getName();
    private static final String PREFIX_JS = "/js/";
    private static final String PREFIX_IMG = "/img/";
    private static final String PREFIX_FONT = "/fonts/";
    private static final String HIDE_IMG_NAME = "0.png";
    public static boolean mIsCreated = false;
    private static String mUnzipPath = null;
    private static ResourceManager mCommonResourceManager = null;
    private static Map<String, ResourceManager> mResourceManagerMap = null;
    private static DConfigFromServer mConfigFromServer = null;
    private String mPageName = null;
    private HashMap<String, String> mStringMap = null;
    private DPage mPageConfig = null;

    public ResourceManager(String pPageName) {
        super();
        this.mPageName = pPageName;
    }

    public static synchronized ResourceManager getInstance(String pPageName) {
        if (pPageName == null) {
            if (mCommonResourceManager == null)
                mCommonResourceManager = new ResourceManager(null);
            return mCommonResourceManager;
        } else {
            if (mResourceManagerMap == null) {
                mResourceManagerMap = new HashMap<String, ResourceManager>();
            }

            ResourceManager resourceManager = mResourceManagerMap.get(pPageName);

            if (resourceManager != null) {
                return resourceManager;
            }

            resourceManager = new ResourceManager(pPageName);
            mResourceManagerMap.put(pPageName, resourceManager);

            return resourceManager;
        }
    }

    /************************************************************************/

    private static String getUnzipFolderPath() throws Exception {
        if (SharedPreferencesManager.getInstance() == null)
            throw new Exception("Missing shared preferences!!!");

        if (mUnzipPath == null)
            mUnzipPath = SharedPreferencesManager.getInstance().getUnzipPath();

        return mUnzipPath;
    }

    private static String loadResourceFile(String pPathNamePrefix, String pFileName) throws Exception {
        String result = "";
        String path = getUnzipFolderPath() + File.separator
                + ((pPathNamePrefix != null) ? (pPathNamePrefix + pFileName) : pFileName);
        File file = new File(path);

        if (file.exists()) {
            try {
                InputStream inputStream = new FileInputStream(file);
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String line = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append("\r\n");
                    }

                    bufferedReader.close();
                    inputStreamReader.close();
                    inputStream.close();

                    result = stringBuilder.toString();
                }
            } catch (Exception ex) {
                Log.e("loadResourceFile", ex);
            }
        }
        result = result.trim();
        return result;
    }

    /***
     * load config from config.json
     *
     * @return
     * @throws Exception
     */
    public static String loadResourceFile() throws Exception {
        String result = "";
        String path = getUnzipFolderPath() + File.separator + ResourceManager.CONFIG_FILE;

        File file = new File(path);

        if (file.exists()) {
            try {
                InputStream inputStream = new FileInputStream(file);
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String line = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append("\r\n");
                    }

                    bufferedReader.close();
                    inputStreamReader.close();
                    inputStream.close();

                    result = stringBuilder.toString();
                }
            } catch (Exception ex) {
                throw ex;
            }
        }

        result = result.trim();
        return result;
    }

    public static boolean isInit() {
        return (mIsCreated == true) && (mConfigFromServer != null);
    }

    public static synchronized void initResource() throws Exception {
        try {
            Log.d("ResourceManager", "===initResource===");

            String json = loadResourceFile(null, CONFIG_FILE);

            if (TextUtils.isEmpty(json)) {
                mIsCreated = false;
                return;
            }
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

            mIsCreated = true;


        } catch (Exception e) {
            Log.e("initResource", e);

            mIsCreated = false;
        }

    }

    public static String getJavascriptContent(String pJsName) {
        try {
            return loadResourceFile(PREFIX_JS, pJsName);
        } catch (Exception e) {
            Log.e("getJavascriptContent", e);
        }
        return null;
    }

    public static Bitmap getImage(String imageName) {
        if (imageName.equals(HIDE_IMG_NAME)) {
            return null;
        }

        String imgLocalPath = null;

        Bitmap bitmap = null;

        try {
            imgLocalPath = String.format("%s%s%s%s%s", getUnzipFolderPath(), File.separator, PREFIX_IMG, File.separator, imageName);

            bitmap = BitmapFactory.decodeFile(imgLocalPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static String getPathFont() {
        try {
            return getUnzipFolderPath() + PREFIX_FONT;
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


    public List<DCardIdentifier> getCreditCardIdentifier() {
        if (mConfigFromServer == null)
            return null;

        return mConfigFromServer.CCIdentifier;
    }

    public List<DCardIdentifier> getBankCardIdentifier() {
        if (mConfigFromServer == null)
            return null;

        return mConfigFromServer.BankIdentifier;
    }

    public String getResourcePath() {
        return mUnzipPath;
    }

    public List<DBankScript> getBankScripts() {
        if (mConfigFromServer == null)
            return null;

        return mConfigFromServer.bankScripts;
    }

    //get otp pattern for each of bank.
    public ArrayList<DOtpReceiverPattern> getOtpReceiverPattern(String pBankCode) {
        if (mConfigFromServer == null || mConfigFromServer.otpReceiverPattern == null)
            return null;

        ArrayList<DOtpReceiverPattern> otpReceiverPattern = new ArrayList<>();

        for (DOtpReceiverPattern pattern : mConfigFromServer.otpReceiverPattern) {
            if (pattern.bankcode.equals(pBankCode)) {
                otpReceiverPattern.add(pattern);
            }
        }

        return otpReceiverPattern;
    }

    //get otp pattern for each of bank.
    public DCardIdentifier getBankIdentifier(String pCode) {
        if (mConfigFromServer == null || mConfigFromServer.BankIdentifier == null) {
            Log.d(this, "===mConfigFromServer=NULL");
            return null;
        }

        DCardIdentifier cardIdentifier = null;

        for (DCardIdentifier item : mConfigFromServer.BankIdentifier) {
            if (item.code.equalsIgnoreCase(pCode)) {
                cardIdentifier = item;
                break;
            }
        }

        return cardIdentifier;
    }

    public List<DKeyBoardConfig> getKeyBoardConfig() {
        if (mConfigFromServer == null) {
            Log.d(this, "===mConfigFromServer=NULL");
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

    public ActivityRendering produceRendering(BasePaymentActivity pActivity) {
        if (this.mPageConfig == null)
            return null;

        return new ActivityRendering(this, pActivity);
    }
}
