package vn.com.vng.zalopay.mdl;

import android.app.Application;
import android.content.res.AssetManager;
import android.os.Environment;

import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.shell.MainReactPackage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import timber.log.Timber;
import vn.com.vng.zalopay.mdl.internal.ReactInternalPackage;

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

    public BundleServiceImpl(Application application) {
        mApplication = application;
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
    public void prepareInternalBundle() {
        Timber.d("Hello from bundle Service");
        String folder = copyAssets("zalopay_v1.zip", "zalopay");
        mInternalBundleInstanceManager = ReactInstanceManager.builder()
                .setApplication(mApplication)
                .setJSBundleFile(folder + "/index.android.js")
                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .addPackage(new ReactInternalPackage())
//                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setUseDeveloperSupport(false)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        Timber.d("Done");
    }

    void prepareExternalBundle() {

    }

    private String copyAssets(String assetName, String destinationFolder) {
        AssetManager assetManager = mApplication.getAssets();
        String packageName = mApplication.getPackageName();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(assetName);

            String outFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + packageName + "/bundles/modules/" + destinationFolder;

            File outFile = new File(outFolder, assetName);
            File dir = outFile.getParentFile();
            if (dir.exists()) {
                deleteContents(dir);
            } else {
                dir.mkdirs();
            }

            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;

            unzip(outFile.getPath(), outFolder);

            return outFolder;
        } catch (IOException e) {
            Timber.e(e, "Failed to copy asset file: %s", assetName);
            return null;
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
}
