package vn.com.zalopay.wallet.fingerprint;


public interface IFPCallback {
    void onError(FPError pError);

    void showPassword();

    void onComplete(String pHashPin);
}

