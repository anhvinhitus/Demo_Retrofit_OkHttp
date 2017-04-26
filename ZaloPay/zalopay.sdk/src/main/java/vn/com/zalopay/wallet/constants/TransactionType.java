package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({TransactionType.PAY, TransactionType.TOPUP, TransactionType.LINK_CARD, TransactionType.MONEY_TRANSFER, TransactionType.WITHDRAW, TransactionType.LINK_ACCOUNT})
@Retention(RetentionPolicy.SOURCE)
public @interface TransactionType {
    int PAY = 1;
    int TOPUP = 2;
    int LINK_CARD = 3;
    int MONEY_TRANSFER = 4;
    int WITHDRAW = 5;
    int LINK_ACCOUNT = 6;
}
