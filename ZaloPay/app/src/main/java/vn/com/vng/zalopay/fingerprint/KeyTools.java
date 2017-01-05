package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;

/**
 * Created by hieuvm on 1/3/17.
 */

public class KeyTools {

    private KeyStore mKeyStore;

    private KeyGenerator mKeyGenerator;

    private SharedPreferences mPreferences;

    private Cipher mEncryptCipher;

    private Cipher mDecryptCipher;

    private Context mContext;

    @Inject
    public KeyTools(Context context, SharedPreferences preferences) {
        this.mKeyStore = providesKeyStore();
        this.mKeyGenerator = providesKeyGenerator();
        this.mPreferences = preferences;
        this.mContext = context;
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
    private KeyStore getKeyStore() {
        return mKeyStore;
    }

    @Nullable
    private KeyGenerator getKeyGenerator() {
        return mKeyGenerator;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    private KeyGenerator providesKeyGenerator() {
        if (!FingerprintProvider.checkAndroidMVersion()) {
            return null;
        }

        try {
            return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            Timber.e(e, "Failed to get an instance of KeyGenerator");
            return null;
        }
    }


    private SecretKey getKey() {

        KeyStore mKeyStore = getKeyStore();
        if (mKeyStore == null) {
            Timber.d(new NullPointerException(), "KeyStore is NULL");
            return null;
        }

        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(Constants.KEY_ALIAS_NAME, null);
            if (key != null) return key;
            return createKey();

        } catch (Exception e) {
            Timber.e(e, "get secret key fail ");
        }
        return null;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private SecretKey createKey() {
        Timber.d("create secret key");
        KeyGenerator mKeyGenerator = getKeyGenerator();
        if (mKeyGenerator == null) {
            Timber.d(new NullPointerException(), "KeyGenerator is NULL");
            return null;
        }

        try {
            mKeyGenerator.init(new KeyGenParameterSpec.Builder(Constants.KEY_ALIAS_NAME,
                    KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            return mKeyGenerator.generateKey();

        } catch (Exception e) {
            Timber.e(e, "create secret key fail");
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Cipher getCipher(int mode) {

        KeyStore mKeyStore = getKeyStore();
        if (mKeyStore == null) {
            Timber.d(new NullPointerException(), "KeyStore is NULL");
            return null;
        }

        Cipher cipher;

        try {
            mKeyStore.load(null);
            byte[] iv;
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            IvParameterSpec ivParams;
            if (mode == Cipher.ENCRYPT_MODE) {
                cipher.init(mode, getKey());

            } else {
                SecretKey key = (SecretKey) mKeyStore.getKey(Constants.KEY_ALIAS_NAME, null);
                iv = Base64.decode(mPreferences.getString(Constants.PREF_KEY_PASSWORD_IV, ""), Base64.DEFAULT);
                ivParams = new IvParameterSpec(iv);
                cipher.init(mode, key, ivParams);
            }
            return cipher;
        } catch (Exception e) {
            Timber.e(e, "get cipher error");
        }
        return null;
    }


    public boolean initEncryptCipher() {
        Timber.d("initEncryptCipher %s", mEncryptCipher);
        if (!FingerprintProvider.checkAndroidMVersion()) {
            return false;
        }

      /*  if (mEncryptCipher != null) {
            return true;
        }*/

        mEncryptCipher = getCipher(Cipher.ENCRYPT_MODE);
        if (mEncryptCipher == null) {
            createKey();
            mEncryptCipher = getCipher(Cipher.ENCRYPT_MODE);
        }
        Timber.d("encrypt cipher: %s", mEncryptCipher);
        return (mEncryptCipher != null);

    }

    public boolean initDecryptCipher() {
        Timber.d("initDecryptCipher");
        if (!FingerprintProvider.checkAndroidMVersion()) {
            return false;
        }

       /* if (mDecryptCipher != null) {
            return true;
        }*/

        mDecryptCipher = getCipher(Cipher.DECRYPT_MODE);
        Timber.d("decrypt cipher: %s", mDecryptCipher);
        return (mDecryptCipher != null);
    }


    public boolean encrypt(String secret) {
        Timber.d("encrypt [%s]", secret);
        try {

            byte[] encrypted = mEncryptCipher.doFinal(secret.getBytes());

            IvParameterSpec ivParams = mEncryptCipher.getParameters().getParameterSpec(IvParameterSpec.class);
            String iv = Base64.encodeToString(ivParams.getIV(), Base64.DEFAULT);

            SharedPreferences.Editor editor = mPreferences.edit();
            String secretBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
            Timber.d("secret base64 : [%s]", secretBase64);
            editor.putString(Constants.PREF_KEY_PASSWORD, secretBase64);
            editor.putString(Constants.PREF_KEY_PASSWORD_IV, iv);
            editor.apply();
            return true;

        } catch (Exception e) {
            Timber.e(e, "Failed to encrypt the data with the generated key.");
        }

        return false;
    }


    public String decrypt(Cipher cipher) {
        try {
            byte[] encodedData = Base64.decode(mPreferences.getString(Constants.PREF_KEY_PASSWORD, ""), Base64.DEFAULT);
            byte[] decodedData = cipher.doFinal(encodedData);
            return new String(decodedData);
        } catch (Exception e) {
            Timber.e(e, "Failed to decrypt the data with the generated key.");
        }
        return null;
    }

    public Cipher getDecryptCipher() {
        return mDecryptCipher;
    }

    public Cipher getEncryptCipher() {
        return mEncryptCipher;
    }
}
