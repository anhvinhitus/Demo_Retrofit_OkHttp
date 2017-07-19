package vn.com.vng.zalopay.service;

import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.react.bridge.JSApplicationCausedNativeException;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.appresources.ExceptionEvent;
import vn.com.vng.zalopay.data.eventbus.DownloadZaloPayResourceEvent;
import vn.com.vng.zalopay.data.util.ConfigLoader;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.event.InternalAppExceptionEvent;
import vn.com.vng.zalopay.event.PaymentAppExceptionEvent;
import vn.com.vng.zalopay.event.UncaughtRuntimeExceptionEvent;

/**
 * Created by huuhoa on 6/11/16.
 * Implementation for global event handling service
 */
public class GlobalEventHandlingServiceImpl implements GlobalEventHandlingService {
    private Message mCurrentMessage;
    private Message mCurrentMessageAtLogin;
    private final EventBus mEventBus;
    private final AppResourceStore.Repository mAppRepository;

    public GlobalEventHandlingServiceImpl(EventBus eventBus, AppResourceStore.Repository appRepository) {
        this.mEventBus = eventBus;
        this.mEventBus.register(this);
        this.mAppRepository = appRepository;
    }

    @Override
    protected void finalize() throws Throwable {
        this.mEventBus.unregister(this);
        super.finalize();
    }

    @Override
    public void enqueueMessage(int messageType, String title, String body) {
        mCurrentMessage = new Message(messageType, title, body);
    }

    @Override
    public void enqueueMessageAtLogin(int messageType, String title, String body) {
        mCurrentMessageAtLogin = new Message(messageType, title, body);
    }

    @Override
    public void enqueueMessageAtLogin(int messageType, int title, int body) {
        mCurrentMessageAtLogin = new Message(messageType, title, body);
    }

    @Nullable
    @Override
    public Message popMessage() {
        Message message = mCurrentMessage;
        mCurrentMessage = null;
        return message;
    }

    @Override
    public Message popMessageAtLogin() {
        Message message = mCurrentMessageAtLogin;
        mCurrentMessageAtLogin = null;
        return message;
    }

    @Subscribe
    public void onInternalAppException(InternalAppExceptionEvent event) {
        enqueueMessage(SweetAlertDialog.WARNING_TYPE, "ĐÓNG", "Có lỗi xảy ra trong quá trình thực thi ứng dụng.");
        Crashlytics.log(Log.ERROR, "EXCEPTION", String.format("Internal App causes exception: %s", event.getInnerException().getMessage()));
        Crashlytics.logException(event.getInnerException());
        Answers.getInstance().logCustom(new CustomEvent("EXCEPTION INTERNAL APP"));
    }

    @Subscribe
    public void onPaymentAppException(PaymentAppExceptionEvent event) {
        enqueueMessage(SweetAlertDialog.WARNING_TYPE, "ĐÓNG", "Có lỗi xảy ra trong quá trình thực thi ứng dụng.");
        Crashlytics.log(Log.ERROR, "EXCEPTION", String.format(Locale.getDefault(), "Payment App %d causes exception: %s", event.getAppId(), event.getInnerException().getMessage()));
        Crashlytics.logException(event.getInnerException());
        Answers.getInstance().logCustom(new CustomEvent("EXCEPTION APP " + String.valueOf(event.getAppId())));
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Timber.i("Exception with type: %s message: %s", ex.getClass(), ex.getMessage());
        if (ex instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException) ex;

            Throwable cause = runtimeException.getCause();
            Timber.i("Exception cause: %s", cause == null ? "NULL" : cause.getClass());
            boolean shouldHandleException = false;
            if (cause instanceof ExecutionException || cause instanceof InterruptedException || cause == null) {
                Timber.i("Should handle uncaught exception");
                shouldHandleException = true;
            }

            if (ex instanceof JSApplicationCausedNativeException) {
                Timber.i("Should handle JSApplicationCausedNativeException exception");
                shouldHandleException = true;
            }

            if (shouldHandleException) {
                mEventBus.post(new UncaughtRuntimeExceptionEvent(ex));
            }
        }
        Timber.e(ex, "UncaughtException!!!");
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDownloadResourceSuccessEvent(DownloadZaloPayResourceEvent event) {
        Timber.d("on Download app %s resource success : url [%s]", event.mDownloadInfo.appid, event.mDownloadInfo.url);
        mAppRepository.isAppResourceAvailable(event.mDownloadInfo.appid, false)
                .filter(Boolean::booleanValue)
                .doOnError(Timber::d)
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        loadResourceInApp(event.mDownloadInfo.appid);
                    }
                });
    }

    @Subscribe
    public void onExceptionEvent(ExceptionEvent event) {
        if (event != null && event.exception != null && event.exception.getMessage().matches(".* " + "ENOSPC" + " .*")) {
            enqueueMessage(SweetAlertDialog.WARNING_TYPE, "ĐÓNG", "Bộ nhớ đã đầy, không tải được dữ liệu.");
        }

    }

    protected void loadResourceInApp(long appid) {
        Timber.d("begin load config in app [%s]", appid);
        if (appid == BuildConfig.WITHDRAW_APP_ID) {
            ConfigLoader.loadConfigFromResource(BuildConfig.WITHDRAW_APP_ID);
        } else if (appid == BuildConfig.ZALOPAY_APP_ID) {
            AndroidApplication.instance().loadFontFromApp1();
        }
    }
}
