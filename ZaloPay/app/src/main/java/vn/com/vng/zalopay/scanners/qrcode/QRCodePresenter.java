package vn.com.vng.zalopay.scanners.qrcode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.JsonObject;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.qrcode.QRCodeStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZPTransaction;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;

/**
 * Created by longlv on 09/05/2016.
 * Controller for QR code scanning
 */

public final class QRCodePresenter extends AbstractPresenter<IQRScanView> {

    private final Context mApplicationContext;
    private final Navigator mNavigator;
    private final User mUser;

    private QRCodeStore.Repository mQRCodeRepository;
    private PaymentWrapper paymentWrapper;

    @Inject
    QRCodePresenter(BalanceStore.Repository balanceRepository,
                    ZaloPayRepository zaloPayRepository,
                    TransactionStore.Repository transactionRepository,
                    QRCodeStore.Repository qrCodeRepository,
                    Context applicationContext,
                    Navigator navigator,
                    User user) {
        Timber.d("New instance of QRCodePresenter");
        mApplicationContext = applicationContext;
        mNavigator = navigator;
        mUser = user;
        mQRCodeRepository = qrCodeRepository;
        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new PaymentRedirectListener())
                .build();
    }

    @Override
    public void resume() {
        hideLoadingView();
    }

    private void showLoadingView() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    private void showNetworkErrorAndResumeAfterDismiss() {
        if (mView == null) {
            return;
        }
        mView.showNetworkErrorDialog(new ZPWOnSweetDialogListener() {
            @Override
            public void onClickDiaLog(int i) {
                mView.resumeScanner();
            }
        });
    }

    void handleResult(String scanResult, boolean fromPhotoLibrary) {
        if (TextUtils.isEmpty(scanResult)) {
            Timber.i("Empty QR code");
            resumeScanningAfterWrongQR();
        } else if (AndroidUtils.isHttpRequest(scanResult)) {
            payViaUrl(scanResult);
        } else {
            pay(scanResult, fromPhotoLibrary);
        }
    }

    private void payViaUrl(String url) {
        showLoadingView();
        Subscription subscription = mQRCodeRepository.getPaymentInfo(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetPaymentInfoSubscribe(url));
        mSubscription.add(subscription);
    }

    private void pay(String jsonString, boolean fromPhotoLibrary) {
        Timber.d("start to process paying QR code: %s, from lib: %s", jsonString, fromPhotoLibrary);
        if (!NetworkHelper.isNetworkAvailable(mApplicationContext)) {
            showNetworkErrorAndResumeAfterDismiss();
            return;
        }

        if (TextUtils.isEmpty(jsonString)) {
            Timber.i("Empty QR code");
            resumeScanningAfterWrongQR();
            return;
        }

        Timber.d("about to process payment with order: %s", jsonString);
        showLoadingView();

        JSONObject data;
        try {
            data = new JSONObject(jsonString);
        } catch (JSONException e) {
            Timber.i(e, "Invalid JSON input");
            resumeScanningAfterWrongQR();
            return;
        }

        if (transferMoney(data, fromPhotoLibrary)) {
            return;
        }

        if (zpTransaction(data)) {
            return;
        }

        if (orderTransaction(data)) {
            return;
        }

        resumeScanningAfterWrongQR();
    }

    private boolean transferMoney(JSONObject data, boolean fromPhotoLibrary) {
        int type = data.optInt("type", -1);
        if (type == vn.com.vng.zalopay.Constants.QRCode.RECEIVE_MONEY) {
            if (tryTransferMoney(data)) {
                if (fromPhotoLibrary) {
                    ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_GETMTCODE);
                } else {
                    ZPAnalytics.trackEvent(ZPEvents.SCANQR_MONEYTRANSFER);
                }
                return true;
            }
        }
        return false;
    }

    private void resumeScanningAfterWrongQR() {
        hideLoadingView();

        ZPAnalytics.trackEvent(ZPEvents.SCANQR_WRONGCODE);
        qrDataInvalid();
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

        if (String.valueOf(zalopayId).equals(mUser.zaloPayId)) {
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
        bundle.putParcelable(vn.com.vng.zalopay.Constants.ARG_TRANSFERRECENT, item);
        mNavigator.startTransferActivity(mView.getContext(), bundle, false);
    }

    private boolean zpTransaction(JSONObject jsonObject) {

        ZPTransaction zpTransaction = new ZPTransaction(jsonObject);
        boolean isValidZPTransaction = zpTransaction.isValid();

        Timber.d("Trying with zptranstoken %s ", isValidZPTransaction);
        if (isValidZPTransaction) {
            paymentWrapper.payWithToken(mView.getActivity(), zpTransaction.appId, zpTransaction.transactionToken);
        }
        return isValidZPTransaction;
    }

    private boolean orderTransaction(JSONObject jsonOrder) {
        Order order = new Order(jsonOrder);
        boolean isValidOrder = order.isValid();
        if (isValidOrder) {
            paymentWrapper.payWithOrder(mView.getActivity(), order);
            hideLoadingView();
        }
        return isValidOrder;
    }

    private void qrDataInvalid() {
        if (mView != null && mView.getContext() != null) {
            mView.showWarningDialog(mView.getContext().getString(R.string.data_invalid),
                    new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {
                            mView.resumeScanner();
                        }

                        @Override
                        public void onOKevent() {
                            mView.resumeScanner();
                        }
                    });
        }
    }

    private String scanQRImage(Bitmap bMap) {
        String contents = null;

        int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        } catch (Exception e) {
            Timber.w(e, "Error decoding barcode");
        }
        return contents;
    }

    public void pay(final Intent data) {
        if (data == null) {
            qrDataInvalid();
            return;
        }
        final Uri uri = data.getData();
        if (uri == null) {
            qrDataInvalid();
            return;
        }
        Timber.d("pay by image uri [%s]", uri.toString());
        showLoadingView();
        Subscription subscription = ObservableHelper.makeObservable(new Callable<String>() {
            @Override
            public String call() {
                InputStream is = null;
                try {
                    is = mView.getContext().getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    String decoded = scanQRImage(bitmap);
                    Timber.d("Decoded string = %s", decoded);
                    return decoded;
                } catch (FileNotFoundException e) {
                    Timber.w(e, "Create input stream from uri exception");
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            Timber.d(e, "Close input stream exception");
                        }
                    }
                }
                return null;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String decoded) {
                        if (TextUtils.isEmpty(decoded)) {
                            ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_NOQRCODE);
                            resumeScanningAfterWrongQR();
                        } else {
                            handleResult(decoded, true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_NOQRCODE);
                        super.onError(e);
                    }
                });

        mSubscription.add(subscription);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (paymentWrapper == null) {
            return;
        }
        paymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    private void ensureResumeScannerInUIThread() {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                mView.resumeScanner();
            }
        });
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {

        @Override
        protected ILoadDataView getView() {
            return mView;
        }

        @Override
        public void onParameterError(String param) {
            super.onParameterError(param);

            if (mView == null) {
                return;
            }

            if ("token".equalsIgnoreCase(param)) {
                ZPAnalytics.trackEvent(ZPEvents.SCANQR_NOORDER);
            }
            ensureResumeScannerInUIThread();
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            if (mView == null) {
                return;
            }

            super.onResponseError(paymentError);
            hideLoadingView();
            ensureResumeScannerInUIThread();
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (mView != null && mView.getActivity() != null) {
                mView.getActivity().finish();
            }
        }

        @Override
        public void onAppError(String msg) {
            super.onAppError(msg);
            ensureResumeScannerInUIThread();
        }

        @Override
        public void onNotEnoughMoney() {
            if (mView == null || mView.getFragment() == null) {
                return;
            }

            mNavigator.startDepositForResultActivity(mView.getFragment());
        }
    }

    private class PaymentRedirectListener implements PaymentWrapper.IRedirectListener {
        @Override
        public void startUpdateProfileLevel(String walletTransId) {
            if (mView == null || mView.getFragment() == null) {
                return;
            }
            Timber.d("startUpdateProfileLevel");
            mNavigator.startUpdateProfile2ForResult(mView.getFragment(), walletTransId);
        }
    }

    private class GetPaymentInfoSubscribe extends DefaultSubscriber<JsonObject> {

        private String mUrl;

        GetPaymentInfoSubscribe(String url) {
            this.mUrl = url;
        }

        @Override
        public void onNext(JsonObject jsonObject) {
            hideLoadingView();
            if (jsonObject == null || TextUtils.isEmpty(jsonObject.toString())) {
                mView.showError(mView.getContext().getString(R.string.data_invalid));
                return;
            }
            pay(jsonObject.toString(), false);
        }

        @Override
        public void onError(Throwable e) {
            Timber.d("Get payment info error [%s]", e);
            hideLoadingView();
            if (e instanceof NetworkConnectionException) {
                showNetworkErrorAndResumeAfterDismiss();
            } else {
                if (mView != null && mView.getContext() != null) {
                    mNavigator.startWebAppActivity(mView.getContext(), mUrl);
                }
            }
        }
    }
}
