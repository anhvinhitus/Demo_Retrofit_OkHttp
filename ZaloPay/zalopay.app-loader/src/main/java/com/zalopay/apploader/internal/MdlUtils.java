package com.zalopay.apploader.internal;

import java.io.File;

import timber.log.Timber;

/**
 * Created by huuhoa on 4/29/16.
 * Some utilities functions
 */
public class MdlUtils {
    public static String appendPathComponent(String basePath, String appendPathComponent) {
        return new File(basePath, appendPathComponent).getAbsolutePath();
    }

    public static void log(String message) {
        Timber.d(message);
    }
}
