package vn.com.vng.zalopay.data.balance;

import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BalanceResponse;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.ws.payment.request.BalanceCallback;
import vn.com.vng.zalopay.data.ws.payment.request.PaymentRequest;
import vn.com.vng.zalopay.data.ws.payment.request.PaymentRequestService;
import vn.com.vng.zalopay.domain.model.User;

import static vn.com.vng.zalopay.data.util.BusComponent.APP_SUBJECT;
import static vn.com.vng.zalopay.data.ws.payment.request.PaymentConnectorFactory.createBalanceRequest;

/**
 * Created by huuhoa on 6/15/16.
 * Implementation of @link{BalanceStore.Repository}
 */
public class BalanceRepository implements BalanceStore.Repository {
    private final BalanceStore.LocalStorage mLocalStorage;
    private final BalanceStore.RequestService mRequestService;
    private User mUser;
    private EventBus mEventBus;
    private Long mCurrentBalance;

    private PaymentRequestService mPaymentService;

    public BalanceRepository(BalanceStore.LocalStorage localStorage,
                             BalanceStore.RequestService requestService,
                             User user,
                             EventBus eventBus, PaymentRequestService paymentRequestService) {
        mUser = user;
        mLocalStorage = localStorage;
        mRequestService = requestService;
        mEventBus = eventBus;
        mPaymentService = paymentRequestService;
        Timber.d("accessToken[%s]", mUser.accesstoken);
    }

    @Override
    public Observable<Long> balance() {
        return Observable.merge(
                balanceLocal(),
                updateBalance().onErrorResumeNext(throwable -> Observable.empty())
        );
    }

    @Override
    public Observable<Long> updateBalance() {
        return mRequestService.balance(mUser.zaloPayId, mUser.accesstoken)
                .doOnNext(response -> {
                    mCurrentBalance = response.zpwbalance;
                    mLocalStorage.putBalance(response.zpwbalance);
                    mEventBus.post(new ChangeBalanceEvent(response.zpwbalance));
                    //BusComponent.publish(APP_SUBJECT, new ChangeBalanceEvent(response.zpwbalance));
                })
                .map(response -> response.zpwbalance);
    }

    public Long currentBalance() {
        if (mCurrentBalance == null) {
            mCurrentBalance = 0L;
        }
        return mCurrentBalance;
    }

    public Observable<Long> balanceLocal() {
        return ObservableHelper.makeObservable(() -> {
            mCurrentBalance = mLocalStorage.getBalance();
            return mCurrentBalance;
        });
    }

    @Override
    public Observable<Long> fetchBalance() {
        return updateBalance().onErrorResumeNext(throwable -> balanceLocal());
    }

    @Override
    public Observable<Void> fetchBalancePayment() {
        return ObservableHelper.makeObservable(() -> {
            mPaymentService.request(balanceRequest(), new BalanceCallback() {
                @Override
                public Long doBackground(@NonNull BalanceResponse response) {
                    Timber.d("doBackground: %s",Thread.currentThread().getName());
                    mCurrentBalance = response.zpwbalance;
                    mLocalStorage.putBalance(response.zpwbalance);
                    BusComponent.publish(APP_SUBJECT, new ChangeBalanceEvent(response.zpwbalance));
                    return response.zpwbalance;
                }
            });
            return null;
        });
    }

    private PaymentRequest balanceRequest() {
        PaymentRequest.Builder builder = new PaymentRequest.Builder()
                .domain("sandbox.zalopay.com.vn")
                .path(Constants.TPE_API.GETBALANCE)
                .method("GET")
                .user(mUser);
        return builder.build();
    }
}
