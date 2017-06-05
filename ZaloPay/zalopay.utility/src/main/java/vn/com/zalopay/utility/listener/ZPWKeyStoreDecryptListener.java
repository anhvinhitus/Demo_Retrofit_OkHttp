package vn.com.zalopay.utility.listener;

/**
 * Created by cpu11843-local on 12/9/16.
 */

public interface ZPWKeyStoreDecryptListener {
    /***
     * failure callback
     *
     * @param error
     */
    void failure(String error);

    /***
     * success decrypt callback
     *
     * @param plainText
     */
    void success(String plainText);
}
