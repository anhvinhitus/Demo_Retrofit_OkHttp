package vn.com.zalopay.wallet.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({BankFunctionCode.LINK_CARD, BankFunctionCode.LINK_BANK_ACCOUNT,
        BankFunctionCode.PAY_BY_CARD_TOKEN, BankFunctionCode.PAY_BY_BANKACCOUNT_TOKEN,
        BankFunctionCode.PAY, BankFunctionCode.WITHDRAW})
@Retention(RetentionPolicy.SOURCE)
public @interface BankFunctionCode {
    int LINK_CARD = 301;
    int LINK_BANK_ACCOUNT = 302;
    int PAY_BY_CARD_TOKEN = 105;
    int PAY_BY_BANKACCOUNT_TOKEN = 106;
    int WITHDRAW = 5;
    int PAY = -1;
}
