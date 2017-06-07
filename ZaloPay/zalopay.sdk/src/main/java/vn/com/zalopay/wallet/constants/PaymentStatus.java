package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({PaymentStatus.PROCESSING, PaymentStatus.SUCCESS, PaymentStatus.FAILURE,
        PaymentStatus.MONEY_NOT_ENOUGH, PaymentStatus.TOKEN_EXPIRE, PaymentStatus.INVALID_DATA,
        PaymentStatus.USER_CLOSE, PaymentStatus.USER_LOCK, PaymentStatus.LEVEL_UPGRADE_PASSWORD,
        PaymentStatus.DISCONNECT, PaymentStatus.SERVICE_MAINTENANCE,
        PaymentStatus.UPVERSION, PaymentStatus.DIRECT_LINKCARD, PaymentStatus.DIRECT_LINKCARD_AND_PAYMENT,
        PaymentStatus.DIRECT_LINK_ACCOUNT, PaymentStatus.DIRECT_LINK_ACCOUNT_AND_PAYMENT,
        PaymentStatus.UPLEVEL_AND_LINK_BANKACCOUNT_AND_PAYMENT, PaymentStatus.LEVEL_UPGRADE_CMND_EMAIL})
@Retention(RetentionPolicy.SOURCE)
public @interface PaymentStatus {
    int PROCESSING = 0; //order is processing
    int SUCCESS = 1;// transaction is success
    int FAILURE = -1; // transaction is fail
    int MONEY_NOT_ENOUGH = -2; // user's wallet not enough money for payment, app redirect user to cash in
    int TOKEN_EXPIRE = -3; // expire token, maybe user login on many devices, app force user to logout
    int INVALID_DATA = -4; // order info is invalid
    int USER_CLOSE = -5; // user close transaction
    int USER_LOCK = -6;// zalopay account is locked
    int LEVEL_UPGRADE_PASSWORD = 6; // user need to up level, app force user to update numberphone + payment password
    int DISCONNECT = 8;// device is offline
    int SERVICE_MAINTENANCE = 9;// server is maintenance
    int UPVERSION = 10;// there're a newer version on store, app show dialog info and redirect user to newer version
    int DIRECT_LINKCARD = 11;// app need to redirect user to link card
    int DIRECT_LINKCARD_AND_PAYMENT = 12;//user using BIDV for payment but hasn't link card yet, app redirect user to link card then auto redirect user to sdk again with previous order info
    int DIRECT_LINK_ACCOUNT = 13;// app need to redirect user to link account
    int DIRECT_LINK_ACCOUNT_AND_PAYMENT = 14;//user using Vietcombank for payment but hasn't link account yet, app redirect user to link account then auto redirect user to sdk again with previous order info
    int UPLEVEL_AND_LINK_BANKACCOUNT_AND_PAYMENT = 15;// user level 1 input Vietcombank card or select bank account channel, app need to redirect user to update level then link bank account and then redirect user to sdk again with previous order info
    int LEVEL_UPGRADE_CMND_EMAIL = 16;// user need to update cmnd and email
}
