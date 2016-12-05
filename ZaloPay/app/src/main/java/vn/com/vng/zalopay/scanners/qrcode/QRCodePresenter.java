package vn.com.vng.zalopay.scanners.qrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;

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

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;

/**
 * Created by longlv on 09/05/2016.
 * Controller for QR code scanning
 */

public final class QRCodePresenter extends BaseUserPresenter implements IPresenter<IQRScanView> {

    private final Context mApplicationContext;
    private final Navigator mNavigator;
    private final UserConfig mUserConfig;
    private IQRScanView mView;

    private PaymentWrapper paymentWrapper;

    @Inject
    QRCodePresenter(BalanceStore.Repository balanceRepository,
                    ZaloPayRepository zaloPayRepository,
                    TransactionStore.Repository transactionRepository,
                    Context applicationContext,
                    Navigator navigator,
                    UserConfig userConfig) {
        mApplicationContext = applicationContext;
        mNavigator = navigator;
        mUserConfig = userConfig;
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
                    ZPAnalytics.trackEvent(ZPEvents.SCANQR_NOORDER);
                    mView.resumeScanner();
                }
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                if (mView == null) {
                    return;
                }

                if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                    mView.showWarning(mApplicationContext.getString(R.string.exception_no_connection_try_again));
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

                mNavigator.startDepositActivity(mApplicationContext);
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
        pay(jsonString, false);
    }

    private void pay(String jsonString, boolean fromPhotoLibrary) {

        if (!NetworkHelper.isNetworkAvailable(mApplicationContext)) {
            if (mView != null) {
                mView.showError(mApplicationContext.getString(R.string.exception_no_connection_try_again));
                mView.resumeScanner();
            }
            return;
        }

        if (TextUtils.isEmpty(jsonString)) {
            resumeScanningAfterWrongQR();
            return;
        }
        Timber.d("about to process payment with order: %s", jsonString);
        try {
            showLoadingView();

            JSONObject data = new JSONObject(jsonString);

            int type = data.optInt("type", -1);
            if (type == vn.com.vng.zalopay.Constants.QRCode.RECEIVE_MONEY) {
                if (tryTransferMoney(data)) {
                    if (fromPhotoLibrary) {
                        ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_GETMTCODE);
                    } else {
                        ZPAnalytics.trackEvent(ZPEvents.SCANQR_MONEYTRANSFER);
                    }
                    return;
                }
            }

            if (zpTransaction(data)) {
                return;
            }

            if (orderTransaction(data)) {
                return;
            }
        } catch (JSONException | IllegalArgumentException e) {
            Timber.i("Invalid JSON input: %s", e.getMessage());
        }
        resumeScanningAfterWrongQR();
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

        if (String.valueOf(zalopayId).equals(mUserConfig.getCurrentUser().zaloPayId)) {
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
        if (order.appid < 0) {
            return false;
        }
        if (TextUtils.isEmpty(order.apptransid)) {
            return false;
        }
        if (TextUtils.isEmpty(order.appuser)) {
            return false;
        }
        if (order.apptime <= 0) {
            return false;
        }
        if (TextUtils.isEmpty(order.item)) {
            return false;
        }
        if (order.amount < 0) {
            return false;
        }
        if (TextUtils.isEmpty(order.description)) {
            return false;
        }
        if (TextUtils.isEmpty(order.mac)) {
            return false;
        }
        paymentWrapper.payWithOrder(order);
        hideLoadingView();
        return true;
    }

    private void qrDataInvalid() {
        if (mView != null && mView.getContext() != null) {
            mView.showWarning(mView.getContext().getString(R.string.data_invalid),
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
            Timber.w("Error decoding barcode", e);
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
        }
        Timber.d("pay by image uri[%s]", uri.toString());
        showLoadingView();
        ObservableHelper.makeObservable(new Callable<String>() {
            @Override
            public String call() {
                InputStream is = null;
                try {
                    is = mView.getContext().getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    String decoded = scanQRImage(bitmap);
                    Timber.d("Decoded string=" + decoded);
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
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String decoded) {
                        if (TextUtils.isEmpty(decoded)) {
                            ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_NOQRCODE);
                        } else {
                            pay(decoded, true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ZPAnalytics.trackEvent(ZPEvents.SCANQR_PL_NOQRCODE);
                        super.onError(e);
                    }
                });
    }

}
