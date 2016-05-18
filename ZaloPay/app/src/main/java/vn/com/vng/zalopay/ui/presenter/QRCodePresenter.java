package vn.com.vng.zalopay.ui.presenter;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;

/**
 * Created by longlv on 09/05/2016.
 */

public final class QRCodePresenter extends BaseZaloPayPresenter implements Presenter<IQRScanView>, ZPPaymentListener {

    private IQRScanView mView;

    private Subscription subscriptionGetOrder;

    private User user;

    public QRCodePresenter(User user) {
        this.user = user;
    }

    @Override
    public void setView(IQRScanView view) {
        this.mView = view;
    }

    @Override
    public void destroyView() {
        this.mView = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        this.destroyView();
        this.unsubscribe();
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(subscriptionGetOrder);
    }

    private void showLoadingView() {
        mView.showLoading();
    }

    private void hideLoadingView() {
        mView.hideLoading();
    }

    private void showErrorView(String message) {
        mView.showError(message);
    }

    public void pay(String jsonString) {
        Timber.tag(TAG).d("getOrder................jsonOrder:" + jsonString);
        showLoadingView();
        if (zpTransaction(jsonString)) {
            return;
        }
        if (orderTransaction(jsonString)) {
            return;
        }
        hideLoadingView();
        qrDataInvalid();
        mView.resumeScanner();
    }

    private boolean zpTransaction(String jsonOrder) {
        Timber.tag(TAG).d("getOrder................jsonOrder:" + jsonOrder);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonOrder);
            long appId = jsonObject.getLong(Constants.APPID);
            String zptranstoken = jsonObject.getString(Constants.ZPTRANSTOKEN);
            getOrder(appId, zptranstoken);
            return true;
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean orderTransaction(String jsonOrder) {
        JSONObject jsonObject = null;
        try {
            Order order = new Order(jsonOrder);
            if (order == null || order.getAppid() < 0) {
                return false;
            }
            if (TextUtils.isEmpty(order.getApptransid())) {
                return false;
            }
            if (TextUtils.isEmpty(order.getAppuser())) {
                return false;
            }
            if (TextUtils.isEmpty(order.getApptime())) {
                return false;
            }
            if (TextUtils.isEmpty(order.getItem())) {
                return false;
            }
            if (TextUtils.isEmpty(order.getAmount())) {
                return false;
            }
            long amount = Long.parseLong(order.getAmount());
            if (TextUtils.isEmpty(order.getEmbeddata())) {
                return false;
            }
            if (TextUtils.isEmpty(order.getMac())) {
                return false;
            }
            pay(order);
            hideLoadingView();
            return true;
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void qrDataInvalid() {
        ToastUtil.showToast(mView.getActivity(), "Dữ liệu không hợp lệ.");
    }

    private void getOrder(long appId, String zalooauthcode) {
        subscriptionGetOrder = zaloPayRepository.getOrder(appId, zalooauthcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetOrderSubscriber());
    }

    private void showOrderDetail(Order order) {
        mView.showOrderDetail(order);
    }

    private final void onGetOrderError(Throwable e) {
        hideLoadingView();
        String message = ErrorMessageFactory.create(mView.getContext(), e);
        showErrorView(message);
    }

    private final void onGetOrderSuccess(Order order) {
        Timber.d("session =========" + order.getItem());
        hideLoadingView();
        pay(order);
    }


    private final class GetOrderSubscriber extends DefaultSubscriber<Order> {
        public GetOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("login success " + order);
            QRCodePresenter.this.onGetOrderSuccess(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "onError " + e);
            QRCodePresenter.this.onGetOrderError(e);
        }
    }

    //Zalo payment sdk
    private void pay(Order order) {
        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.==============");
        if (order == null) {
            showErrorView(mView.getContext().getString(R.string.order_invalid));
            return;
        }
        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................2");
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (user.uid <= 0) {
            showErrorView(mView.getContext().getString(R.string.user_invalid));
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
            Timber.tag("_____________________").d("paymentInfo.appTransID:" + paymentInfo.appTransID);
            paymentInfo.itemName = order.getItem();
            paymentInfo.amount = Long.parseLong(order.getAmount());
            paymentInfo.description = order.getDescription();
            paymentInfo.embedData = order.getEmbeddata();
            //lap vao ví appId = appUser = 1
            paymentInfo.appUser = order.getAppuser();
            paymentInfo.mac = order.getMac();

            Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................3");
//        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
            ZingMobilePayService.pay(mView.getActivity(), forcedPaymentChannel, paymentInfo, this);
        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onComplete(ZPPaymentResult pPaymentResult) {
        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("onComplete.................pPaymentResult:" + pPaymentResult);
        this.hideLoadingView();
        if (pPaymentResult == null) {
            if (!AndroidUtils.isNetworkAvailable(mView.getContext())) {
                mView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
            } else {
                mView.showError("Lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại sau.");
            }
        } else {
            int resultStatus = pPaymentResult.paymentStatus.getNum();
            if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
                transactionUpdate();
                if (mView != null && mView.getActivity() != null) {
                    mView.getActivity().finish();
                }
            } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.getNum()) {
                mView.onTokenInvalid();
            } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH.getNum()) {
                mView.getActivity().finish();
            }
        }
    }

    @Override
    public void onCancel() {
        this.hideLoadingView();
    }

    @Override
    public void onSMSCallBack(String appTransID) {

    }

    private void transactionUpdate() {
        zaloPayRepository.transactionUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
