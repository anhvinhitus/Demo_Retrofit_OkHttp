package vn.com.zalopay.wallet.merchant.listener;

import java.util.List;

public interface IReloadMapInfoListener<T> {
    void onComplete(List<T> pMapList);

    void onError(String pErrorMess);
}
