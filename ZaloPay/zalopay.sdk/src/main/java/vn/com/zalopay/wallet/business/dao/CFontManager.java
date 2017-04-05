package vn.com.zalopay.wallet.business.dao;

import android.graphics.Typeface;
import android.os.Build;
import android.util.ArrayMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;

/***
 * class for caching font.
 */
public class CFontManager extends SingletonBase {
    private static CFontManager _object;
    private Map<String, Typeface> fontMap;

    public CFontManager() {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            fontMap = new ArrayMap<>();
        } else {
            fontMap = new HashMap<>();
        }
    }

    public synchronized static CFontManager getInstance() {
        if (CFontManager._object == null) {
            CFontManager._object = new CFontManager();
        }
        return CFontManager._object;
    }

    /***
     * @param pFontName
     * @return
     */

    public Typeface loadFont(String pFontName) {
        Typeface tf = fontMap.get(pFontName);
        if (tf == null) {
            try {
                tf = Typeface.createFromFile(new File(ResourceManager.getPathFont(), pFontName + ".ttf"));
                if (tf != null) {
                    fontMap.put(pFontName, tf);
                }
            } catch (Exception e) {
                Log.d(this, e);
            }
        }
        return tf;
    }
}
