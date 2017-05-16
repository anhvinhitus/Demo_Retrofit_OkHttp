package vn.com.vng.zalopay.webapp;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Collections;

import timber.log.Timber;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;

/**
 * Created by huuhoa on 4/20/17.
 */
class ProcessMessageListener implements IProcessMessageListener {
    private WeakReference<WebAppPresenter> mWebAppPresenterWeakReference;

    public ProcessMessageListener(WebAppPresenter presenter) {
        mWebAppPresenterWeakReference = new WeakReference<>(presenter);
    }

    @Override
    public void hideLoading() {
        if (mWebAppPresenterWeakReference.get() == null) {
            return;
        }

        mWebAppPresenterWeakReference.get().hideLoading();
    }

    @Override
    public void writeLog(String type, long time, String data) {
        switch (type) {
            case "info":
                Timber.i("time: %s, data: %s", time, data);
                break;
            case "warn":
                Timber.w("time: %s, data: %s", time, data);
                break;
            case "error":
                Timber.e("time: %s, data: %s", time, data);
                break;
            default:
                Timber.d("type: %s, time: %s, data: %s", type, time, data);
        }
    }

    @Override
    public void showDialog(final int dialogType, final String title, final String message, final String buttonLabel) {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (mWebAppPresenterWeakReference.get() == null) {
                    return;
                }

                DialogHelper.showCustomDialog(mWebAppPresenterWeakReference.get().getActivity(),
                        dialogType,
                        title,
                        message,
                        null,
                        Collections.singletonList(buttonLabel).toArray(new String[0]));
            }
        });
    }

    @Override
    public void showLoading() {
        if (mWebAppPresenterWeakReference.get() == null) {
            return;
        }

        mWebAppPresenterWeakReference.get().showLoading();
    }

    @Override
    public void payOrder(JSONObject jsonObject, IPaymentListener listener) {
        if (mWebAppPresenterWeakReference.get() == null) {
            return;
        }

        mWebAppPresenterWeakReference.get().pay(jsonObject, listener);
    }

    @Override
    public void transferMoney(JSONObject jsonObject, IPaymentListener listener) {
        if (mWebAppPresenterWeakReference.get() == null) {
            return;
        }

        mWebAppPresenterWeakReference.get().transferMoney(jsonObject, listener);
    }
}
