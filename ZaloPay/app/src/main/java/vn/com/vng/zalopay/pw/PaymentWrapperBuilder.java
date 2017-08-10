package vn.com.vng.zalopay.pw;

public class PaymentWrapperBuilder {
    private PaymentWrapper.IResponseListener mResponseListener;
    private PaymentWrapper.IRedirectListener mRedirectListener = null;
    private PaymentWrapper.ILinkCardListener mLinkCardListener = null;

    public PaymentWrapperBuilder setResponseListener(PaymentWrapper.IResponseListener responseListener) {
        mResponseListener = responseListener;
        return this;
    }

    public PaymentWrapperBuilder setRedirectListener(PaymentWrapper.IRedirectListener redirectListener) {
        mRedirectListener = redirectListener;
        return this;
    }

    public PaymentWrapperBuilder setLinkCardListener(PaymentWrapper.ILinkCardListener linkCardListener) {
        mLinkCardListener = linkCardListener;
        return this;
    }

    public PaymentWrapper build() {
        return new PaymentWrapper(mResponseListener, mRedirectListener, mLinkCardListener);
    }
}
