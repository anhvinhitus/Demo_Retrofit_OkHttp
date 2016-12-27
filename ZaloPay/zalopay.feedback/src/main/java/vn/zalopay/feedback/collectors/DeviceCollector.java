package vn.zalopay.feedback.collectors;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vn.zalopay.feedback.CollectorSetting;
import vn.zalopay.feedback.IFeedbackCollector;

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

        try {
            JSONObject retVal = new JSONObject();
            retVal.put("model", Build.MODEL);
            retVal.put("os_version", System.getProperty("os.version"));
            retVal.put("cpu", getCpu());
            retVal.put("ram", getRam());
            retVal.put("internal_memory", getTotalMemorySize(internalPath));
            retVal.put("external_memory", getTotalMemorySize(externalPath));
//            retVal.put("version", Build.VERSION.RELEASE);
//            retVal.put("sdk_version", Build.VERSION.SDK_INT);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                retVal.put("abilist", Build.SUPPORTED_ABIS);
//            }

            return retVal;
        } catch (JSONException e) {
            return null;
        }
    }

    private String getCpu() {
        if (new File("/proc/cpuinfo").exists()) {
            try {
                Scanner s = new Scanner(new File("/proc/cpuinfo"));
                while (s.hasNextLine()) {
                    String[] values = s.nextLine().split(": ");
                    if (values.length > 1 && values[0].trim().contains("model name")) {
                        return values[1].trim();
                    }
                }
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    private static long getRam() {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile("/proc/meminfo", "r");
            String load = randomAccessFile.readLine();
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(load);
            String value = "";
            while (matcher.find()) {
                value = matcher.group(1);
            }
            randomAccessFile.close();

            return Long.parseLong(value);
        } catch (IOException ex) {
            return 0;
        }
    }

    private static long getTotalMemorySize(StatFs statFs) {
        long blockSize, totalBlocks;
        if(Build.VERSION.SDK_INT >= 18) {
            blockSize = statFs.getBlockSizeLong();
            totalBlocks = statFs.getBlockCountLong();
        }
        else {
            blockSize = statFs.getBlockSize();
            totalBlocks = statFs.getBlockCount();
        }
        return totalBlocks * blockSize;
    }
}
