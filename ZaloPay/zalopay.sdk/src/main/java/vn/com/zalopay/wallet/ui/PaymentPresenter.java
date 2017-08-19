package vn.com.zalopay.wallet.ui;

import android.content.Intent;

import timber.log.Timber;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkPaymentInfoReadyMessage;
import vn.com.zalopay.wallet.interactor.ChannelListInteractor;
import vn.com.zalopay.wallet.interactor.VersionCallback;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;

/**
 * Created by chucvv on 6/24/17.
 */

public abstract class PaymentPresenter<T extends IContract> extends AbstractPresenter<T> {
    protected ZPMonitorEventTiming mEventTiming = SDKApplication.getApplicationComponent().monitorEventTiming();

    protected void onProcessUpVersionMessage(VersionCallback message) {
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onUpVersion(message.forceupdate, message.newestappversion, message.forceupdatemessage);
        }
        if (message.forceupdate) {
            callback();
        }
    }

    protected void callback() {
    }

    /***
     * load app info from cache or api
     */
    protected void startSubscribePaymentReadyMessage() {
        Timber.d("Start subscribe payment data");
        try {
            getViewOrThrow().showLoading(GlobalData.getAppContext().getString(R.string.sdk_loading_payment_info_title));
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_ON_SUBSCRIBE_START);
        ChannelListInteractor interactor = SDKApplication.getApplicationComponent().channelListInteractor();
        interactor.subscribeOnPaymentReady(message -> {
            try {
                mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_ON_SUBSCRIBE);
                getViewOrThrow().hideLoading();
                onProcessPaymentInfo(message);
            } catch (Exception e) {
                Timber.d(e, "Exception when loading payment info");
            }
        });
    }

    public Intent getChannelIntent() {
        Intent intent = new Intent(GlobalData.getAppContext(), ChannelActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    protected abstract void onProcessPaymentInfo(SdkPaymentInfoReadyMessage message) throws Exception;
}
