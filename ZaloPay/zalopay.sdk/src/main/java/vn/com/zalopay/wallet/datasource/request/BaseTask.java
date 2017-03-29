package vn.com.zalopay.wallet.datasource.request;

import java.util.HashMap;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.DataRepository;

public abstract class BaseTask<T extends BaseResponse> extends SingletonBase {
    protected HashMap<String, String> mDataParams;

    public BaseTask() {
        super();
        mDataParams = new HashMap<>();
    }

    public abstract void onDoTaskOnResponse(T pResponse);//do something after finishing request(save response to cache...)

    public abstract void onRequestSuccess(T pResponse);

    public abstract void onRequestFail(Throwable e);

    public abstract void onRequestInProcess();

    public abstract String getDefaulErrorNetwork();

    protected abstract void doRequest();

    protected abstract boolean doParams();

    public void makeRequest() {
        if (doParams()) {
            doRequest();
        }
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
