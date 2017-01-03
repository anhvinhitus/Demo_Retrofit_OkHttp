package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by hieuvm on 1/3/17.
 */

public class KeyTools {

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;

    @Inject
    public KeyTools(Context context) {
        mKeyStore = providesKeyStore();
        mKeyGenerator = providesKeyGenerator();
    }

    private KeyStore providesKeyStore() {
        try {
            return KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            Timber.e(e, "Failed to get an instance of KeyStore");
            return null;
        }
    }

    @Nullable
    public KeyStore getKeyStore() {
        return mKeyStore;
    }

    @Nullable
    public KeyGenerator getKeyGenerator() {
        return mKeyGenerator;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    public Cipher getCipher() {
        try {
            return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Timber.e(e, "Failed to get an instance of Cipher");
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    private KeyGenerator providesKeyGenerator() {
        try {
            return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            Timber.e(e, "Failed to get an instance of KeyGenerator");
            return null;
        }
    }
}
