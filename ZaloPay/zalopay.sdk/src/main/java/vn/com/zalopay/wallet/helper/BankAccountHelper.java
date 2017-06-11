package vn.com.zalopay.wallet.helper;

import java.util.List;

import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.controller.SDKApplication;

public class BankAccountHelper {
    private static final String TAG = BankAccountHelper.class.getCanonicalName();

    public static boolean isBankAccount(String pBankCode) {
        return CardType.PVCB.equals(pBankCode);
    }

    public static boolean hasBankAccountOnCache(String pUserId, String pBankCode) {
        try {
            List<BankAccount> bankAccountList = SDKApplication.getApplicationComponent()
                    .linkInteractor()
                    .getBankAccountList(pUserId);
            BankAccount bankAccount = new BankAccount();
            bankAccount.bankcode = pBankCode;
            return bankAccountList != null && bankAccountList.size() > 0 && bankAccountList.contains(bankAccount);

        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return false;
    }
}
