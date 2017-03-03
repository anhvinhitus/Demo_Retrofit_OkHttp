package vn.com.zalopay.wallet.datasource.request;

import java.util.HashMap;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.zalopay.ZaloPayBalance;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetBalanceImpl;
import vn.com.zalopay.wallet.listener.IDataSourceListener;
import vn.com.zalopay.wallet.listener.ZPWGetWalletBalanceListener;

public class GetBalance extends SingletonBase {

    private ZaloPayBalance mZaloPayBalance;

    private ZPWGetWalletBalanceListener mLoadBalanceListener;

    private HashMap<String, String> paramsRequest;

    private boolean isRetry;
    private IDataSourceListener mAPIResultListener = new IDataSourceListener() {
        @Override
        public void onRequestAPIComplete(boolean isSuccess, String message, BaseResponse response) {

            if (!isRetry && (response == null || !isSuccess)) {
                isRetry = true;
                retry();
                return;
            }
            if (isSuccess && response instanceof ZaloPayBalance) {
                mZaloPayBalance = (ZaloPayBalance) response;
            }

            onPostResult();
        }

        @Override
        public void onRequestAPIProgress() {

        }
    };


    public GetBalance(ZPWGetWalletBalanceListener pListener) {
        super();
        mLoadBalanceListener = pListener;

        paramsRequest = new HashMap<>();

        isRetry = false;
    }

    private void retry() {
        try {

            DataParameter.prepareGetBalanceParams(paramsRequest);
            DataRepository.newInstance().setDataSourceListener(mAPIResultListener).getData(new GetBalanceImpl(), paramsRequest);

        } catch (Exception ex) {
            onPostResult();
        }
    }

    private void onPostResult() {
        if (mLoadBalanceListener != null)
            mLoadBalanceListener.onGetBalanceComplete(mZaloPayBalance);
    }
}
