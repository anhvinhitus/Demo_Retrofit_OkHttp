package vn.com.vng.zalopay.mdl.impl;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import com.facebook.react.ReactInstanceManager;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import timber.log.Timber;
import vn.com.vng.zalopay.data.download.FileUtil;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.repository.LocalResourceRepository;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.mdl.MiniApplicationException;
import vn.com.vng.zalopay.mdl.internal.FileUtils;
import vn.com.vng.zalopay.mdl.model.ReactBundleAssetData;

/**
 * Created by huuhoa on 4/25/16.
 * Manage mini application bundles.
 * There are two types of bundle to manage:
 * + Internal mini application bundle: some core app's functionality are implemented using react-native
 * + External mini application bundle: external payment scenarios implemented using react-native
 * `BundleService` provide following functions:
 * + Check for update of internalBundle, some externalBundles (externalBundles that are parts of app)
 * + Download bundle updates
 */
public class BundleServiceImpl implements BundleService {
    Application mApplication;
    public String mCurrentInternalBundleFolder;
    private final LocalResourceRepository mLocalResourceRepository;

    private Gson mGson;

    public BundleServiceImpl(Application application, LocalResourceRepository localResourceRepository, Gson gson) {
        mApplication = application;
        this.mLocalResourceRepository = localResourceRepository;
        this.mGson = gson;
    }

    @Override
    public String getInternalBundleFolder() {
        return mCurrentInternalBundleFolder;
    }

    @Override
    public String getExternalBundleFolder(String paymentAppName) {
        // TODO: 5/18/16 Return real folder of the payment app
        return "";
    }

    private String getBundleRoot() {
        String packageName = mApplication.getPackageName();
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + packageName + File.separator +"bundles";
    }

    private String getInternalBundleRoot() {
        return getBundleRoot() + File.separator + "modules/zalopay";
    }

    public String loadStringFromStream(InputStream is) {
        String json = null;
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            if (is.read(buffer) < 0) {
                buffer[0] = 0;
            }
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void ensureDirectory(String path) {
        File file = new File(path);
        ensureDirectory(file);
    }

    private void ensureDirectory(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String getRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public String getResourcePath() {
        return getRootPath() + File.separator + "zmres";
    }

    public String getTempFilePath() {
        return getResourcePath() + File.separator + "temp.zip";
    }

    public String getRootApplicationPath(AppResource resource) {
        return getResourcePath() + File.separator + resource.appname;
    }

    public String getRootApplicationPath(ReactBundleAssetData.ExternalBundle resource) {
        return getResourcePath() + File.separator + resource.appname;
    }

    public File getFileVersionApplication(ReactBundleAssetData.ExternalBundle ebundle) {
        return new File(getRootApplicationPath(ebundle), "version.txt");
    }

    public String getUnZipPath(ReactBundleAssetData.ExternalBundle ebundle) {
        return getRootApplicationPath(ebundle) + File.separator + "app";
    }

    @Override
    public void ensureLocalResources() {
        try {
            PackageInfo packageInfo = mApplication.getPackageManager().getPackageInfo(mApplication.getPackageName(), 0);
            Timber.i("Version name: %s", packageInfo.versionName);

            ensureInternalLocalResources(packageInfo);
            ensurePaymentAppLocalResources(packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Error!!!");
        }
    }

    private void ensureInternalLocalResources(PackageInfo packageInfo) {
        String currentInternalVersion = mLocalResourceRepository.getInternalResourceVersion();
        if (currentInternalVersion == null) {
            currentInternalVersion = "";
        }
        Timber.i("Internal version: %s", currentInternalVersion);
        if (!currentInternalVersion.equalsIgnoreCase(packageInfo.versionName)) {
            Timber.i("Need to update internal resource");
            if (updateInternalResource()) {
                mLocalResourceRepository.setInternalResourceVersion(packageInfo.versionName);
            }
        } else {
            Timber.i("Internal resource is updated");
        }

        mCurrentInternalBundleFolder = getInternalBundleRoot();
    }

    private void ensurePaymentAppLocalResources(PackageInfo packageInfo) {
        Timber.i("Extract External Application Start");

        AssetManager assetManager = mApplication.getAssets();

        String bundle;

        try {
            bundle = loadStringFromStream(assetManager.open("bundle.json"));
        } catch (IOException ex) {
            Timber.e(ex, "IOException loadStringFromStream");
            return;
        }

        ReactBundleAssetData reactBundleAssetData = mGson.fromJson(bundle, ReactBundleAssetData.class);

        for (ReactBundleAssetData.ExternalBundle ebundle : reactBundleAssetData.external_bundle) {
            String appVersion = mLocalResourceRepository.getExternalResourceVersion(ebundle.appid);
            if (appVersion != null && appVersion.equalsIgnoreCase(packageInfo.versionName)) {
                continue;
            }

            if (!updatePaymentAppLocalResource(ebundle)) {
                continue;
            }
            mLocalResourceRepository.setExternalResourceVersion(ebundle.appid, packageInfo.versionName);
        }

        Timber.i("Extract External Application done");
    }

    private boolean updatePaymentAppLocalResource(ReactBundleAssetData.ExternalBundle bundle) {
        String rootApp = getRootApplicationPath(bundle);
        ensureDirectory(rootApp);

        String destination = getUnZipPath(bundle);

        Timber.d("destination %s %s ", destination, bundle.appname);

        try {
            InputStream stream = mApplication.getAssets().open("external/" + bundle.asset);
            FileUtils.unzipFile(stream, destination, true);

            return true;
        } catch (Exception e) {
            Timber.e(e, "exception %s", e);
            return false;
        }
    }

    /**
     * Extract zalopay_internal.zip from apk's assets and unzip to destination folder
     * @return true if succeeded
     */
    private boolean updateInternalResource() {
        Timber.d("updateInternalResource");

        String internalRoot = getInternalBundleRoot();
        ensureDirectory(internalRoot);

        try {
            InputStream stream = mApplication.getAssets().open("zalopay_internal.zip");
            FileUtils.unzipFile(stream, internalRoot, true);
        } catch (IOException e) {
            Timber.e(e, "Error loading bundle info");
            return false;
        } catch (MiniApplicationException e) {
            Timber.e(e, "Error extracting internal local resource");
            return false;
        }
        return true;
    }
}
