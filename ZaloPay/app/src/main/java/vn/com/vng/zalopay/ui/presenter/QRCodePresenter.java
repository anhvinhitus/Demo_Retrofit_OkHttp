package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentStatus;

/**
 * Created by longlv on 09/05/2016.
 */

public final class QRCodePresenter extends BaseZaloPayPresenter implements IPresenter<IQRScanView> {

    private IQRScanView mView;

    private PaymentWrapper paymentWrapper;

    private User user;

    public QRCodePresenter(User user) {
        this.user = user;
        paymentWrapper = new PaymentWrapper(zaloPayRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mView.getActivity();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                if ("order".equalsIgnoreCase(param)) {
                    mView.showError(mView.getContext().getString(R.string.order_invalid));
                } else if ("uid".equalsIgnoreCase(param)) {
                    mView.showError(mView.getContext().getString(R.string.user_invalid));
                } else if ("token".equalsIgnoreCase(param)) {
                    hideLoadingView();
                    mView.showError(mView.getContext().getString(R.string.order_invalid));
                    mView.resumeScanner();
                }
            }

            @Override
            public void onResponseError(int status) {
                if (status == EPaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH.getNum()) {
                    mView.getActivity().finish();
                } else {
                    hideLoadingView();
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                transactionUpdate();
                if (mView != null && mView.getActivity() != null) {
                    mView.getActivity().finish();
                }
            }

            @Override
            public void onResponseTokenInvalid() {
                mView.onTokenInvalid();
                clearAndLogout();
            }

            @Override
            public void onResponseCancel() {
                hideLoadingView();
            }
        });
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
        super.destroy();
        this.destroyView();
    }

    private void showLoadingView() {
        mView.showLoading();
    }

    private void hideLoadingView() {
        mView.hideLoading();
    }

//    private void showErrorView(String message) {
//        mView.showError(message);
//    }

    public void pay(String jsonString) {
        Timber.d("about to process payment with order: %s", jsonString);
        try {
            showLoadingView();
            if (zpTransaction(jsonString)) {
                return;
            }

            orderTransaction(jsonString);
        }
        catch (JSONException | IllegalArgumentException e) {
            Timber.i("Invalid JSON input: %s", e.getMessage());
            hideLoadingView();
            qrDataInvalid();
            mView.resumeScanner();
        }
    }

    private boolean zpTransaction(String jsonOrder) throws JSONException, IllegalArgumentException {
        Timber.d("trying to get transaction token from: %s", jsonOrder);
        JSONObject jsonObject = new JSONObject(jsonOrder);
        long appId = jsonObject.optInt(Constants.APPID);
        String transactionToken = jsonObject.optString(Constants.ZPTRANSTOKEN);
        if (appId < 0 || TextUtils.isEmpty(transactionToken)) {
            throw new IllegalArgumentException();
        }
        paymentWrapper.payWithToken(appId, transactionToken);
        return true;
    }

    private void orderTransaction(String jsonOrder) throws JSONException, IllegalArgumentException {
        Order order = new Order(jsonOrder);
        if (order.getAppid() < 0) {
            throw new IllegalArgumentException();
        }
        if (TextUtils.isEmpty(order.getApptransid())) {
            throw new IllegalArgumentException();
        }
        if (TextUtils.isEmpty(order.getAppuser())) {
            throw new IllegalArgumentException();
        }
        if (TextUtils.isEmpty(order.getApptime())) {
            throw new IllegalArgumentException();
        }
        if (TextUtils.isEmpty(order.getItem())) {
            throw new IllegalArgumentException();
        }
        if (TextUtils.isEmpty(order.getAmount())) {
            throw new IllegalArgumentException();
        }
        long amount = Long.parseLong(order.getAmount());
        if (TextUtils.isEmpty(order.getEmbeddata())) {
            throw new IllegalArgumentException();
        }
        if (TextUtils.isEmpty(order.getMac())) {
            throw new IllegalArgumentException();
        }
        paymentWrapper.payWithOrder(order);
        hideLoadingView();
    }

    private void qrDataInvalid() {
        ToastUtil.showToast(mView.getActivity(), "Dữ liệu không hợp lệ.");
    }

//    private void showOrderDetail(Order order) {
//        mView.showOrderDetail(order);
//    }

}
