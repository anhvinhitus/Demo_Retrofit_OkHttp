package vn.zalopay.feedback.collectors;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import timber.log.Timber;
import vn.zalopay.feedback.CollectorSetting;
import vn.zalopay.feedback.IFeedbackCollector;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.text.format.Formatter.formatFileSize;

/**
 * Created by khattn on 12/27/16.
 * Collect device information
 */

public class DeviceCollector implements IFeedbackCollector {
    private static CollectorSetting sSetting;

    static {
        sSetting = new CollectorSetting();
        sSetting.userVisibility = true;
        sSetting.displayName = "Device Information";
        sSetting.dataKeyName = "deviceinfo";
    }

    private Context mContext;

    public DeviceCollector(Context context) {
        this.mContext = context;
    }

    /**
     * Get pre-config settings for data collector
     */
    @Override
    public CollectorSetting getSetting() {
        return sSetting;
    }

    /**
     * Start collecting data. If data is collected, then return JSONObject of the encoded data
     *
     * @return JSONObject value, null if data is not collected
     */
    @Override
    public JSONObject doInBackground() {

        StatFs internalPath = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        StatFs externalPath = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());

        JSONObject retVal = new JSONObject();

        try {

            retVal.put("model", Build.MODEL);
            retVal.put("os_version", System.getProperty("os.version"));
            retVal.put("cpu", getCpu());
            retVal.put("api_level", Build.VERSION.SDK_INT);
            retVal.put("density", mContext.getResources().getDisplayMetrics().density);

            retVal.put("internal_memory", formatFileSize(mContext, getTotalMemorySize(internalPath)));
            retVal.put("external_memory", formatFileSize(mContext, getTotalMemorySize(externalPath)));
            retVal.put("internal_memory_available", formatFileSize(mContext, getAvailableMemorySize(internalPath)));
            retVal.put("external_memory_available", formatFileSize(mContext, getAvailableMemorySize(externalPath)));

            ActivityManager.MemoryInfo memoryInfo = getMemoryInfo(mContext);
            retVal.put("ram", formatFileSize(mContext, memoryInfo.totalMem));
            retVal.put("availMem", formatFileSize(mContext, memoryInfo.availMem));
            retVal.put("threshold", formatFileSize(mContext, memoryInfo.threshold));

        } catch (Exception e) {
            Timber.d(e);
        }

        return retVal;
    }

    private String getCpu() {
        if (new File("/proc/cpuinfo").exists()) {
            Scanner scanner = null;
            try {
                scanner = new Scanner(new File("/proc/cpuinfo"));
                while (scanner.hasNextLine()) {
                    String[] values = scanner.nextLine().split(": ");
                    if (values.length > 1 && values[0].trim().contains("model name")) {
                        return values[1].trim();
                    }
                }
            } catch (IOException e) {
                return null;
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }
        return null;
    }

    private static ActivityManager.MemoryInfo getMemoryInfo(Context context) throws Exception {
        ActivityManager actManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return memInfo;
    }

    private static long getTotalMemorySize(StatFs statFs) {
        if (Build.VERSION.SDK_INT >= 18) {
            return statFs.getTotalBytes();
        } else {
            return statFs.getBlockSize() * statFs.getBlockCount();
        }

    }

    private static long getAvailableMemorySize(StatFs statFs) {
        if (Build.VERSION.SDK_INT >= 18) {
            return statFs.getAvailableBytes();
        } else {
            return statFs.getBlockSize() * statFs.getAvailableBlocks();
        }
    }

    @Override
    public void cleanUp() {

    }
}
