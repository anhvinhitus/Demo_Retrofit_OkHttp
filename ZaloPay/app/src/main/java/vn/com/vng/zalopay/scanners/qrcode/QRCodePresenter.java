package vn.com.vng.zalopay.scanners.qrcode;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 09/05/2016.
 * Controller for QR code scanning
 */

public final class QRCodePresenter extends BaseUserPresenter implements IPresenter<IQRScanView> {

    private IQRScanView mView;

    private PaymentWrapper paymentWrapper;

    @Inject
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
            public void onResponseError(PaymentError paymentError) {
                if (mView == null) {
                    return;
                }

                if (paymentError == PaymentError.ERR_CODE_INTERNET) {
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
            public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {

            }

            @Override
            public void onAppError(String msg) {
                if (mView == null) {
                    return;
                }
                if (mView.getContext() != null) {
                    mView.showError(mView.getContext().getString(R.string.exception_generic));
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

    public void pay(String jsonString) {

        if (!NetworkHelper.isNetworkAvailable(applicationContext)) {
            if (mView != null) {
                mView.showError(applicationContext.getString(R.string.exception_no_connection_try_again));
                mView.resumeScanner();
            }
            return;
        }

        Timber.d("about to process payment with order: %s", jsonString);
        try {
            showLoadingView();

            JSONObject data;
            try {
                data = new JSONObject(jsonString);
            } catch (JSONException e) {
                Timber.i("Invalid JSON input: %s", e.getMessage());
                resumeScanningAfterWrongQR();
                return;
            }

            int type = data.optInt("type", -1);
            if (type == vn.com.vng.zalopay.Constants.QRCode.RECEIVE_MONEY) {
                if (tryTransferMoney(data)) {
                    return;
                }
            }

            if (zpTransaction(data)) {
                return;
            }

            if (orderTransaction(data)) {
                return;
            }

            resumeScanningAfterWrongQR();
        } catch (JSONException | IllegalArgumentException e) {
            Timber.i("Invalid JSON input: %s", e.getMessage());
            resumeScanningAfterWrongQR();
        }
    }

    private void resumeScanningAfterWrongQR() {
        hideLoadingView();

        ZPAnalytics.trackEvent(ZPEvents.SCANQR_WRONGCODE);
        qrDataInvalid();

        mView.resumeScanner();
    }

    private boolean tryTransferMoney(JSONObject data) {

        Timber.d("transferMoneyViaQrCode");

        List<String> fields = new ArrayList<>();
        int type = data.optInt("type", -1);
        if (type != vn.com.vng.zalopay.Constants.QRCode.RECEIVE_MONEY) {
            return false;
        }

        fields.add(String.valueOf(type));

        long zalopayId = data.optLong("uid", -1);
        if (zalopayId <= 0) {
            return false;
        }

        if (String.valueOf(zalopayId).equals(userConfig.getCurrentUser().zaloPayId)) {
            return false;
        }

        fields.add(String.valueOf(zalopayId));

        long amount = data.optLong("amount", -1);
        if (amount != -1) {
            fields.add(String.valueOf(amount));
        }

        String messageBase64 = data.optString("message");
        if (!TextUtils.isEmpty(messageBase64)) {
            fields.add(messageBase64);
        }

        String checksum = data.optString("checksum");
        if (TextUtils.isEmpty(checksum)) {
            return false;
        }

        String computedChecksum = Utils.sha256(fields.toArray(new String[0])).substring(0, 8);

        Timber.d("tryTransferMoney: computedChecksum %s", computedChecksum);

        if (!checksum.equals(computedChecksum)) {
            Timber.d("Checksum does not match");
            return false;
        }

        // Start money transfer process
        startMoneyTransfer(zalopayId, amount, messageBase64);

        hideLoadingView();
        return true;
    }

    private void startMoneyTransfer(long zalopayId, long amount, String message) {
        RecentTransaction item = new RecentTransaction();
        item.zaloPayId = String.valueOf(zalopayId);
        if (amount != -1) {
            item.amount = amount;
        }

        if (!TextUtils.isEmpty(message)) {
            item.message = new String(Base64.decode(message, Base64.NO_PADDING | Base64.NO_WRAP));
        }

        Bundle bundle = new Bundle();
        bundle.putInt(vn.com.vng.zalopay.Constants.ARG_MONEY_TRANSFER_MODE, vn.com.vng.zalopay.Constants.MoneyTransfer.MODE_QR);
        bundle.putParcelable(vn.com.vng.zalopay.Constants.ARG_TRANSFERRECENT, Parcels.wrap(item));
        navigator.startTransferActivity(mView.getContext(), bundle);
    }

    private boolean zpTransaction(JSONObject jsonObject) throws IllegalArgumentException {
        long appId = jsonObject.optInt(Constants.APPID);
        String transactionToken = jsonObject.optString(Constants.ZPTRANSTOKEN);
        if (appId < 0 || TextUtils.isEmpty(transactionToken)) {
            return false;
        }
        paymentWrapper.payWithToken(appId, transactionToken);
        return true;
    }

    private boolean orderTransaction(JSONObject jsonOrder) throws JSONException, IllegalArgumentException {
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
}
