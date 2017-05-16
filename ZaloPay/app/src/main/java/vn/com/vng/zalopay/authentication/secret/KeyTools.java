package vn.com.vng.zalopay.authentication.secret;

import android.os.Build;
import android.text.TextUtils;

import javax.crypto.Cipher;

import rx.Scheduler;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by hieuvm on 1/3/17.
 * *
 */

public class KeyTools {

    private static final KeytoolInternal IMPL;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            IMPL = new MarshmallowKeytool();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            IMPL = new JellyBeanMR2KeyTool();
        } else {
            IMPL = null;
        }
    }

    private final UserConfig mUserConfig;

    public KeyTools() {
        mUserConfig = AndroidApplication.instance().getAppComponent().userConfig();
    }

    public boolean storePassword(String hashPassword) {
        String oldPassword = mUserConfig.getEncryptedPassword();
        return TextUtils.isEmpty(hashPassword) || hashPassword.equals(oldPassword) || encrypt(hashPassword);
    }

    private boolean encrypt(String hashPassword) {
        if (IMPL == null) {
            return false;
        }
        
        ObservableHelper.makeObservable(() -> IMPL.encrypt(hashPassword))
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        return true;
    }

    public String decrypt(Cipher cipher) {
        return IMPL == null ? "" : IMPL.decrypt(cipher);
    }

    public Cipher getDecryptCipher() {
        return IMPL == null ? null : IMPL.getCipher(Cipher.DECRYPT_MODE);
    }

    public boolean isHavePassword() {
        String password = mUserConfig.getEncryptedPassword();
        return !TextUtils.isEmpty(password);
    }
}