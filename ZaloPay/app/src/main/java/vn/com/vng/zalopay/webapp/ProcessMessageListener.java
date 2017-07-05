package vn.com.vng.zalopay.webapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.zalopay.apploader.internal.ModuleName;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.ui.subscribe.MerchantUserInfoSubscribe;
import vn.com.vng.zalopay.ui.subscribe.StartPaymentAppSubscriber;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;

import static vn.com.vng.zalopay.paymentapps.PaymentAppConfig.getAppResource;

/**
 * Created by huuhoa on 4/20/17.
 */
class ProcessMessageListener implements IProcessMessageListener {
    private WeakReference<AbstractWebAppPresenter> mWebAppPresenterWeakReference;
//    private WeakReference<WebAppPresenter> mWebAppPresenterWeakReference;

    public ProcessMessageListener(AbstractWebAppPresenter presenter) {
        mWebAppPresenterWeakReference = new WeakReference<>(presenter);
    }

//    public ProcessMessageListener(WebAppPresenter presenter) {
//        mWebAppPresenterWeakReference = new WeakReference<>(presenter);
//    }

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
    public void launchApp(String packageID, String alternateUrl) {
        if (mWebAppPresenterWeakReference.get() == null ||
                mWebAppPresenterWeakReference.get().getActivity() == null) {
            return;
        }

        try {
            // Check whether the application exists or not
            boolean isPackageInstalled = isPackageInstalled(packageID, mWebAppPresenterWeakReference.get().getActivity());
            if (isPackageInstalled) {
                // Open app
                Intent launchIntent = mWebAppPresenterWeakReference.get().getActivity().getPackageManager().getLaunchIntentForPackage(packageID);

                mWebAppPresenterWeakReference.get().getActivity().startActivity(launchIntent);
            } else {
                String strDownloadLink = "https://play.google.com/store/apps/details?id=" + packageID;
                if (!TextUtils.isEmpty(strDownloadLink)) {
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
    public void launchInternalApp(int internalAppID) {
        if (mWebAppPresenterWeakReference.get() == null
                || mWebAppPresenterWeakReference.get().getActivity() == null) {
            return;
        }

        if (internalAppID == PaymentAppConfig.Constants.RED_PACKET) {
            mWebAppPresenterWeakReference.get().mNavigator.startMiniAppActivity(
                    mWebAppPresenterWeakReference.get().getActivity(), ModuleName.RED_PACKET);
        } else if (internalAppID == PaymentAppConfig.Constants.TRANSFER_MONEY) {
            mWebAppPresenterWeakReference.get().mNavigator.startTransferMoneyActivity(mWebAppPresenterWeakReference.get().getActivity());
        } else if (internalAppID == PaymentAppConfig.Constants.RECEIVE_MONEY) {
            mWebAppPresenterWeakReference.get().mNavigator.startReceiveMoneyActivity(mWebAppPresenterWeakReference.get().getActivity());
        } else if (internalAppID == 15) {
            openAppWebService(internalAppID);
        } else {
            AppResource appResource = getAppResource(internalAppID);
            if (appResource == null) {
                appResource = new AppResource(internalAppID);
            }
            startExternalApp(appResource);
        }
    }

    @Override
    public void sendEmail() {
//        mWebAppPresenterWeakReference.get().mNavigator.startEmail(
//                mWebAppPresenterWeakReference.get().getActivity(),
//                "hotro@zalopay.vn",
//                null,
//                "Yêu cầu hỗ trợ thanh toán game",
//                "Muốn hoàn tiền",
//                null);
    }

    @Override
    public void openDial() {
//        mWebAppPresenterWeakReference.get().mNavigator.startDialSupport(mWebAppPresenterWeakReference.get().getActivity());
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

    private void startExternalApp(AppResource app) {
        Subscription subscription = mWebAppPresenterWeakReference.get().mAppResourceRepository.isAppResourceAvailable(app.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StartPaymentAppSubscriber(mWebAppPresenterWeakReference.get().mNavigator,
                        mWebAppPresenterWeakReference.get().getActivity(), app));
        mWebAppPresenterWeakReference.get().getSubscription().add(subscription);
    }

    public void openAppWebService(int appID) {
        Subscription subscription = mWebAppPresenterWeakReference.get().mAppResourceRepository.getListAppHome()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResourceSubscriber(appID));
        mWebAppPresenterWeakReference.get().getSubscription().add(subscription);
    }

    private void starWebAppService(int internalAppID, String webURL) {
        Subscription subscription = mWebAppPresenterWeakReference.get().mMerchantRepository.getMerchantUserInfo(internalAppID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MerchantUserInfoSubscribe(mWebAppPresenterWeakReference.get().mNavigator,
                        mWebAppPresenterWeakReference.get().getActivity(), internalAppID, webURL));
        mWebAppPresenterWeakReference.get().getSubscription().add(subscription);
    }

    private void handleOpenWebService(int internalAppID, List<AppResource> listResouces) {
        if (listResouces == null) return;

        String webURL = "";
        for (AppResource appResource : listResouces) {
            if (appResource.appid == 15) {
                webURL = appResource.webUrl;
                break;
            }
        }

        if (!TextUtils.isEmpty(webURL)) {
            starWebAppService(internalAppID, webURL);
        }
    }

    private class AppResourceSubscriber extends DefaultSubscriber<List<AppResource>> {
        private int mAppID;

        AppResourceSubscriber(int appID) {
            this.mAppID = appID;
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
        }

        @Override
        public void onNext(List<AppResource> appResources) {
            handleOpenWebService(mAppID, appResources);
        }

        @Override
        public void onError(Throwable e) {
        }
    }
}