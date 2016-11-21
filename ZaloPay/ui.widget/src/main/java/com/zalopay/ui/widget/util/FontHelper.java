package com.zalopay.ui.widget.util;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by longlv on 11/17/16.
 * Cache mFonts that app is using.
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

    public Typeface getFont(String asset) {
        asset= "fonts/" + asset;
        if (mFonts.containsKey(asset))
            return mFonts.get(asset);

        Typeface font = null;

        try {
            font = Typeface.createFromAsset(mAssetManager, asset);
            mFonts.put(asset, font);
        } catch (Exception e) {
            Log.d("FontHelper", "Get font, font name should contain extension.");
        }

        if (font == null) {
            try {
                String fixedAsset = fixAssetFilename(asset);
                font = Typeface.createFromAsset(mAssetManager, fixedAsset);
                mFonts.put(asset, font);
                mFonts.put(fixedAsset, font);
            } catch (Exception e) {
                Log.d("FontHelper", "Get font error, font isn't found.");
            }
        }

        return font;
    }

    private String fixAssetFilename(String asset) {
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