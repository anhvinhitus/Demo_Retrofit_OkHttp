package vn.com.zalopay.wallet.api;

import vn.com.zalopay.wallet.entity.UserInfo;

/**
 * Created by huuhoa on 7/19/17.
 * Helper for CardGuiProcessor
 */

public interface ISdkErrorContext {
    boolean hasCardGuiProcessor();
    String getBankCode();
    String getTransactionId();
    UserInfo getUserInfo();
}
