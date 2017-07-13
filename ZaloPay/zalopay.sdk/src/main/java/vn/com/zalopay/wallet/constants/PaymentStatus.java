package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({PaymentStatus.NON_STATE, PaymentStatus.PROCESSING, PaymentStatus.SUCCESS, PaymentStatus.FAILURE,
        PaymentStatus.ERROR_BALANCE, PaymentStatus.TOKEN_EXPIRE, PaymentStatus.INVALID_DATA,
        PaymentStatus.USER_CLOSE, PaymentStatus.USER_LOCK, PaymentStatus.LEVEL_UPGRADE_PASSWORD,
        PaymentStatus.DISCONNECT, PaymentStatus.SERVICE_MAINTENANCE,
        PaymentStatus.UPVERSION, PaymentStatus.DIRECT_LINK_ACCOUNT, PaymentStatus.LEVEL_UPGRADE_CMND_EMAIL})
@Retention(RetentionPolicy.SOURCE)
public @interface PaymentStatus {
    int NON_STATE = -7;// order is processing on system, hasn't finish yet: client need to back user to home page
    int PROCESSING = 0; //order is processing
    int SUCCESS = 1;// transaction is success
    int FAILURE = -1; // transaction is fail
    int ERROR_BALANCE = -2; // user's wallet not enough money for payment, app redirect user to cash in
    int TOKEN_EXPIRE = -3; // expire token, maybe user login on many devices, app force user to logout
    int INVALID_DATA = -4; // order info is invalid
    int USER_CLOSE = -5; // user close transaction
    int USER_LOCK = -6;// zalopay account is locked
    int LEVEL_UPGRADE_PASSWORD = 6; // user need to up level, app force user to update numberphone + payment password
    int DISCONNECT = 8;// device is offline
    int SERVICE_MAINTENANCE = 9;// server is maintenance
    int UPVERSION = 10;// there're a newer version on store, app show dialog info and redirect user to newer version
    int DIRECT_LINKCARD = 11;// app need to redirect user to link card
    int DIRECT_LINK_ACCOUNT = 12;// app need to redirect user to link account
    int UPLEVEL_AND_LINK_BANKACCOUNT_AND_PAYMENT = 13;// user level 1 input Vietcombank card or select bank account channel, app need to redirect user to update level then link bank account and then redirect user to sdk again with previous order info
    int LEVEL_UPGRADE_CMND_EMAIL = 14;// user need to update cmnd and email
}
