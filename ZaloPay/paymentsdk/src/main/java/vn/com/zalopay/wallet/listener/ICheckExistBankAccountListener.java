package vn.com.zalopay.wallet.listener;


public interface ICheckExistBankAccountListener {
    void onCheckExistBankAccountComplete(boolean pExisted);

    void onCheckExistBankAccountFail(String pMessage);
}
