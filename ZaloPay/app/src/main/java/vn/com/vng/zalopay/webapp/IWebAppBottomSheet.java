package vn.com.vng.zalopay.webapp;

/**
 * Created by khattn on 2/27/17.
 */

public interface IWebAppBottomSheet {

    void handleClickShareOnZalo(String currentUrl);

    void handleClickCopyURL(String currentUrl);

    void handleClickOpenInBrowser(String currentUrl);

}
