package vn.com.zalopay.wallet.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import vn.com.zalopay.wallet.listener.ZPWKeyStoreDecryptListener;
import vn.com.zalopay.wallet.listener.ZPWKeyStoreDeleteKeyPairListener;
import vn.com.zalopay.wallet.listener.ZPWKeyStoreEncryptListener;

/**
 * Created by cpu11843-local on 12/9/16.
 */

public class KeyStoreUtil {
    private static KeyStoreUtil mKeyStoreUtil;
    private String CIPHER_TYPE = "RSA/ECB/PKCS1Padding";
    private String ALIAS_KEY = "YhEahsbviSfzwSCDXiE3kCLDkLL2GDha";
    private Charset UTF8CHARSET = Charset.forName("UTF-8");
    private String HMACMD5 = "HmacMD5";
    private String HMACSHA1 = "HmacSHA1";
    private String HMACSHA256 = "HmacSHA256";
    private String HMACSHA512 = "HmacSHA512";
    private KeyStore mKeyStore;

    private KeyStoreUtil() throws Exception {
        mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        mKeyStore.load(null);
    }

    /***
     * to getInstance()
     *
     * @return
     */
    public static KeyStoreUtil getInstance() throws Exception {
        if (mKeyStoreUtil == null) {
            mKeyStoreUtil = new KeyStoreUtil();
        }
        return mKeyStoreUtil;
    }

    /***
     * to generateKeyPair()
     *
     * @param pContext
     * @param pAlias
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public KeyStoreGenerate generateKeyPair(Context pContext, String pAlias) {
        try {
            // Create new key if needed
            if (!mKeyStore.containsAlias(encodeAlias(pAlias))) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(pContext)
                        .setAlias(encodeAlias(pAlias))
                        .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();

                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);
                generator.generateKeyPair();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new KeyStoreGenerate(pAlias);
    }

    /***
     * decrypt
     *
     * @param cipherText
     * @param pAlias
     * @param pListener
     */
    public void decrypt(String cipherText, String pAlias, ZPWKeyStoreDecryptListener pListener) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(encodeAlias(pAlias), null);

            Cipher output = Cipher.getInstance(CIPHER_TYPE);
            output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            // get plainText
            String plainText = new String(bytes, 0, bytes.length, "UTF-8");
            pListener.success(plainText);
        } catch (Exception e) {
            pListener.failure("Exception: " + e.getMessage());
        }
    }

    /***
     * to delete Keypair
     *
     * @param pAlias
     * @param pListener
     */
    public boolean deleteKeyPair(String pAlias, ZPWKeyStoreDeleteKeyPairListener pListener) {
        try {
            mKeyStore.deleteEntry(encodeAlias(pAlias));
            pListener.success();
            return true;
        } catch (Exception e) {
            pListener.failure("Exception: " + e.getMessage());
        }
        return false;
    }

    /***
     * get Aliases() list
     *
     * @return
     */
    public Enumeration<String> getAliases() {
        try {
            return mKeyStore.aliases();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * encode Alias
     *
     * @param pAlias
     * @return
     */
    private String encodeAlias(String pAlias) {
        return HMacBase64Encode(HMACMD5, ALIAS_KEY, pAlias);
    }

    /**
     * Calculating a message authentication code (MAC) involving a cryptographic
     * hash function in combination with a secret cryptographic key.
     * <p>
     * The result will be represented base64-encoded string.
     *
     * @param algorithm A cryptographic hash function (such as MD5 or SHA-1)
     * @param key       A secret cryptographic key
     * @param data      The message to be authenticated
     * @return Base64-encoded HMAC String
     */
    private String HMacBase64Encode(final String algorithm, final String key, final String data) {
        byte[] hmacEncodeBytes = HMacEncode(algorithm, key, data);
        if (hmacEncodeBytes == null) {
            return null;
        }
        return Base64.encodeToString(hmacEncodeBytes, Base64.DEFAULT);
    }

    /**
     * Calculating a message authentication code (MAC) involving a cryptographic
     * hash function in combination with a secret cryptographic key.
     *
     * @param algorithm A cryptographic hash function (such as MD5 or SHA-1)
     * @param key       A secret cryptographic key
     * @param data      The message to be authenticated
     * @return The cryptographic strength of the HMAC depends upon the
     * cryptographic strength of the underlying hash function, the size
     * of its hash output, and on the size and quality of the key.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private byte[] HMacEncode(final String algorithm, final String key, final String data) {
        Mac macGenerator = null;
        try {
            macGenerator = Mac.getInstance(algorithm);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(UTF8CHARSET), algorithm);
                macGenerator.init(signingKey);
            } else {
                SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"), algorithm);
                macGenerator.init(signingKey);
            }

        } catch (Exception ex) {
        }

        if (macGenerator == null) {
            return null;
        }

        byte[] dataByte = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            dataByte = data.getBytes(UTF8CHARSET);
        } else {
            try {
                dataByte = data.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        return macGenerator.doFinal(dataByte);
    }

    public class KeyStoreGenerate {
        private String mAlias;

        public KeyStoreGenerate(String pAlias) {
            mAlias = pAlias;
        }

        /***
         * encrypt
         *
         * @param plainText
         * @param pListener
         */
        public void encrypt(String plainText, ZPWKeyStoreEncryptListener pListener) {
            try {
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(encodeAlias(mAlias), null);
                RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

                if (plainText.isEmpty()) {
                    pListener.failure("Empty: plaintext");
                    return;
                }

                Cipher inCipher = Cipher.getInstance(CIPHER_TYPE);
                inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CipherOutputStream cipherOutputStream = new CipherOutputStream(
                        outputStream, inCipher);
                cipherOutputStream.write(plainText.getBytes("UTF-8"));
                cipherOutputStream.close();

                //  get cipherText
                byte[] vals = outputStream.toByteArray();
                pListener.success(Base64.encodeToString(vals, Base64.DEFAULT));
            } catch (Exception e) {
                pListener.failure("Exception: " + e.getMessage());
            }
        }
    }

}
