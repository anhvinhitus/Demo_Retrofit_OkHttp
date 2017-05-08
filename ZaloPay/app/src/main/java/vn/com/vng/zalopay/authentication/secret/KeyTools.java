package vn.com.vng.zalopay.authentication.secret;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;

import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.crypto.Cipher;
import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;

/**
 * Created by hieuvm on 1/3/17.
 * *
 */

public class KeyTools {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";// KeyStore.getDefaultType();

    private final IKeytool mKeytool;
    private final UserConfig mUserConfig;
    
    @Inject
    public KeyTools(UserConfig userConfig) {
        mKeytool = new MarshmallowKeytool(providesKeyStore());
        mUserConfig = userConfig;
    }

    private KeyStore providesKeyStore() {
        try {
            return KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (KeyStoreException e) {
            Timber.w(e, "Failed to get an instance of KeyStore");
            return null;
        }
    }

    public boolean storePassword(String hashPassword) {
        String oldPassword = mUserConfig.getEncryptedPassword();
        if (TextUtils.isEmpty(hashPassword) || hashPassword.equals(oldPassword)) {
            return true;
        }

        return mKeytool.encrypt(hashPassword);
    }

    public String decrypt(Cipher cipher) {
        return mKeytool.decrypt(cipher);
    }

    public Cipher getDecryptCipher() {
        return mKeytool.getCipher(Cipher.DECRYPT_MODE);
    }

    public boolean isHavePassword() {
        String password = mUserConfig.getEncryptedPassword();
        return !TextUtils.isEmpty(password);
    }
}
