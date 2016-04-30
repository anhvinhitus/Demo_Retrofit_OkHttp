package vn.com.vng.zalopay.account.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;


/**
 * Created by longlv on 22/04/2016.
 */
public class ShaUtils {

    public static void getSha() {
        Context ctx = AndroidApplication.instance();
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sig = Base64.encodeToString(md.digest(), Base64.DEFAULT).trim();
                if (sig.trim().length() > 0) {
                    Timber.tag("ShaUtils").d("key: %s",sig);
                }
            }
        } catch (PackageManager.NameNotFoundException e)
        {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
