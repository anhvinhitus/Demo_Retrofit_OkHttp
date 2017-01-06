package com.zalopay.ui.widget.util;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by longlv on 11/17/16.
 * Cache fonts that app is using.
 */

public class FontHelper {
    private static FontHelper mInstance;

    public static FontHelper getInstance() {
        if (mInstance == null) {
            mInstance = new FontHelper();
        }
        return mInstance;
    }

    private Map<String, Typeface> mFonts;

    private FontHelper() {
        this.mFonts = new HashMap<>();
    }

    public Typeface getFontFromAsset(AssetManager assetManager, String asset) {
        if (assetManager == null || TextUtils.isEmpty(asset)) {
            return null;
        }
        asset = fixFontFileName(asset);
        if (!asset.startsWith("fonts/")) {
            asset = "fonts/" + asset;
        }
        if (mFonts.containsKey(asset))
            return mFonts.get(asset);

        Typeface font = null;

        try {
            font = Typeface.createFromAsset(assetManager, asset);
            mFonts.put(asset, font);
        } catch (Exception e) {
            Log.d("FontHelper", "Get font, font name should contain extension: " + asset);
        }

        return font;
    }

    public Typeface getFontFromFile(String filePath) {
        //String filePath = "/storage/emulated/0/fonts/" + fileName;
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        if (mFonts.containsKey(filePath))
            return mFonts.get(filePath);

        Typeface typeface = null;

        try {
            typeface = Typeface.createFromFile(filePath);
            mFonts.put(filePath, typeface);
        } catch (Exception e) {
            Log.d("FontHelper", "Get font, font name should contain extension: " + filePath);
        }

        return typeface;
    }

    private String fixFontFileName(String asset) {
        // Empty font filename?
        // Just return it. We can't help.
        if (TextUtils.isEmpty(asset))
            return asset;

        // Make sure that the font ends in '.ttf' or '.ttc'
        if ((!asset.endsWith(".ttf")) && (!asset.endsWith(".ttc")))
            asset = String.format("%s.ttf", asset);

        return asset;
    }
}