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

    private AssetManager mAssetManager;

    private Map<String, Typeface> mFonts;

    private FontHelper(AssetManager _mgr) {
        mAssetManager = _mgr;
        mFonts = new HashMap<>();
    }

    public static void init(AssetManager mgr) {
        mInstance = new FontHelper(mgr);
    }

    public static FontHelper getmInstance() {
        return mInstance;
    }

    public Typeface getFontFromAsset(String asset) {
        if (TextUtils.isEmpty(asset)) {
            return null;
        }
        asset= "fonts/" + asset;
        if (mFonts.containsKey(asset))
            return mFonts.get(asset);

        Typeface font = null;

        try {
            font = Typeface.createFromAsset(mAssetManager, asset);
            mFonts.put(asset, font);
        } catch (Exception e) {
            Log.d("FontHelper", "Get font, font name should contain extension: " + asset);
        }

        if (font == null) {
            try {
                String fixedAsset = fixFontFileName(asset);
                font = Typeface.createFromAsset(mAssetManager, fixedAsset);
                mFonts.put(asset, font);
                mFonts.put(fixedAsset, font);
            } catch (Exception e) {
                Log.d("FontHelper", "Get font error, font isn't found: "+ asset);
            }
        }

        return font;
    }

    public Typeface getFontFromFileName(String filePath) {
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

        if (typeface == null) {
            try {
                String fixedAsset = fixFontFileName(filePath);
                typeface = Typeface.createFromFile(fixedAsset);
                mFonts.put(filePath, typeface);
                mFonts.put(fixedAsset, typeface);
            } catch (Exception e) {
                Log.d("FontHelper", "Get font error, font isn't found: " + filePath);
            }
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