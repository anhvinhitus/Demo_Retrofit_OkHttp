package com.zalopay.ui.widget.iconfont;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zalopay.ui.widget.util.FileUtil;
import com.zalopay.ui.widget.util.FontHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by longlv on 1/6/17.
 * *
 */

public class IconFontHelper {
    private static IconFontHelper mInstance;

    public static IconFontHelper getInstance() {
        if (mInstance == null) {
            mInstance = new IconFontHelper();
        }
        return mInstance;
    }

    private AssetManager mAssetManager;
    private Typeface mCurrentTypeface;
    private Map<String, IconFontInfo> mCurrentIconFontInfoMap;
    private IconFontType mCurrentIconFontType;

    private IconFontHelper() {
    }

    public void initialize(AssetManager mgr, String fontPathInAsset, String jsonPathInAsset,
                     String fontPathInResource, String jsonPathInResource) {
        this.mAssetManager = mgr;
        initIconFont(fontPathInAsset, jsonPathInAsset,
                fontPathInResource, jsonPathInResource);
    }

    private void initIconFont(String fontPathInAsset, String jsonPathInAsset,
                              String fontPathInResource, String jsonPathInResource) {
        if (initIconFontInResourceApp1(fontPathInResource, jsonPathInResource)) {
            mCurrentIconFontType = IconFontType.RESOURCE_APP_1;
            Log.d("IconFontHelper", "load icon font from resource app 1 successfully.");
        } else if (initIconFontInAsset(fontPathInAsset, jsonPathInAsset)) {
            mCurrentIconFontType = IconFontType.ASSET;
            Log.d("IconFontHelper", "load icon font from asset successfully.");
        } else {
            Log.w("IconFontHelper", "load icon font fail.");
            mCurrentIconFontType = IconFontType.UNKNOWN;
        }
    }

    private void setCurrentIconFont(Typeface typeface, Map<String, IconFontInfo> iconFontInfoMap) {
        mCurrentTypeface = typeface;
        mCurrentIconFontInfoMap = iconFontInfoMap;
    }

    private boolean initIconFontInResourceApp1(String fontFilePath, String jsonFilePath) {
        Typeface typeface = FontHelper.getInstance().getFontFromFile(fontFilePath);
        Map<String, IconFontInfo> iconFontInfoMap = getMapIconFontInfoInFile(jsonFilePath);
        if (typeface != null && iconFontInfoMap != null && iconFontInfoMap.size() > 0) {
            setCurrentIconFont(typeface, iconFontInfoMap);
            return true;
        } else {
            return false;
        }
    }

    private boolean initIconFontInAsset(String fontFilePath, String jsonFilePath) {
        Typeface typeface = FontHelper.getInstance().getFontFromAsset(mAssetManager, fontFilePath);
        Map<String, IconFontInfo> iconFontInfoMap = getMapIconFontInfoInAsset(jsonFilePath);
        if (typeface != null && iconFontInfoMap != null && iconFontInfoMap.size() > 0) {
            setCurrentIconFont(typeface, iconFontInfoMap);
            return true;
        } else {
            return false;
        }
    }

    private Map<String, IconFontInfo> getMapIconFontInfoInAsset(String jsonFilePath) {
        try {
            String jsonString = FileUtil.readAssetToString(mAssetManager, jsonFilePath);
            return getMapIconFontInfo(jsonString);
        } catch (IOException e) {
            Log.d("IconFontHelper", "Read file in asset to string throw exception: " + e.getMessage());
        }
        return null;
    }

    private Map<String, IconFontInfo> getMapIconFontInfoInFile(String jsonFilePath) {
        String jsonString;
        try {
            jsonString = FileUtil.readFileToString(jsonFilePath);
            return getMapIconFontInfo(jsonString);
        } catch (IOException e) {
            Log.d("IconFontHelper", "Read file to string throw exception: " + e.getMessage());
        }
        return null;
    }

    private Map<String, IconFontInfo> getMapIconFontInfo(String jsonString) {
        Map<String, IconFontInfo> iconFontInfoMap = null;
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            iconFontInfoMap = new Gson().fromJson(jsonString,
                    new TypeToken<HashMap<String, IconFontInfo>>() {
                    }.getType());
        } catch (Exception e) {
            Log.e("IconFontHelper", "initIconFontInResourceApp1 throw exception: " + e.getMessage());
        }
        return iconFontInfoMap;
    }

    public IconFontInfo getIconFontInfo(String iconName) {
        if (TextUtils.isEmpty(iconName) || mCurrentIconFontInfoMap == null) {
            return null;
        }
        return mCurrentIconFontInfoMap.get(iconName);
    }

    public void setCurrentIconFontInfoMap(Map<String, IconFontInfo> currentIconFontInfoMap) {
        mCurrentIconFontInfoMap = currentIconFontInfoMap;
    }

    public Typeface getCurrentTypeface() {
        return mCurrentTypeface;
    }

    public IconFontType getCurrentIconFontType() {
        return mCurrentIconFontType;
    }
}
