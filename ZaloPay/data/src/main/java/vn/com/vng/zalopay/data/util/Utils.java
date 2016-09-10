package vn.com.vng.zalopay.data.util;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

/**
 * Created by AnhHieu on 8/26/16.
 * SHA Utils
 */
public class Utils {

    public static String sha256Base(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            Timber.e(ex, "sha256Base");
            return "";
        }
    }


    /*
    * join with char |
    * */
    public static String sha256(String... params) {
        String content = Strings.joinWithDelimiter("|", params);

        Timber.d("pre-sha256:  %s", content);

        return sha256Base(content);
    }

    private static long crypt(long value, int operation) {
        // Configuration
        byte[] key	= String.format("%s", String.valueOf(16091112L), String.valueOf(53996027L)).getBytes();
        String IV  	= String.valueOf(16091112L);

        // Create new Blowfish cipher
        SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
        try {
            Cipher cipher = Cipher.getInstance("Blowfish/CBC/NoPadding");

            if (operation == Cipher.ENCRYPT_MODE) {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new javax.crypto.spec.IvParameterSpec(IV.getBytes()));
                byte[] encoding = cipher.doFinal(longToBytes(value));
                return bytesToLong(encoding);
            } else if (operation == Cipher.DECRYPT_MODE) {
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new javax.crypto.spec.IvParameterSpec(IV.getBytes()));
                byte[] decoding = cipher.doFinal(longToBytes(value));
                return bytesToLong(decoding);
            }
        } catch (NoSuchAlgorithmException e) {
            Timber.e("No such algorithm for encrypting/decrypting value");
            return -2;
        } catch (NoSuchPaddingException e) {
            Timber.e("No such padding for encrypting/decrypting value");
            return -3;
        } catch (InvalidAlgorithmParameterException e) {
            Timber.e("Invalid algorithm parameter for encrypting/decrypting value");
            return -4;
        } catch (InvalidKeyException e) {
            Timber.e("Invalid key for encrypting/decrypting value");
            return -5;
        } catch (IllegalBlockSizeException e) {
            Timber.e("Invalid block size for encrypting/decrypting value");
            return -6;
        } catch (BadPaddingException e) {
            Timber.e("Bad padding for encrypting/decrypting value");
            return -7;
//        } catch (NoSuchProviderException e) {
//            Timber.e("No such provider for encrypting/decrypting value");
//            return -8;
        }
        return -1;
    }

    public static long encryptLong(long value) {
        return crypt(value, Cipher.ENCRYPT_MODE);
    }

    public static long decryptLong(long value) {
        return crypt(value, Cipher.DECRYPT_MODE);
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/Byte.SIZE);
        buffer.putLong(x);
        return buffer.array();
    }

    private static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/Byte.SIZE);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }
}
