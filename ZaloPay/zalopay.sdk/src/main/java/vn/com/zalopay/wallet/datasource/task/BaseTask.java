package vn.com.zalopay.wallet.datasource.task;

import android.os.Build;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.DataRepository;

public abstract class BaseTask<T> extends SingletonBase {
    protected Map<String, String> mDataParams;

    public BaseTask() {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDataParams = new ArrayMap<>();
        } else {
            mDataParams = new HashMap<>();
        }
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

    protected Map<String, String> getDataParams() {
        return mDataParams;
    }

    protected DataRepository shareDataRepository() {
        return DataRepository.shareInstance();
    }

    protected DataRepository newDataRepository() {
        return DataRepository.newInstance();
    }
}
