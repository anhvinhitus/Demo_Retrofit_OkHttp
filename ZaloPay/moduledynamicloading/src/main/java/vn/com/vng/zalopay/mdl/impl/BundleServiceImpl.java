package vn.com.vng.zalopay.mdl.impl;

import android.app.Application;
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
import vn.com.vng.zalopay.mdl.BundleService;
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
    ReactInstanceManager mInternalBundleInstanceManager;
    Application mApplication;
    public String mCurrentInternalBundleFolder;
    String mCurrentInternalBundleVersion;
    String mExpectedInternalBundleVersion;

    private Gson mGson;

    public BundleServiceImpl(Application application, Gson gson) {
        mApplication = application;
        this.mGson = gson;
        prepareBundleEnvironment();
    }

    @Override
    public ReactInstanceManager getInternalBundleInstanceManager() {
        return mInternalBundleInstanceManager;
    }

    @Override
    public boolean checkForInternalBundleUpdate() {
        return false;
    }

    @Override
    public void downloadInternalBundle() {
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

    @Override
    public void extractAllExternalApplication() {

        Timber.i("Extract External Application Start");

        AssetManager assetManager = mApplication.getAssets();

        String bundle = null;

        try {
            bundle = loadStringFromStream(assetManager.open("bundle.json"));
        } catch (IOException ex) {
            Timber.e(ex, "IOException loadStringFromStream");
            return;
        }

        ReactBundleAssetData reactBundleAssetData = mGson.fromJson(bundle, ReactBundleAssetData.class);

        for (ReactBundleAssetData.ExternalBundle ebundle : reactBundleAssetData.external_bundle) {
            String rootApp = getRootApplicationPath(ebundle);
            ensureDirectory(rootApp);

            File versionFile = getFileVersionApplication(ebundle);

            boolean isExtract = false;

            if (!versionFile.exists()) {
                isExtract = true;
            } else {
                String version = loadStringFromFile(versionFile);
                if (TextUtils.isEmpty(version) || !version.equals(ebundle.version)) {
                    isExtract = true;
                }
            }


            Timber.d("isExtract %s %s", ebundle, isExtract);

            if (isExtract) {
                String destination = getUnZipPath(ebundle);

                FileUtils.deleteDirectoryAtPath(destination);

                Timber.d("destination %s %s ", destination, ebundle.appname);

                ensureDirectory(destination);

                File fileAsset = new File(getRootApplicationPath(ebundle), ebundle.asset);
                copyAssetToDirectory("external/" + ebundle.asset, fileAsset.getAbsolutePath());

                try {
                    FileUtil.unzip(fileAsset.getAbsolutePath(), destination);
                } catch (Exception e) {
                    Timber.e(e, "exception %s", e);
                } finally {
                    if (fileAsset.exists()) {
                        fileAsset.delete();
                    }
                }

                try {
                    writeToFile(ebundle.version, versionFile.getAbsolutePath());
                } catch (Exception ex) {
                    Timber.e(ex, " write to file exception");
                }
            }

        }

        Timber.i("Extract External Application done");

    }


    private boolean equalsVersion(ReactBundleAssetData.ExternalBundle ebundle, String oldVersion) {
        if (!TextUtils.isEmpty(oldVersion) && !oldVersion.equals(ebundle.version)) {
            return true;
        }
        return false;
    }


    @Override
    public void prepareInternalBundle() {
        Timber.d("Hello from bundle Service");

        String currentVersion = getCurrentInternalBundleVersion();
        if (currentVersion != null && currentVersion.compareTo(mExpectedInternalBundleVersion) >= 0) {
            Timber.d("Internal version is updated");
            mCurrentInternalBundleFolder = String.format("%s/zalopay_v%s", getInternalBundleRoot(), mExpectedInternalBundleVersion);
        } else {
            String fileName = String.format("zalopay_v%s.zip", mExpectedInternalBundleVersion);
            String bundleFolder = String.format("%s/zalopay_v%s", getInternalBundleRoot(), mExpectedInternalBundleVersion);

            String folder = copyAssets(fileName, bundleFolder);

            mCurrentInternalBundleFolder = folder;
            setCurrentInternalBundleVersion(mExpectedInternalBundleVersion);
        }

        //initializeInternalBundleInstanceManager();
        Timber.d("Done");
    }

    private void setCurrentInternalBundleVersion(String bundleVersion) {
        try {
            File currentVersionFile = new File(getInternalBundleRoot(), "VERSION");
            FileOutputStream out = new FileOutputStream(currentVersionFile);
            out.write(bundleVersion.getBytes());
            out.close();
        } catch (IOException e) {
            Timber.e(e, "Cannot update internal bundle version");
        }
    }

    void prepareExternalBundle() {

    }

    private void initializeInternalBundleInstanceManager() {
//        mInternalBundleInstanceManager = ReactInstanceManager.builder()
//                .setApplication(mApplication)
//                .setBundleAssetName("index.android.js")
//                .setJSMainModuleName("index.android")
//                .addPackage(new MainReactPackage())
//                .addPackage(new ReactInternalPackage())
////                .setUseDeveloperSupport(true)
//                .setUseDeveloperSupport(false)
//                .setInitialLifecycleState(LifecycleState.RESUMED)
//                .build();
    /*    mInternalBundleInstanceManager = ReactInstanceManager.builder()
                .setApplication(mApplication)
                .setJSBundleFile(mCurrentInternalBundleFolder + "/main.jsbundle")
                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .addPackage(new ReactInternalPackage())
//                .setUseDeveloperSupport(true)
                .setUseDeveloperSupport(false)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();*/
    }

    String getCurrentInternalBundleVersion() {
        File currentVersionFile = new File(getInternalBundleRoot(), "VERSION");
        if (!currentVersionFile.exists()) {
            return null;
        }

        return loadStringFromFile(currentVersionFile);
    }

    private void prepareBundleEnvironment() {
        Timber.d("prepareBundleEnvironment");
        // create root bundle folder
        String root = getBundleRoot();
        ensureDirectory(root);
        String internalRoot = getInternalBundleRoot();
        ensureDirectory(internalRoot);

        try {
            File metadataFile = new File(root, "bundle.info");
            if (!metadataFile.exists()) {
                copyAssetToDirectory("bundle.json", metadataFile.getPath());
                mExpectedInternalBundleVersion = extractVersionFromStream(new FileInputStream(metadataFile));
            } else {
                // if shipped version is newer, then copy
                String shipVersion = getShippedInternalBundleVersion();
                String currentVersion = extractVersionFromStream(new FileInputStream(metadataFile));
                Timber.d("Shipped version: %s", shipVersion);
                Timber.d("Current version: %s", currentVersion);
                if (compareVersion(shipVersion, currentVersion) > 0) {
                    copyAssetToDirectory("bundle.json", metadataFile.getPath());
                    currentVersion = shipVersion;
                }

                mExpectedInternalBundleVersion = currentVersion;
            }

            Timber.d("Internal bundle version: %s", mExpectedInternalBundleVersion);
        } catch (IOException e) {
            Timber.e(e, "Error loading bundle info");
        }
    }

    private int compareVersion(String version1, String version2) {
        String[] vals1 = version1.split("\\.");
        String[] vals2 = version2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }

    String getShippedInternalBundleVersion() {
        AssetManager assetManager = mApplication.getAssets();
        try {
            InputStream in = assetManager.open("bundle.json");
            return extractVersionFromStream(in);
        } catch (IOException e) {
            Timber.e(e, "Error loading bundle info");
            return null;
        }
    }

    private String extractVersionFromStream(InputStream stream) {
        try {
            JSONObject jsonObject = new JSONObject(loadStringFromStream(stream));

            JSONObject internalBundle = jsonObject.getJSONObject("internal_bundle");

            String version = internalBundle.getString("version");
            Timber.d("Internal bundle version: %s", version);

            return version;
        } catch (JSONException e) {
            Timber.e(e, "Error loading bundle info");

            return null;
        }
    }

    private String getBundleRoot() {
        String packageName = mApplication.getPackageName();
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + packageName + "/bundles";
    }

    private String getInternalBundleRoot() {
        return getBundleRoot() + "/modules/zalopay";
    }

    public String loadStringFromFile(File file) {
        try {
            InputStream is = new FileInputStream(file);
            return loadStringFromStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeToFile(final String fileContents, String path) throws IOException {
        FileWriter out = null;
        try {
            out = new FileWriter(new File(path));
            out.write(fileContents);
        } finally {
            if (out != null) {
                out.close();
            }
        }
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

    private String copyAssets(String assetName, String destinationFolder) {
        String packageName = mApplication.getPackageName();
        try {
            ensureDirectory(destinationFolder);
            deleteContents(new File(destinationFolder));

            copyAssetToDirectory(assetName, destinationFolder + "/" + assetName);

            unzip(destinationFolder + "/" + assetName, destinationFolder);

            File outFile = new File(destinationFolder, assetName);
            outFile.delete();

            return destinationFolder;
        } catch (Exception e) {
            Timber.e(e, "Failed to copy asset file: %s", assetName);
            return null;
        }
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

    private void copyAssetToDirectory(String assetName, String destinationPath) {
        AssetManager assetManager = mApplication.getAssets();
        try {
            InputStream in = assetManager.open(assetName);
            OutputStream out = new FileOutputStream(destinationPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (IOException e) {
            Timber.e(e, "Failed to copy asset file: %s", assetName);
        }
    }

    public static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Timber.w("Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void unzip(String zipFilePath, String destinationPath) {
        try {
            File archive = new File(zipFilePath);

            ZipFile zipfile = new ZipFile(archive);

            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                Timber.d("Unzip file %s", entry.getName());
                unzipEntry(zipfile, entry, destinationPath);

            }

            zipfile.close();
        } catch (Exception e) {
            Timber.e(e, "Unzip exception");
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        Timber.v("Extracting: " + entry);

        InputStream zin = zipfile.getInputStream(entry);
        BufferedInputStream inputStream = new BufferedInputStream(zin);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            copyFile(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {

        if (dir.exists()) {
            return;
        }

        Timber.v("Creating dir " + dir.getName());

        if (!dir.mkdirs()) {

            throw new RuntimeException("Can not create dir " + dir);
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


}
