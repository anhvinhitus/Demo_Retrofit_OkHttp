package vn.com.vng.zalopay.data.ws.payment.request;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by hieuvm on 3/9/17.
 */

class InternalHandler extends Handler {

    static final int MESSAGE_POST_RESULT = 1;

    private InternalHandler() {
        super(Looper.getMainLooper());
    }

    @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
    @Override
    public void handleMessage(Message msg) {
        PaymentRequestResult<?> result = (PaymentRequestResult<?>) msg.obj;
        switch (msg.what) {
            case MESSAGE_POST_RESULT:
                result.mTask.onResult(result.mData[0]);
                break;
        }

    }

    private static InternalHandler sHandler;

    static Handler getHandler() {
        synchronized (PaymentRequestCallback.class) {
            if (sHandler == null) {
                sHandler = new InternalHandler();
            }
            return sHandler;
        }
    }

    static class PaymentRequestResult<Data> {
        final PaymentRequestCallback mTask;
        final Data[] mData;

        PaymentRequestResult(PaymentRequestCallback task, Data... data) {
            mTask = task;
            mData = data;
        }
    }
}