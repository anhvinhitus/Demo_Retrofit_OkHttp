package vn.com.vng.zalopay.game;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import org.parceler.Parcels;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.zalopay.game.businnesslogic.entity.base.AppGameError;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;
import vn.com.zalopay.game.businnesslogic.interfaces.callback.IAppGameResultListener;
import vn.com.zalopay.game.businnesslogic.interfaces.payment.IPaymentCallback;
import vn.com.zalopay.game.businnesslogic.interfaces.payment.IPaymentService;
import vn.com.zalopay.game.controller.AppGameController;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 07/09/2016.
 *
 */
public class AppGamePaymentImpl implements IPaymentService {

    protected ZaloPayRepository zaloPayRepository = AndroidApplication.instance().getUserComponent().zaloPayRepository();

    protected AccountStore.Repository accountRepository = AndroidApplication.instance().getUserComponent().accountRepository();

    protected TransactionStore.Repository transactionRepository = AndroidApplication.instance().getUserComponent().transactionRepository();

    protected BalanceStore.Repository balanceRepository = AndroidApplication.instance().getUserComponent().balanceRepository();

    @Inject
    Navigator navigator;

    @Override
    public void pay(final Activity activity, Bundle bundle, final IPaymentCallback paymentCallback) {
        if (activity == null || bundle == null) {
            return;
        }
        String accesstoken = bundle.getString("accesstoken");
        String appid = bundle.getString("appid");
        String apptransid = bundle.getString("apptransid");
        String appuser = bundle.getString("appuser");
        String apptime = bundle.getString("apptime");
        String item = bundle.getString("item");
        String description = bundle.getString("description");
        String embeddata = bundle.getString("embeddata");
        String amount = bundle.getString("amount");
        String mac = bundle.getString("mac");

        final AppGamePayInfo appGamePayInfo = Parcels.unwrap(bundle.getParcelable("AppGamePayInfo"));
        final Order order = new Order(Long.valueOf(appid), accesstoken, apptransid, appuser, Long.valueOf(apptime),
                embeddata, item, Long.parseLong(amount), description, null, mac);

        PaymentWrapper paymentWrapper = new PaymentWrapper(balanceRepository, zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return activity;
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                Timber.d("onParameterError");

                if (activity == null) {
                    return;
                }

                if ("order".equalsIgnoreCase(param)) {
                    showError(activity.getString(R.string.order_invalid));
                } else if ("uid".equalsIgnoreCase(param)) {
                    showError(activity.getString(R.string.user_invalid));
                } else if ("token".equalsIgnoreCase(param)) {
                    showError(activity.getString(R.string.order_invalid));
                }
            }

            @Override
            public void onPreComplete(boolean isSuccessful,String transId, String pAppTransId) {
                Timber.d("onPreComplete paymentCallback [%s] appTransID [%s]", paymentCallback, pAppTransId);

            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                Timber.d("onResponseError");
                if (activity == null) {
                    return;
                }

                if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                    showError(activity.getString(R.string.exception_no_connection_try_again));
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                Timber.d("onResponseSuccess paymentCallback [%s]", paymentCallback);
                if (paymentCallback == null) {
                    return;
                }

                AppGamePayInfo appGamePayInfo2 = appGamePayInfo;
                appGamePayInfo2.setApptransid(zpPaymentResult.paymentInfo.appTransID);

                paymentCallback.onResponseSuccess(appGamePayInfo2);
            }

            @Override
            public void onResponseTokenInvalid() {
                Timber.d("onResponseTokenInvalid");
            }

            @Override
            public void onAppError(String msg) {
                Timber.d("onAppError msg [%s]", msg);
                    showError(activity.getString(R.string.exception_generic));
            }

            @Override
            public void onNotEnoughMoney() {
                Timber.d("onNotEnoughMoney");
                navigator.startDepositActivity(activity);
            }

            private void showError(String text) {
                if (activity == null || TextUtils.isEmpty(text)) {
                    return;
                }
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }

            private void onSessionExpired() {

            }
        });

        paymentWrapper.payWithOrder(order);
    }

    @Override
    public void destroyVariable() {

    }

}
