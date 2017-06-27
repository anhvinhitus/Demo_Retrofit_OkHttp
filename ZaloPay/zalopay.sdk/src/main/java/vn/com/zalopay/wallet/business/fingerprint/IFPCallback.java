package vn.com.zalopay.wallet.business.fingerprint;


public interface IFPCallback {
    void onError(FPError pError);

    void showPassword();

    void onComplete(String pHashPin);
}

