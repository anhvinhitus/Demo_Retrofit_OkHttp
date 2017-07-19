package vn.com.zalopay.wallet.api;

import vn.com.zalopay.wallet.business.entity.user.UserInfo;

/**
 * Created by huuhoa on 7/19/17.
 * Helper for CardGuiProcessor
 */

public interface ISdkErrorContext {
    boolean hasCardGuiProcessor();
    String getDetectedBankCode();
    String getTransactionId();
    UserInfo getUserInfo();
}
