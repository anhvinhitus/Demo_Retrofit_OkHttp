package com.zalopay.ui.widget.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zalopay.ui.widget.iconfont.IconFontInfo;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by hieuvm on 7/3/17.
 * *
 */

public class FontLoader {

    private static final String FONT_DEFAULT_NAME = "zalopay.ttf";
    private static final String FONT_CODE_DEFAULT_NAME = "zalopay.json";
    private static final String FONTS_ASSET_PATH = "fonts/";
    private static final String FILE_EXTENSIONS = ".ttf";
    private static boolean _initialize = false;

    private static final Map<String, Typeface> sFontMap;
    private static Map<String, IconFontInfo> sFontCode;
    private static final Gson gson;

    static {
        sFontMap = new HashMap<>();
        sFontCode = new HashMap<>();
        gson = new Gson();
    }

    public static void initialize(Context context) {
        if (_initialize) {
            return;
        }
        _initialize = true;
        try {
            loadFontFromAsset(context.getAssets(), FONT_DEFAULT_NAME, FONT_CODE_DEFAULT_NAME);
        } catch (Exception e) {
            Timber.e("Load icon font from assets error: %s", e.toString());
        }

    }

    private static boolean loadFontFromAsset(AssetManager assetManager, String fontName, String codePath) throws Exception {
        Typeface typeface = Typeface.createFromAsset(assetManager, FONTS_ASSET_PATH + fontName);
        sFontMap.put(fontName, typeface);
        Type typeOfT = new TypeToken<HashMap<String, IconFontInfo>>() {
        }.getType();
        sFontCode = gson.fromJson(FileUtil.readAssetToString(assetManager, FONTS_ASSET_PATH + codePath), typeOfT);
        Timber.i("load font from asset success");
        return true;
    }

    private static boolean loadFont(File fontFile, File codeFile) throws Exception {
        if (!(fontFile.exists() && codeFile.exists())) {
            Timber.i("load font from app1 = %s", fontFile.exists());
            return false;
        }
        String fontName = fontFile.getName();
        Typeface typeface = Typeface.createFromFile(fontFile);
        sFontMap.put(fontName, typeface);
        Type typeOfT = new TypeToken<HashMap<String, IconFontInfo>>() {
        }.getType();
        sFontCode = gson.fromJson(FileUtil.readFileToString(codeFile.getAbsolutePath()), typeOfT);
        Timber.i("load font from app1 success");
        return true;
    }

    public static boolean loadFont(String fontPath, String codePath) {
        try {
            Timber.i("load font from [%s]", fontPath);
            return loadFont(new File(fontPath), new File(codePath));
        } catch (Exception e) {
            Timber.e("Load icon font from file error: [fontPath: %s Error: %s]", fontPath, e.toString());
        }
        return false;
    }

    public static boolean loadFontFromAsset(Context context) {
        try {
            return loadFontFromAsset(context.getAssets(), FONT_DEFAULT_NAME, FONT_CODE_DEFAULT_NAME);
        } catch (Exception e) {
            Timber.e("Load icon font from assets error: %s", e.toString());
        }
        return false;
    }


    public static String getCode(@NonNull String iconName) {
        return getCode(iconName, "");
    }

    public static String getCode(@NonNull String iconName, @NonNull String defaultCode) {
        if (sFontCode.containsKey(iconName)) {
            return sFontCode.get(iconName).code;
        }

        if (sFontCode.containsKey(defaultCode)) {
            return sFontCode.get(defaultCode).code;
        }

        Timber.e("icon font not found [name:%s - sFontCode:%s]", iconName, sFontCode.size());
        return "";
    }

    public static Typeface getDefaultTypeface() {
        Typeface typeface = sFontMap.get(FONT_DEFAULT_NAME);
        if (typeface == null) {
            Timber.e("Default Typeface is null [sFontMap: %s]", sFontMap.size());
        }
        return typeface;
    }

    public static Typeface getFont(AssetManager assetManager, String fontName) {
        Typeface typeface = sFontMap.get(fontName);
        if (typeface == null) {
            String fileName = FONTS_ASSET_PATH + fontName + FILE_EXTENSIONS;
            typeface = Typeface.createFromAsset(assetManager, fileName);
            sFontMap.put(fontName, typeface);
        }
        return typeface;
    }
}
