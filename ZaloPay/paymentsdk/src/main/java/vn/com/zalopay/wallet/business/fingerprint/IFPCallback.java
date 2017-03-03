package vn.com.zalopay.wallet.business.fingerprint;


public interface IFPCallback {
    void onError(FPError pError);

    void onCancel();

    void onComplete(String pHashPin);
}
