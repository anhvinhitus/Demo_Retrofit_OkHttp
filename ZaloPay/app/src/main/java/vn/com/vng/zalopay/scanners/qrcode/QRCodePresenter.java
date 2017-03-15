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
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

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
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.qrcode.QRCodeStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.ui.presenter.AbstractPaymentPresenter;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 09/05/2016.
 * Controller for QR code scanning
 */

public final class QRCodePresenter extends AbstractPaymentPresenter<IQRScanView> {

    private final Context mApplicationContext;
    private final Navigator mNavigator;
    private final User mUser;

    private QRCodeStore.Repository mQRCodeRepository;

    @Inject
    QRCodePresenter(BalanceStore.Repository balanceRepository,
                    ZaloPayRepository zaloPayRepository,
                    TransactionStore.Repository transactionRepository,
                    QRCodeStore.Repository qrCodeRepository,
                    Context applicationContext,
                    Navigator navigator,
                    User user) {
        super(balanceRepository, zaloPayRepository, transactionRepository, navigator);
        Timber.d("New instance of QRCodePresenter");
        mApplicationContext = applicationContext;
        mNavigator = navigator;
        mUser = user;
        mQRCodeRepository = qrCodeRepository;
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

        if (transferFixedMoney(data, fromPhotoLibrary)) {
            return;
        }

        if (zpTransaction(data)) {
            return;
        }

        if (orderTransaction(data)) {
            hideLoadingView();
            return;
        }

        resumeScanningAfterWrongQR();
    }

    private boolean transferMoney(JSONObject data, boolean fromPhotoLibrary) {
        int type = data.optInt("type", -1);
        if (type == Constants.QRCode.RECEIVE_MONEY) {
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

    private boolean transferFixedMoney(JSONObject data, boolean fromPhotoLibrary) {
        int type = data.optInt(Constants.TransferFixedMoney.TYPE);
        if (type == Constants.QRCode.RECEIVE_FIXED_MONEY) {
            if (tryTransferFixedMoney(data)) {
                if (fromPhotoLibrary) {
                    // TODO: 3/15/17 - longlv: need update track event
                    ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_GETMTCODE);
                } else {
                    // TODO: 3/15/17 - longlv: need update track event
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
        if (type != Constants.QRCode.RECEIVE_MONEY) {
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
        startMoneyTransfer(zalopayId, "", amount, messageBase64, Constants.QRCode.RECEIVE_MONEY);

        hideLoadingView();
        return true;
    }

    private boolean tryTransferFixedMoney(JSONObject data) {
        Timber.d("Try transfer fixed money via QrCode");

        int type = data.optInt(Constants.TransferFixedMoney.TYPE, -1);
        Timber.d("tryTransferFixedMoney type[%s]", type);
        if (type != Constants.QRCode.RECEIVE_FIXED_MONEY) {
            return false;
        }

        String zaloPayName = data.optString(Constants.TransferFixedMoney.ZALO_PAY_ID, "");
        Timber.d("tryTransferFixedMoney zaloPayId[%s]", zaloPayName);
        if (TextUtils.isEmpty(zaloPayName)) {
            return false;
        }

        if (String.valueOf(zaloPayName).equals(mUser.zalopayname)) {
            return false;
        }

        long amount = data.optLong(Constants.TransferFixedMoney.AMOUNT, -1);
        Timber.d("tryTransferFixedMoney [%s]", amount);
        if (amount <= 0) {
            return false;
        }

        String messageBase64 = data.optString(Constants.TransferFixedMoney.MESSAGE);
        Timber.d("tryTransferFixedMoney messageBase64[%s]", messageBase64);
        if (TextUtils.isEmpty(messageBase64)) {
            return false;
        }

        // Start money transfer process
        startMoneyTransfer(null, zaloPayName, amount, messageBase64, Constants.QRCode.RECEIVE_FIXED_MONEY);

        hideLoadingView();
        return true;
    }

    private void startMoneyTransfer(Long zaloPayId, String zaloPayName, long amount, String message, int type) {
        RecentTransaction item = new RecentTransaction();
        if (zaloPayId != null) {
            item.zaloPayId = String.valueOf(zaloPayId);
        }
        if (!TextUtils.isEmpty(zaloPayName)) {
            item.zaloPayName = zaloPayName;
        }
        if (amount != -1) {
            item.amount = amount;
        }

        if (!TextUtils.isEmpty(message)) {
            item.message = new String(Base64.decode(message, Base64.NO_PADDING | Base64.NO_WRAP));
        }

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_QR);
        bundle.putInt(Constants.ARG_MONEY_TRANSFER_TYPE, type);
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
        mNavigator.startTransferActivity(mView.getContext(), bundle, false);
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

    private void ensureResumeScannerInUIThread() {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                mView.resumeScanner();
            }
        });
    }

    @Override
    public void onPayParameterError(String param) {
        if (mView == null) {
            return;
        }

        if ("token".equalsIgnoreCase(param)) {
            ZPAnalytics.trackEvent(ZPEvents.SCANQR_NOORDER);
        }
        ensureResumeScannerInUIThread();
    }

    @Override
    public void onPayResponseError(PaymentError paymentError) {
        if (mView == null) {
            return;
        }

        hideLoadingView();
        ensureResumeScannerInUIThread();
    }

    @Override
    public void onPayResponseSuccess(ZPPaymentResult zpPaymentResult) {
        if (getActivity() != null) {
            mView.getActivity().finish();
        }
    }

    @Override
    public void onPayAppError(String msg) {
        ensureResumeScannerInUIThread();
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
