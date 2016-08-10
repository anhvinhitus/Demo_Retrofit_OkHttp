package vn.com.vng.zalopay.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

import timber.log.Timber;

/**
 * Created by longlv on 10/08/2016.
 * cache typeface
 */
public class TypefaceHelper {
    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    public static Typeface get(Context c, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(), assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    Timber.w(e, "get typeface exception [%s]", e.getMessage());
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }
}