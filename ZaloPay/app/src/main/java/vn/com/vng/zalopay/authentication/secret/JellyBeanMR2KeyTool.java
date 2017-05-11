package vn.com.vng.zalopay.authentication.secret;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.ConvertHelper;

/**
 * Created by hieuvm on 5/7/17.
 * *
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
final class JellyBeanMR2KeyTool implements KeytoolInternal {

    //  static final String CIPHER_PROVIDER = "AndroidOpenSSL";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    private static final String AES_MODE = "AES/ECB/PKCS7Padding";
    private static final String ENCRYPTED_KEY = "encrypted_key";

    private final KeyStore mKeyStore;
    private final Context mContext;
    private final SharedPreferences mPreferences;
    private final UserConfig mUserConfig;

    JellyBeanMR2KeyTool() {
        this.mContext = AndroidApplication.instance();
        this.mKeyStore = providesKeyStore();
        this.mPreferences = AndroidApplication.instance().getAppComponent().sharedPreferences();
        this.mUserConfig = AndroidApplication.instance().getAppComponent().userConfig();
    }

    private static KeyStore providesKeyStore() {
        try {
            return KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            Timber.w(e, "Failed to get an instance of KeyStore");
            return null;
        }
    }

    private void generateKeyPair() {
        try {
            mKeyStore.load(null);
            if (!mKeyStore.containsAlias(Constants.KEY_ALIAS_NAME)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 30);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mContext)
                        .setAlias(vn.com.vng.zalopay.Constants.KEY_ALIAS_NAME)
                        .setSubject(new X500Principal("CN=" + Constants.KEY_ALIAS_NAME))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", mKeyStore.getType());
                kpg.initialize(spec);
                kpg.generateKeyPair();

                KeyPair keyPair = kpg.generateKeyPair();
                Timber.d("Public Key is: %s", keyPair.getPublic().toString());
            }
        } catch (Exception e) {
            Timber.w(e, "Generate KeyPair error");
        }
    }

    private void generateAESKey() throws Exception {
        String enryptedKeyB64 = mPreferences.getString(ENCRYPTED_KEY, null);
        if (enryptedKeyB64 == null) {
            byte[] key = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(key);
            byte[] encryptedKey = rsaEncrypt(key);
            enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            SharedPreferences.Editor edit = mPreferences.edit();
            edit.putString(ENCRYPTED_KEY, enryptedKeyB64);
            edit.apply();
        }
    }

    private SecretKeySpec getSecretKey() throws Exception {
        generateKeyPair();
        generateAESKey();

        String enryptedKeyB64 = mPreferences.getString(ENCRYPTED_KEY, null);
        byte[] encryptedKey = Base64.decode(enryptedKeyB64, Base64.DEFAULT);
        byte[] key = rsaDecrypt(encryptedKey);
        return new SecretKeySpec(key, "AES");
    }

    private String encrypt(byte[] input) throws Exception {
        Cipher c = getCipherObject(Cipher.ENCRYPT_MODE);
        if (c != null) {
            byte[] encodedBytes = c.doFinal(input);
            String secretBase64 = Base64.encodeToString(encodedBytes, Base64.DEFAULT);
            mUserConfig.setEncryptedPassword(secretBase64, "");
        }
        return null;
    }

    private byte[] decrypt(byte[] encrypted) throws Exception {
        Cipher c = getCipherObject(Cipher.DECRYPT_MODE);
        if (c != null) {
            return c.doFinal(encrypted);
        }

        return null;
    }

    private byte[] rsaEncrypt(byte[] secret) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(Constants.KEY_ALIAS_NAME, null);
        Cipher inputCipher = Cipher.getInstance(RSA_MODE);
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();

        return outputStream.toByteArray();
    }

    private byte[] rsaDecrypt(byte[] encrypted) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(Constants.KEY_ALIAS_NAME, null);
        Cipher output = Cipher.getInstance(RSA_MODE);
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(encrypted), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            byte defaultValue = 0;
            bytes[i] = ConvertHelper.unboxValue(values.get(i), defaultValue);
        }
        return bytes;
    }

    @Override
    public Cipher getCipher(int mode) {
        return null;
    }

    @Nullable
    private Cipher getCipherObject(int mode) {

        try {
            Cipher c = Cipher.getInstance(AES_MODE, "BC");
            if (mode == Cipher.ENCRYPT_MODE) {
                c.init(Cipher.ENCRYPT_MODE, getSecretKey());
            } else {
                c.init(Cipher.DECRYPT_MODE, getSecretKey());
            }
            return c;
        } catch (Exception e) {
            Timber.w(e, "Get cipher error");
            return null;
        }
    }

    @Override
    public String decrypt(Cipher cipher) {

        try {

            String keyPassword = mUserConfig.getEncryptedPassword();
            Timber.d("secret base64: [%s] ", keyPassword);

            byte[] decodedData = decrypt(Base64.decode(keyPassword, Base64.DEFAULT));

            if (decodedData == null) {
                return null;
            }

            String result = new String(decodedData);
            Timber.d("decrypt: %s", result);
            return result;
        } catch (Exception e) {
            Timber.w(e, "decrypt error");
        }

        return null;
    }

    @Override
    public boolean encrypt(String hashPassword) {
        try {
            encrypt(hashPassword.getBytes());
            return true;
        } catch (Exception e) {
            Timber.w(e, "encrypt error");
            return false;
        }
    }

}

