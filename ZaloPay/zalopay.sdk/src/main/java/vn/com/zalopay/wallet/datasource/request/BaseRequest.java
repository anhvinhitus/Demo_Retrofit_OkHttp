package vn.com.zalopay.wallet.datasource.request;

import java.util.HashMap;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.listener.IDataSourceListener;
import vn.com.zalopay.wallet.utils.Log;

public abstract class BaseRequest<T extends BaseResponse> extends SingletonBase {
    protected T mResponse = null;
    protected HashMap<String, String> mDataParams;
    private IDataSourceListener mDataSourceListener = new IDataSourceListener() {
        @Override
        public void onRequestAPIComplete(boolean isSuccess, String message, BaseResponse response) {
            if (isSuccess && response != null) {
                try {
                    mResponse = (T) response;
                    onRequestSuccess();
                } catch (Exception ex) {
                    Log.e(this, ex);
                    onRequestFail(null);
                }
            } else {
                Log.d(this, "===onRequestFail====message" + message);
                onRequestFail(null);
            }
        }

        @Override
        public void onRequestAPIProgress() {
            onRequestInProcess();
        }
    };

    public BaseRequest() {
        super();
        mDataParams = new HashMap<>();
        mResponse = null;
    }

    protected abstract void onRequestSuccess() throws Exception;

    protected abstract void onRequestFail(String pMessage);

    protected abstract void onRequestInProcess();

    protected abstract void doRequest();

    protected abstract boolean doParams();

    public void makeRequest() {
        if (doParams()) {
            doRequest();
        }
    }

    protected T getResponse() {
        return mResponse;
    }

    protected void createReponse(int pCode, String pMessage) {
    }

    protected IDataSourceListener getDataSourceListener() {
        return mDataSourceListener;
    }

    protected HashMap<String, String> getDataParams() {
        return mDataParams;
    }

    protected DataRepository shareDataRepository()
    {
        return DataRepository.shareInstance().setDataSourceListener(getDataSourceListener());
    }

    protected DataRepository newDataRepository()
    {
        return DataRepository.newInstance().setDataSourceListener(getDataSourceListener());
    }
}
