package vn.com.vng.zalopay.webapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.ZPTransfer;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.ui.presenter.AbstractPaymentPresenter;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 2/9/17.
 * *
 */

class WebAppPresenter extends AbstractPaymentPresenter<IWebAppView> {

    private IPaymentListener mResponseListener;
    private AccountStore.Repository mAccountRepository;

    private ZPTransfer mZPTransfer;

    @Inject
    WebAppPresenter(BalanceStore.Repository balanceRepository,
                    ZaloPayRepository zaloPayRepository,
                    TransactionStore.Repository transactionRepository,
                    AccountStore.Repository accountRepository,
                    Navigator navigator) {
        super(balanceRepository, zaloPayRepository, transactionRepository, navigator);
        this.mAccountRepository = accountRepository;
    }

    public void pay(JSONObject data, IPaymentListener listener) {
        if (data == null) {
            Timber.i("Pay fail because json is null.");
            return;
        }
        mResponseListener = listener;
        Timber.d("start to process paying order: %s", data.toString());
        if (!NetworkHelper.isNetworkAvailable(mView.getContext())) {
            listener.onPayError(3, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
            return;
        }

        try {
            showLoadingView();
            if (zpTransaction(data)) {
                hideLoadingView();
                return;
            }

            if (orderTransaction(data)) {
                hideLoadingView();
                return;
            }

            hideLoadingView();
            listener.onPayError(2, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
        } catch (IllegalArgumentException e) {
            Timber.i("Invalid JSON input: %s", e.getMessage());
        }
    }

    void transferMoney(JSONObject data, IPaymentListener listener) {
        if (data == null) {
            Timber.i("Pay fail because json is null.");
            return;
        }
        mResponseListener = listener;
        Timber.d("start to process transfer money: %s", data.toString());
        if (!NetworkHelper.isNetworkAvailable(mView.getContext())) {
            listener.onPayError(3, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
            return;
        }

        try {
            showLoadingView();
            if (zpTransfer(data)) {
                hideLoadingView();
                return;
            }

            hideLoadingView();
            listener.onPayError(2, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
        } catch (IllegalArgumentException e) {
            Timber.i("Invalid JSON input: %s", e.getMessage());
        }
    }

    private boolean zpTransfer(JSONObject jsonObject) {
        mZPTransfer = new ZPTransfer(jsonObject);
        boolean isValidZPTransfer = mZPTransfer.isValid();

        Timber.d("Trying with zptransfermoney [%s] activity [%s]", isValidZPTransfer, getActivity());
        if (isValidZPTransfer) {
            getUserInfo(mZPTransfer.zpid);
        }
        return isValidZPTransfer;
    }

    private void getUserInfo(String zpName) {
        showLoadingView();
        Subscription subscription = mAccountRepository.getUserInfoByZaloPayName(zpName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new WebAppPresenter.UserInfoSubscriber(zpName));
        mSubscription.add(subscription);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bundle result = data.getExtras();
            int code = result.getInt("code");
            String param = result.getString("param");
            if (code == 1) {
                if(mResponseListener != null) {
                    mResponseListener.onPaySuccess();
                }
            } else {
                if(mResponseListener != null) {
                    code = Arrays.asList(mZPTransfer.errorCodeList).contains(code) ? code : 5;
                    mResponseListener.onPayError(code, param);
                }
            }
        }
    }

    @Override
    public void onPayParameterError(String param) {
        if (mResponseListener != null) {
            mResponseListener.onPayError(param);
        }
    }

    @Override
    public void onPayResponseError(PaymentError paymentError) {
        if (mResponseListener != null) {
            if (paymentError == PaymentError.ERR_CODE_USER_CANCEL) {
                mResponseListener.onPayError(4, PaymentError.getErrorMessage(paymentError));
            } else {
                mResponseListener.onPayError(PaymentError.getErrorMessage(paymentError));
            }
        }
    }

    @Override
    public void onPayResponseSuccess(ZPPaymentResult zpPaymentResult) {
        if (mResponseListener != null) {
            mResponseListener.onPaySuccess();
        }
    }

    @Override
    public void onPayAppError(String msg) {
        if (mResponseListener != null) {
            mResponseListener.onPayError(msg);
        }
    }

    private void onGetProfileSuccess(Person person, String zaloPayName) {
        Timber.d("Got profile for %s: %s", zaloPayName, person);
        RecentTransaction item = new RecentTransaction();
        item.avatar = person.avatar;
        item.zaloPayId = person.zaloPayId;
        item.displayName = person.displayName;
        item.phoneNumber = String.valueOf(person.phonenumber);
        item.zaloPayName = zaloPayName;
        item.amount = mZPTransfer.amount;
        item.message = mZPTransfer.message;

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
        bundle.putInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_WEB);
        mNavigator.startTransferActivity(getFragment(), bundle);
    }

    private class UserInfoSubscriber extends DefaultSubscriber<Person> {

        String zaloPayName;

        UserInfoSubscriber(String zaloPayName) {
            this.zaloPayName = zaloPayName;
        }

        @Override
        public void onError(Throwable e) {
            hideLoadingView();
            String message = ErrorMessageFactory.create(getFragment().getContext(), e);
            mResponseListener.onPayError(2, message);
        }

        @Override
        public void onNext(Person person) {
            hideLoadingView();
            onGetProfileSuccess(person, zaloPayName);
        }
    }
}
