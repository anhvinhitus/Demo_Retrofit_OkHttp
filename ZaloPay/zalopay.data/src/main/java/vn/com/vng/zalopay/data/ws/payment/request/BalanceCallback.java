package vn.com.vng.zalopay.data.ws.payment.request;

import android.support.annotation.NonNull;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.response.BalanceResponse;

/**
 * Created by hieuvm on 3/8/17.
 */

public class BalanceCallback extends PaymentRequestCallback<BalanceResponse, Long> {

    public BalanceCallback() {
        super(BalanceResponse.class);
    }

    @Override
    public Long doBackground(@NonNull BalanceResponse result) {
        return result.zpwbalance;
    }

    @Override
    public void onResult(Long result) {
        Timber.d("onResult: %s", result);
    }
}
