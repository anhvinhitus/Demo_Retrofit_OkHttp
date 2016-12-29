package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

/**
 * Created by hieuvm on 12/26/16.
 */
@Module
public class FingerprintModule {

    @TargetApi(Build.VERSION_CODES.M)
    @Provides
    public FingerprintManager providesFingerprintManager(Context context) {
        return context.getSystemService(FingerprintManager.class);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Provides
    public KeyguardManager providesKeyguardManager(Context context) {
        return context.getSystemService(KeyguardManager.class);
    }

    @Provides
    public KeyStore providesKeystore() {
        try {
            return KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            Timber.e(e, "Failed to get an instance of KeyStore");
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Provides
    public KeyGenerator providesKeyGenerator() {
        try {
            return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            Timber.e(e, "Failed to get an instance of KeyGenerator");
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Provides
    public Cipher providesCipher(KeyStore keyStore) {
        try {
            return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Timber.e(e, "Failed to get an instance of Cipher");
            return null;
        }
    }
}
