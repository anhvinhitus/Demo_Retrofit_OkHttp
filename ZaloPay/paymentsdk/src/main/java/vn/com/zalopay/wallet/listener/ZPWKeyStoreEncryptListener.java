package vn.com.zalopay.wallet.listener;

/**
 * Created by cpu11843-local on 12/9/16.
 */

public interface ZPWKeyStoreEncryptListener {
    /***
     * failure callback
     *
     * @param error
     */
    void failure(String error);

    /***
     * success encrypt callback
     *
     * @param cipherText
     */
    void success(String cipherText);
}
