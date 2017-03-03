package vn.com.zalopay.wallet.listener;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface IPaymentApiCallBack<T> {
    void onFinish(Call call, Response<T> response);

    void onFail(Callback pCall, Throwable t);
}
