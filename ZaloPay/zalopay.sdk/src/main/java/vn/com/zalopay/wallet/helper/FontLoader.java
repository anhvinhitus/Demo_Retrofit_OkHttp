package vn.com.zalopay.wallet.helper;

import android.graphics.Typeface;
import android.os.Build;
import android.util.ArrayMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.repository.ResourceManager;

/*
 * class for caching font.
 */
public class FontLoader extends SingletonBase {
    private static FontLoader _object;
    private Map<String, Typeface> fontMap;

    public FontLoader() {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            fontMap = new ArrayMap<>();
        } else {
            fontMap = new HashMap<>();
        }
    }

    public static FontLoader getInstance() {
        if (FontLoader._object == null) {
            FontLoader._object = new FontLoader();
        }
        return FontLoader._object;
    }

    public Typeface loadFont(String pFontName) {
        Typeface tf = fontMap.get(pFontName);
        if (tf != null) {
            return tf;
        }
        try {
            tf = Typeface.createFromFile(new File(ResourceManager.getFontFolder(), pFontName + ".ttf"));
            if (tf != null) {
                fontMap.put(pFontName, tf);
            }
        } catch (Exception e) {
            Timber.d(e, "Exception load font");
        }
        return tf;
    }
}
