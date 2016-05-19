package vn.com.vng.zalopay.mdl;

/**
 * Created by AnhHieu on 5/16/16.
 */
public interface BundleReactConfig {

    String getInternalJsBundle();

    boolean isInternalDevSupport();

    String getExternalJsBundle(String paymentAppName);

    boolean isExternalDevSupport();
}
