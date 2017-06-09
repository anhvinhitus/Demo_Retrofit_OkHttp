package vn.com.vng.zalopay.webapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

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
    public void launchApp(String packageID) {
        try {
            // Check whether the application exists or not
            boolean isPackageInstalled = isPackageInstalled(packageID, mWebAppPresenterWeakReference.get().getActivity());
            if (isPackageInstalled) {
                // Open app
                Intent launchIntent = mWebAppPresenterWeakReference.get().getActivity().getPackageManager().getLaunchIntentForPackage(packageID);

                mWebAppPresenterWeakReference.get().getActivity().startActivity(launchIntent);
            } else {
                String strDownloadLink = "https://play.google.com/store/apps/details?id=" + packageID;
                if (TextUtils.isEmpty(strDownloadLink)) {
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                    i.setData(Uri.parse(strDownloadLink));
                    mWebAppPresenterWeakReference.get().getActivity().startActivity(i);
                }
            }
        } catch (Exception e) {
            Timber.e("Open application error");
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

    private boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
