package vn.com.vng.zalopay.service;

import android.app.Activity;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.wallet.ZingMobilePayService;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;

/**
 * Created by huuhoa on 6/3/16.
 * Wrapper for handle common processing involves with wallet SDK
 */
public class PaymentWrapper {
    public interface IViewListener {
        Activity getActivity();
    }
    public interface IResponseListener {
        void onParameterError(String param);
        void onResponseError(String message);
        void onResponseSuccess();
        void onResponseTokenInvalid();
        void onResponseCancel();
    }

    private final IViewListener viewListener;
    private final IResponseListener responseListener;
    public PaymentWrapper(IViewListener viewListener, IResponseListener responseListener) {
        this.viewListener = viewListener;
        this.responseListener = responseListener;
    }

    public void payWithOrder(Order order) {
        Timber.d("payWithOrder: Start");
        if (order == null) {
            Timber.i("payWithOrder: order is invalid");
            responseListener.onParameterError("order");
//            showErrorView(mView.getContext().getString(R.string.order_invalid));
            return;
        }
        Timber.d("payWithOrder: Order is valid");
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (user.uid <= 0) {
            Timber.i("payWithOrder: Uid is invalid");
            responseListener.onParameterError("uid");
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
            return;
        }
        try {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

            EPaymentChannel forcedPaymentChannel = null;
            paymentInfo.appID = order.getAppid();
            paymentInfo.zaloUserID = String.valueOf(user.uid);
            paymentInfo.zaloPayAccessToken = user.accesstoken;
            paymentInfo.appTime = Long.valueOf(order.getApptime());
            paymentInfo.appTransID = order.getApptransid();
            Timber.d("paymentInfo.appTransID: %s", paymentInfo.appTransID);
            paymentInfo.itemName = order.getItem();
            paymentInfo.amount = Long.parseLong(order.getAmount());
            paymentInfo.description = order.getDescription();
            paymentInfo.embedData = order.getEmbeddata();
            //lap vao ví appId = appUser = 1
            paymentInfo.appUser = order.getAppuser();
            paymentInfo.mac = order.getMac();

            Timber.d("payWithOrder: ZPWPaymentInfo is ready");

//        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
            ZingMobilePayService.pay(viewListener.getActivity(), forcedPaymentChannel, paymentInfo, zpPaymentListener);
        } catch (NumberFormatException e) {
            Timber.e(e, "Exception with number format");
        }

    }


    private ZPPaymentListener zpPaymentListener = new ZPPaymentListener() {
        @Override
        public void onComplete(ZPPaymentResult pPaymentResult) {
            if (pPaymentResult == null) {
                if (!AndroidUtils.isNetworkAvailable(viewListener.getActivity())) {
                    responseListener.onResponseError("Vui lòng kiểm tra kết nối mạng và thử lại.");
                } else {
                    responseListener.onResponseError("Lỗi xảy ra trong quá trình nạp tiền. Vui lòng thử lại sau.");
                }
            } else {
                int resultStatus = pPaymentResult.paymentStatus.getNum();
                if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
                    responseListener.onResponseSuccess();
                } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.getNum()) {
                    responseListener.onResponseTokenInvalid();
                }
            }
        }

        @Override
        public void onCancel() {
            responseListener.onResponseCancel();
        }

        @Override
        public void onSMSCallBack(String appTransID) {

        }
    };
}
