package vn.com.vng.zalopay.authentication.secret;

import javax.crypto.Cipher;

/**
 * Created by hieuvm on 5/7/17.
 * *
 */

interface IKeytool {
    Cipher getCipher(int mode);

    String decrypt(Cipher cipher);

    boolean encrypt(String hashPassword);
}
