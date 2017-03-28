package vn.com.zalopay.wallet.datasource.request;

import java.util.HashMap;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.DataRepository;

public abstract class BaseTask<T extends BaseResponse> extends SingletonBase {
    protected T mResponse = null;
    protected HashMap<String, String> mDataParams;

    public BaseTask() {
        super();
        mDataParams = new HashMap<>();
        mResponse = null;
    }

    public abstract T onSaveResponseToDisk(T pResponse);

    public abstract void onRequestSuccess(T pResponse);

    public abstract void onRequestFail(Throwable e);

    public abstract void onRequestFail(String e);

    public abstract void onRequestInProcess();

    protected abstract void doRequest();

    protected abstract boolean doParams();

    public void makeRequest() {
        if (doParams()) {
            doRequest();
        }
    }

    protected T getResponse() {
        return this.mResponse;
    }

    protected void setResponse(T pResponse)
    {
        this.mResponse = pResponse;
    }

    protected void createReponse(int pCode, String pMessage) {
    }

    protected HashMap<String, String> getDataParams() {
        return mDataParams;
    }

    protected DataRepository shareDataRepository() {
        return DataRepository.shareInstance();
    }

    protected DataRepository newDataRepository() {
        return DataRepository.newInstance();
    }
}
