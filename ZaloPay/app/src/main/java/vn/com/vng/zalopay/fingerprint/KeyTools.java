package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.util.Utils;

/**
 * Created by hieuvm on 1/3/17.
 */

public class KeyTools {

    private KeyStore mKeyStore;

    private SharedPreferences mPreferences;

    private Cipher mEncryptCipher;

    private Cipher mDecryptCipher;

    private Context mContext;

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    @Inject
    public KeyTools(Context context, SharedPreferences preferences) {
        this.mKeyStore = providesKeyStore();
        this.mPreferences = preferences;
        this.mContext = context;
    }

    private KeyStore providesKeyStore() {
        try {
            return KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (KeyStoreException e) {
            Timber.e(e, "Failed to get an instance of KeyStore");
            return null;
        }
    }

    @Nullable
    private KeyStore getKeyStore() {
        return mKeyStore;
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
            if (key != null) {
                return key;
            }
            return createKey();

        } catch (Exception e) {
            Timber.e(e, "get secret key fail ");
        }
        return null;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private SecretKey createKey() {
        Timber.d("create secret key");

        try {

            KeyGenParameterSpec aesSpec = new KeyGenParameterSpec.Builder(Constants.KEY_ALIAS_NAME, KeyProperties.PURPOSE_ENCRYPT
                    | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    //.setKeySize(128)
                    .build();

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(aesSpec);
            keyGenerator.generateKey();

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
                SecretKey secretKey = ((KeyStore.SecretKeyEntry) mKeyStore.getEntry(Constants.KEY_ALIAS_NAME, null)).getSecretKey();
                String keyPasswordIv = mPreferences.getString(Constants.PREF_KEY_PASSWORD_IV, "");
                Timber.d("iv : [%s] ", keyPasswordIv);
                iv = Base64.decode(keyPasswordIv, Base64.DEFAULT);
                ivParams = new IvParameterSpec(iv);
                cipher.init(mode, secretKey, ivParams);
            }
            return cipher;
        } catch (Exception e) {
            Timber.d("Get cipher error", e);
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
        Timber.d("decrypt cipher: [%s]", mDecryptCipher);
        return (mDecryptCipher != null);
    }

   /* public boolean encrypt2(String secret) {
        Timber.d("encrypt [%s]", secret);
        try {

            byte[] encrypted = mEncryptCipher.doFinal(secret.getBytes());

            IvParameterSpec ivParams = mEncryptCipher.getParameters().getParameterSpec(IvParameterSpec.class);
            String iv = Base64.encodeToString(ivParams.getIV(), Base64.DEFAULT);

            SharedPreferences.Editor editor = mPreferences.edit();
            Timber.d("encrypt %s", new String(encrypted));
            String secretBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
            Timber.d("secret base64 : [%s]", secretBase64);
            Timber.d("iv : [%s]", secretBase64);
            editor.putString(Constants.PREF_KEY_PASSWORD, secretBase64);
            editor.putString(Constants.PREF_KEY_PASSWORD_IV, iv);
            editor.apply();
            return true;

        } catch (Exception e) {
            Timber.e(e, "Failed to encrypt the data with the generated key.");
        }

        return false;
    }*/

    public String decrypt(Cipher cipher) {
        Timber.d("decrypt : [%s]", cipher);
        try {
            String keyPassword = mPreferences.getString(Constants.PREF_KEY_PASSWORD, "");
            Timber.d("secret base64: [%s] ", keyPassword);
            byte[] encodedData = Base64.decode(keyPassword, Base64.DEFAULT);
            byte[] decodedData = cipher.doFinal(encodedData);
            String result = new String(decodedData);
            Timber.d("decrypt: %s", result);
            return result;

            //   Timber.d("encrypt: %s", passwordSha256.getBytes().length);
            // Timber.d("encrypt: %s %s", outputStream.toByteArray(), new String(outputStream.toByteArray()));
          /*  String keyPassword = mPreferences.getString(Constants.PREF_KEY_PASSWORD, "");
            Timber.d("secret base64 : %s", keyPassword);
            byte[] decodeData = Base64.decode(keyPassword, Base64.DEFAULT);
            byte[] in = new byte[decodeData.length];
            Timber.d("decrypt: %s", decodeData.length);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decodeData);
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, mDecryptCipher);
            IOUtils.readFully(cipherInputStream, in);
            String muchWow = new String(in);
            Timber.d("encrypt: muchWow %s", muchWow);
            cipherInputStream.close();
            return muchWow;*/
        } catch (Exception e) {
            Timber.e(e, "Failed to decrypt the data with the generated key.");
        }
        return null;
    }


    @TargetApi(Build.VERSION_CODES.M)
    public boolean encrypt(String secret) {
        return encrypt(secret, false);
    }

    private boolean encrypt(String secret, boolean isSha256) {
        if (!FingerprintProvider.checkAndroidMVersion()) {
            return false;
        }

        try {
            if (!isSha256) {
                secret = Utils.sha256Base(secret);
            }

            Timber.d("Password sha256 : [%s]", secret);
            if (!initEncryptCipher()) {
                return false;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, mEncryptCipher);
            cipherOutputStream.write(secret.getBytes());
            cipherOutputStream.flush();
            cipherOutputStream.close();

            IvParameterSpec ivParams = new IvParameterSpec(mEncryptCipher.getIV());
            String iv = Base64.encodeToString(ivParams.getIV(), Base64.DEFAULT);

            byte[] encrypted = outputStream.toByteArray();
            SharedPreferences.Editor editor = mPreferences.edit();
            String secretBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
            editor.putString(Constants.PREF_KEY_PASSWORD, secretBase64);
            editor.putString(Constants.PREF_KEY_PASSWORD_IV, iv);
            editor.apply();

            Timber.d("secret base64 : [%s]", secretBase64);
            Timber.d("iv : [%s]", iv);
            return true;
        } catch (Exception ex) {
            Timber.e(ex, "Failed to encrypt the data with the generated key.");
        }
        return false;
    }

    Cipher getDecryptCipher() {
        return mDecryptCipher;
    }

    public void updatePassword(String password) {
        encrypt(password, true);
    }
}
