package vn.com.vng.zalopay.authentication.secret;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.Utils;

/**
 * Created by hieuvm on 5/7/17.
 * *
 */

@RequiresApi(api = Build.VERSION_CODES.M)
final class MarshmallowKeytool implements KeytoolInternal {

    private final KeyStore mKeyStore;

    private final UserConfig mUserConfig;

    MarshmallowKeytool() {
        this.mKeyStore = providesKeyStore();
        this.mUserConfig = AndroidApplication.instance().getAppComponent().userConfig();
    }

    private KeyStore providesKeyStore() {
        try {
            return KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            Timber.d(e, "Failed to get an instance of KeyStore");
            return null;
        }
    }

    private SecretKey getSecretKey() {
        try {
            mKeyStore.load(null);

            if (!mKeyStore.containsAlias(Constants.KEY_ALIAS_NAME)) {
                return createKey();
            }

            return (SecretKey) mKeyStore.getKey(Constants.KEY_ALIAS_NAME, null);

        } catch (Exception e) {
            Timber.d(e, "get secret key fail ");
        }
        return null;
    }

    private SecretKey createKey() {
        Timber.d("create secret key");
        try {
            KeyGenParameterSpec aesSpec = new KeyGenParameterSpec.Builder(Constants.KEY_ALIAS_NAME, KeyProperties.PURPOSE_ENCRYPT
                    | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    //.setKeySize(128)
                    .build();

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, mKeyStore.getType());
            keyGenerator.init(aesSpec);
            return keyGenerator.generateKey();

        } catch (Exception e) {
            Timber.d(e, "create secret key fail");
        }
        return null;
    }

    @Override
    public Cipher getCipher(int mode) {

        Cipher cipher;

        try {
            mKeyStore.load(null);
            byte[] iv;
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            IvParameterSpec ivParams;
            if (mode == Cipher.ENCRYPT_MODE) {
                cipher.init(mode, getSecretKey());
            } else {
                SecretKey secretKey = ((KeyStore.SecretKeyEntry) mKeyStore.getEntry(Constants.KEY_ALIAS_NAME, null)).getSecretKey();
                String keyPasswordIv = mUserConfig.getEncryptedPasswordIV();
                Timber.d("iv : [%s] ", keyPasswordIv);
                iv = Base64.decode(keyPasswordIv, Base64.DEFAULT);
                ivParams = new IvParameterSpec(iv);
                cipher.init(mode, secretKey, ivParams);
            }
            return cipher;
        } catch (Exception e) {
            Timber.d(e, "Get cipher error");
        }
        return null;
    }

    public String decrypt(Cipher cipher) {
        Timber.d("decrypt : [%s]", cipher);

        if (cipher == null) {
            cipher = getCipher(Cipher.DECRYPT_MODE);
        }

        if (cipher == null) {
            return null;
        }

        try {
            String keyPassword = mUserConfig.getEncryptedPassword();
            Timber.d("secret base64: [%s] ", keyPassword);
            byte[] encodedData = Base64.decode(keyPassword, Base64.DEFAULT);
            byte[] decodedData = cipher.doFinal(encodedData);
            String result = new String(decodedData);
            Timber.d("decrypt: %s", result);
            return result;
        } catch (Exception e) {
            Timber.d(e, "Failed to decrypt the data with the generated key.");
        }

        return null;
    }

    public boolean encrypt(String secret) {
        return encrypt(secret, true);
    }

    private boolean encrypt(String secret, boolean isSha256) {
        try {
            if (!isSha256) {
                secret = Utils.sha256Base(secret);
            }

            Timber.d("Password sha256 : [%s]", secret);

            Cipher encryptCipher = getCipher(Cipher.ENCRYPT_MODE);

            if (encryptCipher == null) {
                return false;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, encryptCipher);
            cipherOutputStream.write(secret.getBytes());
            cipherOutputStream.flush();
            cipherOutputStream.close();

            IvParameterSpec ivParams = new IvParameterSpec(encryptCipher.getIV());
            String iv = Base64.encodeToString(ivParams.getIV(), Base64.DEFAULT);

            byte[] encrypted = outputStream.toByteArray();
            String secretBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT);

            mUserConfig.setEncryptedPassword(secretBase64, iv);

            Timber.d("secret base64 : [%s] secret [%s]", secretBase64, secret);
            Timber.d("iv : [%s]", iv);
            return true;
        } catch (Exception ex) {
            Timber.d(ex, "Failed to encrypt the data with the generated key.");
        }
        return false;
    }

}
