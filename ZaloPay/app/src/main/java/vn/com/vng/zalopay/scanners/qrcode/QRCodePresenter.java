package vn.com.vng.zalopay.scanners.qrcode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
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

    private enum QRCodeType {
        /// Zalo Pay QR Code for Receive money/Transfer money
        MoneyTransfer,
        /// Zalo Pay QR Code Type 2
        ReadOnlyMoneyTransfer,
        /// Zalo Pay Order with only appid and zptranstoken
        OrderWithTranstoken,
        /// Zalo Pay Order with full order information
        OrderWithFullInfo,
        /// Zalo Pay QR Code, valid but unsupported for current build version
        ZaloPayUnknown,
        /// Not a Zalo Pay QR Code
        Unknown
    }

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
        hideLoadingView();
        if (mView == null) {
            return;
        }
        mView.showNetworkErrorDialog(i -> mView.resumeScanner());
    }

    void handleResult(String scanResult, QRCodeResource qrCodeResource) {
        if (TextUtils.isEmpty(scanResult)) {
            Timber.i("Empty QR code");
            resumeScanningAfterWrongQR();
        } else if (AndroidUtils.isHttpRequest(scanResult)) {
            payViaUrl(scanResult);
        } else {
            pay(scanResult, qrCodeResource);
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

    private void pay(String jsonString, QRCodeResource qrCodeResource) {
        Timber.d("start to process paying QR code: %s, from resource: %s", jsonString, qrCodeResource.getValue());
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

        QRCodeType qrCodeType = detectQrCodeType(data);
        boolean executeResult;
        switch (qrCodeType) {
            case MoneyTransfer:
                executeResult = transferMoney(data, qrCodeResource);
                break;
            case ReadOnlyMoneyTransfer:
                executeResult = handleQRCodeOfZaloPay(data, qrCodeResource);
                break;
            case OrderWithTranstoken:
                executeResult = zpTransaction(data);
                break;
            case OrderWithFullInfo:
                executeResult = orderTransaction(data);
                hideLoadingView();
                break;
            case ZaloPayUnknown:
                showWarningDialog(R.string.qrcode_need_upgrade_to_pay);
                executeResult = true;
                if (qrCodeResource == QRCodeResource.PHOTO_LIBRARY) {
                    ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_ZALOPAY_UNKNOWN);
                } else {
                    ZPAnalytics.trackEvent(ZPEvents.SCANQR_ZALOPAY_UNKNOWN);
                }
                break;
            case Unknown:
                resumeScanningAfterWrongQR();
                executeResult = true;
                break;
            default:
                resumeScanningAfterWrongQR();
                executeResult = true;
                break;
        }

        if (!executeResult) {
            hideLoadingView();
        }
//        if (transferMoney(data, fromPhotoLibrary)) {
//            return;
//        }
//
//        boolean isQRCodeOfZaloPay = isQRCodeOfZaloPay(data);
//        Timber.d("pay isQRCodeOfZaloPay [%s]", isQRCodeOfZaloPay);
//        if (isQRCodeOfZaloPay && handleQRCodeOfZaloPay(data, fromPhotoLibrary)) {
//            return;
//        }
//
//        if (zpTransaction(data)) {
//            return;
//        }
//
//        if (orderTransaction(data)) {
//            hideLoadingView();
//            return;
//        }
//
//        if (isQRCodeOfZaloPay) {
//            showWarningDialog(R.string.qrcode_need_upgrade_to_pay);
//        } else {
//            resumeScanningAfterWrongQR();
//        }
    }

    private QRCodeType detectQrCodeType(JSONObject data) {
        if (data == null) {
            return null;
        }

        try {
            // detect if type of MoneyTransfer
            int type = data.optInt(Constants.ReceiveMoney.TYPE, -1);
            if (type == Constants.QRCode.RECEIVE_MONEY) {
                long zaloPayId = data.optLong(Constants.ReceiveMoney.UID, -1);
                if (zaloPayId > 0) {
                    return QRCodeType.MoneyTransfer;
                }
            }

            String app = data.optString(Constants.QRCode.APP);
            if (Constants.QRCode.ZALO_PAY.equalsIgnoreCase(app)) {
                // is Zalo Pay QR code

                // detect if type of ReadOnlyMoneyTransfer
                type = data.optInt(Constants.TransferFixedMoney.TYPE, -1);
                String zpid = data.optString(Constants.TransferFixedMoney.ZALO_PAY_ID);
                long amount = data.optInt(Constants.TransferFixedMoney.AMOUNT, -1);
                if (type == Constants.QRCode.RECEIVE_FIXED_MONEY &&
                        !TextUtils.isEmpty(zpid) &&
                        amount > 0) {
                    return QRCodeType.ReadOnlyMoneyTransfer;
                }

                return QRCodeType.ZaloPayUnknown;
            }

            long appid = data.optLong(vn.com.vng.zalopay.domain.Constants.APPID);
            String zptranstoken = data.optString(vn.com.vng.zalopay.domain.Constants.ZPTRANSTOKEN);

            if (appid > 0 && !TextUtils.isEmpty(zptranstoken)) {
                return QRCodeType.OrderWithTranstoken;
            }

            String apptransid = data.optString(vn.com.vng.zalopay.domain.Constants.APPTRANSID);
            String appuser = data.optString(vn.com.vng.zalopay.domain.Constants.APPUSER);
            long apptime = data.optLong(vn.com.vng.zalopay.domain.Constants.APPTIME);
            long amount = data.optLong(vn.com.vng.zalopay.domain.Constants.AMOUNT);

            if (appid > 0 && apptime > 0 && amount > 0 && !TextUtils.isEmpty(apptransid) && !TextUtils.isEmpty(appuser)) {
                return QRCodeType.OrderWithFullInfo;
            }

            return QRCodeType.Unknown;
        } catch (Throwable t) {
            Timber.d(t, "Exception while detecting QR code type");
            return null;
        }
    }

    private boolean handleQRCodeOfZaloPay(JSONObject data, QRCodeResource qrCodeResource) {
        int type = data.optInt(Constants.TransferFixedMoney.TYPE);
        if (type == Constants.QRCode.RECEIVE_FIXED_MONEY) {
            return transferFixedMoney(data, qrCodeResource);
        } else {
            return false;
        }
    }

    private boolean transferMoney(JSONObject data, QRCodeResource qrCodeResource) {
        int type = data.optInt(Constants.ReceiveMoney.TYPE, -1);
        if (type == Constants.QRCode.RECEIVE_MONEY) {
            if (tryTransferMoney(data)) {
                if (qrCodeResource == QRCodeResource.PHOTO_LIBRARY) {
                    ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_GETMTCODE);
                } else {
                    ZPAnalytics.trackEvent(ZPEvents.SCANQR_MONEYTRANSFER);
                }
                return true;
            }
        }
        return false;
    }

    private boolean transferFixedMoney(JSONObject data, QRCodeResource qrCodeResource) {
        if (tryTransferFixedMoney(data)) {
            if (qrCodeResource == QRCodeResource.PHOTO_LIBRARY) {
                ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_TYPE2);
            } else if (qrCodeResource == QRCodeResource.HTTP_REQUEST) {
                ZPAnalytics.trackEvent(ZPEvents.SCANQR_URL_TYPE2);
            } else {
                ZPAnalytics.trackEvent(ZPEvents.SCANQR_TYPE2);
            }
            return true;
        }
        return false;
    }

    private void resumeScanningAfterWrongQR() {
        ZPAnalytics.trackEvent(ZPEvents.SCANQR_WRONGCODE);
        showDialogDataInvalid();
    }

    private boolean tryTransferMoney(JSONObject data) {
        Timber.d("transferMoneyViaQrCode");

        List<String> fields = new ArrayList<>();
        int type = data.optInt(Constants.ReceiveMoney.TYPE, -1);
        if (type != Constants.QRCode.RECEIVE_MONEY) {
            return false;
        }

        fields.add(String.valueOf(type));

        long zaloPayId = data.optLong(Constants.ReceiveMoney.UID, -1);
        if (zaloPayId <= 0) {
            showDialogDataInvalid();
            return false;
        }

        if (String.valueOf(zaloPayId).equals(mUser.zaloPayId)) {
            showWarningDialog(R.string.can_not_transfer_money_for_your_self);
            return false;
        }

        fields.add(String.valueOf(zaloPayId));

        long amount = data.optLong(Constants.ReceiveMoney.AMOUNT, -1);
        if (amount != -1) {
            fields.add(String.valueOf(amount));
        }

        String messageBase64 = data.optString(Constants.ReceiveMoney.MESSAGE);
        if (!TextUtils.isEmpty(messageBase64)) {
            fields.add(messageBase64);
        }

        String checksum = data.optString(Constants.ReceiveMoney.CHECKSUM);
        if (TextUtils.isEmpty(checksum)) {
            showDialogDataInvalid();
            return false;
        }

        String computedChecksum = Utils.sha256(fields.toArray(new String[0])).substring(0, 8);

        Timber.d("tryTransferMoney: computedChecksum %s", computedChecksum);

        if (!checksum.equals(computedChecksum)) {
            Timber.d("Checksum does not match");
            showDialogDataInvalid();
            return false;
        }

        String message = "";
        if (!TextUtils.isEmpty(messageBase64)) {
            message = new String(Base64.decode(messageBase64, Base64.NO_PADDING | Base64.NO_WRAP));
        }

        // Start money transfer process
        startMoneyTransfer(zaloPayId, "", amount, message,
                Constants.TransferMode.TransferToZaloPayUser, Constants.ActivateSource.FromQRCodeType1);

        hideLoadingView();
        return true;
    }

    private boolean tryTransferFixedMoney(JSONObject data) {
        Timber.d("Try transfer fixed money via QrCode");

        int type = data.optInt(Constants.TransferFixedMoney.TYPE, -1);
        Timber.d("tryTransferFixedMoney type [%s]", type);
        if (type != Constants.QRCode.RECEIVE_FIXED_MONEY) {
            return false;
        }

        String zaloPayName = data.optString(Constants.TransferFixedMoney.ZALO_PAY_ID, "");
        Timber.d("tryTransferFixedMoney zaloPayId [%s]", zaloPayName);
        if (TextUtils.isEmpty(zaloPayName)) {
            showDialogDataInvalid();
            return false;
        }
        if (String.valueOf(zaloPayName).equals(mUser.zalopayname)) {
            showWarningDialog(R.string.can_not_transfer_money_for_your_self);
            return false;
        }

        long amount = data.optLong(Constants.TransferFixedMoney.AMOUNT, -1);
        Timber.d("tryTransferFixedMoney amount [%s]", amount);
        if (amount <= 0) {
            showDialogDataInvalid();
            return false;
        }

        String message = data.optString(Constants.TransferFixedMoney.MESSAGE);
        Timber.d("tryTransferFixedMoney message [%s]", message);

        // Start money transfer process
        startMoneyTransfer(null, zaloPayName, amount, message,
                Constants.TransferMode.TransferToZaloPayID, Constants.ActivateSource.FromQRCodeType2);

        hideLoadingView();
        return true;
    }

    private void startMoneyTransfer(Long zaloPayId, String zaloPayName, long amount, String message,
                                    Constants.TransferMode mode, Constants.ActivateSource activateSource) {
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

        item.message = message;

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.ARG_MONEY_TRANSFER_MODE, mode);
        bundle.putSerializable(Constants.ARG_MONEY_ACTIVATE_SOURCE, activateSource);
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
        mNavigator.startTransferActivity(mView.getContext(), bundle, false);
    }

    private void showDialogDataInvalid() {
        showWarningDialog(R.string.qrcode_data_invalid);
    }

    private void showWarningDialog(@StringRes int strResource) {
        hideLoadingView();
        if (mView != null) {
            mView.showWarningDialogAndResumeScan(strResource);
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
            showDialogDataInvalid();
            return;
        }
        final Uri uri = data.getData();
        if (uri == null) {
            showDialogDataInvalid();
            return;
        }
        Timber.d("pay by image uri [%s]", uri.toString());
        showLoadingView();
        Subscription subscription = ObservableHelper.makeObservable(() -> {
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
                            handleResult(decoded, QRCodeResource.PHOTO_LIBRARY);
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
        AndroidUtils.runOnUIThread(() -> mView.resumeScanner());
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
        // TODO: 3/20/17: showed error dialog & resume scan after dialog hide in AppError of AbstractPaymentPresenter.
        //ensureResumeScannerInUIThread();
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
                showDialogDataInvalid();
                return;
            }
            pay(jsonObject.toString(), QRCodeResource.HTTP_REQUEST);
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
