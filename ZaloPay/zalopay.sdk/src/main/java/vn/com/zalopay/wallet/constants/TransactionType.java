package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({TransactionType.PAY, TransactionType.TOPUP, TransactionType.LINK, TransactionType.MONEY_TRANSFER, TransactionType.WITHDRAW})
@Retention(RetentionPolicy.SOURCE)
public @interface TransactionType {
    int PAY = 1;
    int TOPUP = 2;
    int LINK = 3;
    int MONEY_TRANSFER = 4;
    int WITHDRAW = 5;
}
