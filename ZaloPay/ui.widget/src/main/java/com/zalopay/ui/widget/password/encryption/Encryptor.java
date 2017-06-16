package com.zalopay.ui.widget.password.encryption;

import android.text.TextUtils;

import com.zalopay.ui.widget.password.enums.Algorithm;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * SHA1
 * of the 4-digit password.
 */
public class Encryptor {

    /**
     * Convert a chain of bytes into a {@link String}
     *
     * @param bytes The chain of bytes
     * @return The converted String
     */
    private static String bytes2Hex(byte[] bytes) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < bytes.length; n++) {
            stmp = (Integer.toHexString(bytes[n] & 0XFF));
            if (stmp.length() == 1) {
                hs += "0" + stmp;
            } else {
                hs += stmp;
            }
        }
        return hs.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Allows to get the SHA of a {@link String} using {@link MessageDigest}
     * if device does not support sha-256, fall back to sha-1 instead
     */
    public static String getSHA(String text, Algorithm algorithm) {
        String sha = "";
        if (TextUtils.isEmpty(text)) {
            return sha;
        }

        MessageDigest shaDigest = getShaDigest(algorithm);

        if (shaDigest != null) {
            byte[] textBytes = text.getBytes();
            shaDigest.update(textBytes, 0, text.length());
            byte[] shahash = shaDigest.digest();
            return bytes2Hex(shahash);
        }

        return null;
    }

    /**

     * @param algorithm The {@link Algorithm} to use
     */
    private static MessageDigest getShaDigest(Algorithm algorithm) {
        switch (algorithm) {
            case SHA256:
                try {
                    return MessageDigest.getInstance("SHA-256");
                } catch (Exception e) {
                    try {
                        return MessageDigest.getInstance("SHA-1");
                    } catch (Exception e2) {
                        return null;
                    }
                }
            case SHA1:
            default:
                try {
                    return MessageDigest.getInstance("SHA-1");
                } catch (Exception e2) {
                    return null;
                }
        }
    }

    private static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "x", new BigInteger(1, data));
    }

    public static String sha256(String pPassword) {
        return bin2hex(getHash(pPassword));
    }

    private static byte[] getHash(String password) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(password.getBytes());
    }
}
