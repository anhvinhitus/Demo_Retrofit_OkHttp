package vn.com.zalopay.wallet.event;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;

/**
 * Created by huuhoa on 6/29/17.
 * Payment information is ready to used
 */

public class SdkPaymentInfoReadyMessage {
    public AppInfo mAppInfo;
    public Throwable mError;

    public enum ErrorType {
        /**
         * No error
         */
        SUCCESS,

        /**
         * Error when loading app info
         */
        LOAD_APP_INFO_ERROR,

        /**
         * Error when loading platform info
         */
        LOAD_PLATFORM_INFO_ERROR,

        /**
         * Error when loading card list
         */
        LOAD_CARD_LIST_ERROR,

        /**
         * Error when loading bank list
         */
        LOAD_BANK_LIST_ERROR,

        /**
         * General errors that do not fall into above categories
         */
        LOAD_PAYMENT_INFO_ERROR,
    }

    public ErrorType mErrorType;
}
