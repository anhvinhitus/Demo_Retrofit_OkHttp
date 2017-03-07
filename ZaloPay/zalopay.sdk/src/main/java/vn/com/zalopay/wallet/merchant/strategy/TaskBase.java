package vn.com.zalopay.wallet.merchant.strategy;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.merchant.listener.IMerchantListener;

public abstract class TaskBase implements IMerchantTask {
    protected WeakReference<IMerchantListener> mListener;

    protected abstract void onDoIt();

    protected IMerchantListener getListener() {
        return mListener.get();
    }

    @Override
    public void onTaskInProcess() {
        if (getListener() != null) {
            getListener().onProcess();
        }
    }

    @Override
    public void onPrepareTaskComplete() {
        onDoIt();
    }

    @Override
    public void onTaskError(String pErrorMess) {
        if (getListener() != null) {
            getListener().onError(pErrorMess);
        }
    }

    @Override
    public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
        if (getListener() != null) {
            getListener().onUpVersion(pForceUpdate, pVersion, pMessage);
        }
    }

    @Override
    public void setTaskListener(IMerchantListener pListener) {
        this.mListener = new WeakReference<IMerchantListener>(pListener);
    }
}
