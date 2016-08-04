package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 09/05/2016.
 */

public final class QRCodePresenter extends BaseZaloPayPresenter implements IPresenter<IQRScanView> {

    private IQRScanView mView;

    private PaymentWrapper paymentWrapper;

    public QRCodePresenter() {
        paymentWrapper = new PaymentWrapper(balanceRepository, zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                if (mView != null) {
                    return mView.getActivity();
                }
                return null;
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                if (mView == null) {
                    return;
                }

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
                if (mView == null) {
                    return;
                }

                if (status == PaymentError.ERR_CODE_INTERNET) {
                    mView.showError(applicationContext.getString(R.string.exception_no_connection_try_again));
                }
                hideLoadingView();
                mView.resumeScanner();
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                if (mView != null && mView.getActivity() != null) {
                    mView.getActivity().finish();
                }
            }

            @Override
            public void onResponseTokenInvalid() {
                if (mView == null) {
                    return;
                }

                mView.onTokenInvalid();
                clearAndLogout();
            }

            @Override
            public void onResponseCancel() {
                if (mView == null) {
                    return;
                }

                hideLoadingView();
                mView.resumeScanner();
            }

            @Override
            public void onNotEnoughMoney() {
                if (mView == null) {
                    return;
                }

                navigator.startDepositActivity(applicationContext);
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
        hideLoadingView();
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
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

            if (orderTransaction(jsonString)) {
                return;
            }

            hideLoadingView();

            ZPAnalytics.trackEvent(ZPEvents.SCANQR_WRONGCODE);
            qrDataInvalid();

            mView.resumeScanner();
        } catch (JSONException | IllegalArgumentException e) {
            Timber.i("Invalid JSON input: %s", e.getMessage());
            hideLoadingView();

            ZPAnalytics.trackEvent(ZPEvents.SCANQR_WRONGCODE);
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
            return false;
        }
        paymentWrapper.payWithToken(appId, transactionToken);
        return true;
    }

    private boolean orderTransaction(String jsonOrder) throws JSONException, IllegalArgumentException {
        Order order = new Order(jsonOrder);
        if (order.getAppid() < 0) {
            return false;
        }
        if (TextUtils.isEmpty(order.getApptransid())) {
            return false;
        }
        if (TextUtils.isEmpty(order.getAppuser())) {
            return false;
        }
        if (order.getApptime() <= 0) {
            return false;
        }
        if (TextUtils.isEmpty(order.getItem())) {
            return false;
        }
        if (order.getAmount() < 0) {
            return false;
        }
        if (TextUtils.isEmpty(order.getDescription())) {
            return false;
        }
        if (TextUtils.isEmpty(order.getMac())) {
            return false;
        }
        paymentWrapper.payWithOrder(order);
        hideLoadingView();
        return true;
    }

    private void qrDataInvalid() {
        ToastUtil.showToast(mView.getActivity(), "Dữ liệu không hợp lệ.");
    }

//    private void showOrderDetail(Order order) {
//        mView.showOrderDetail(order);
//    }

}
