package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.DefaultPaymentRedirectListener;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;


/**
 * Created by longlv on 13/06/2016.
 * Controller for transfer money
 */
public class TransferPresenter extends AbstractPresenter<ITransferView> {


    private static String mPreviousTransferId = null;

    private TransferObject mTransferObject;

    private final User mUser;
    private final Context applicationContext;

    private final ZaloPayRepository mZaloPayRepository;
    private final FriendStore.Repository mFriendRepository;

    private final TransferStore.Repository mTransferRepository;
    private final TransferNotificationHelper mTransferNotificationHelper;

    private final ZaloSdkApi mZaloSdkApi;
    private final AccountStore.Repository mAccountRepository;

    private PaymentWrapper paymentWrapper;

    private long mMinAmount = 0;
    private long mMaxAmount = 0;


    @Inject
    TransferPresenter(User user, NotificationStore.Repository notificationRepository,
                      BalanceStore.Repository balanceRepository,
                      ZaloPayRepository zaloPayRepository,
                      TransactionStore.Repository transactionRepository,
                      Navigator navigator,
                      TransferStore.Repository transferRepository,
                      Context applicationContext, ZaloSdkApi zaloSdkApi,
                      FriendStore.Repository friendRepository,
                      AccountStore.Repository mAccountRepository
    ) {

        this.mUser = user;
        this.mZaloPayRepository = zaloPayRepository;
        this.mTransferRepository = transferRepository;
        this.applicationContext = applicationContext;
        this.mAccountRepository = mAccountRepository;

        mTransferNotificationHelper = new TransferNotificationHelper(notificationRepository, user);

        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new DefaultPaymentRedirectListener(navigator) {
                    @Override
                    public Object getContext() {
                        Timber.d("Get context : view [%s]", mView);
                        if (mView == null) {
                            return null;
                        }
                        return mView.getFragment();
                    }
                })
                .build();
        this.mZaloSdkApi = zaloSdkApi;
        this.mFriendRepository = friendRepository;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (paymentWrapper == null) {
            return;
        }

        if (requestCode == Constants.REQUEST_CODE_DEPOSIT && mTransferObject.activateSource == Constants.ActivateSource.FromZalo) {
            ZPAnalytics.trackEvent(ZPEvents.ZALO_BACK);
        }

        paymentWrapper.onActivityResult(requestCode, resultCode, data);
    }


    private void getUserInfo(TransferObject object) {
        if (object.isEnoughZalopayInfo()) {
            return;
        }
        Observable<Person> observable = null;
        if (object.zaloId > 0) {
            observable = mFriendRepository.getUserInfo(object.zaloId);
        } else if (!TextUtils.isEmpty(object.zalopayId)) {
            observable = mAccountRepository.getUserInfoByZaloPayId(object.zalopayId);
        } else if (!TextUtils.isEmpty(object.zalopayName)) {
            observable = mAccountRepository.getUserInfoByZaloPayName(object.zalopayName);
        }

        if (observable != null) {
            Subscription subscription = observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ZaloPayUserSubscriber(object.zalopayName));
            mSubscription.add(subscription);
        }
    }

    void setTransferObject(@NonNull TransferObject object) {

        mTransferObject = object;
        mSubscription.clear(); // Xóa các subscription trước đó. (Activity#newIntent)

        Timber.d("transfer object : zalopayid [%s] zaloid [%s] zalopayname [%s] displayname [%s]", object.zalopayId, object.zaloId, object.zalopayName, object.displayName);

        if (mView != null) {
            mView.setTransferInfo(object, !isTransferFixedMoney());
        }

        if (mPreviousTransferId != null && !mPreviousTransferId.equals(mTransferObject.zalopayId)) {
            Timber.d("Change user receiver money");
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_CHANGERECEIVER);
        }

        setActivateSource();
        getUserInfo(object);
        onViewCreated();
    }

    public void onViewCreated() {
        initLimitAmount();
        checkShowButtonTransfer();
        ensureHaveProfile();
    }

    private class ZaloPayUserSubscriber extends DefaultSubscriber<Person> {

        private String toZalopayName;

        ZaloPayUserSubscriber(String toZalopayName) {
            this.toZalopayName = toZalopayName;
        }

        @Override
        public void onStart() {
            showLoading();
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Error!!! Get zalopay info");

            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            if (mView == null) {
                return;
            }

            hideLoading();
            String message = ErrorMessageFactory.create(applicationContext, e);

            if (e instanceof NetworkConnectionException) {
                showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.NO_INTERNET);
                return;
            } else if (e instanceof BodyException
                    && ((BodyException) e).errorCode == NetworkError.ZALOPAYNAME_NOT_EXIST
                    && isTransferFixedMoney()
                    && !TextUtils.isEmpty(toZalopayName)) {
                showDialogThenClose(
                        String.format(mView.getContext().getString(R.string.receiver_invalid), toZalopayName),
                        R.string.txt_close,
                        SweetAlertDialog.ERROR_TYPE);
                return;
            }

            if (mTransferObject.activateSource == Constants.ActivateSource.FromZalo) {
                ZPAnalytics.trackEvent(ZPEvents.ZALO_RECEIVER_NOT_FOUND);
            }

            showDialogThenClose(message, R.string.txt_close, SweetAlertDialog.ERROR_TYPE);
        }

        @Override
        public void onNext(Person person) {
            Timber.d("Get zalopay info success : displayName [%s] avatar [%s] zalopayId [%s]", person.displayName, person.avatar, person.zaloPayId);

            updateTransferObject(person);

            if (mView == null) {
                return;
            }

            if (!TextUtils.isEmpty(person.avatar) || !TextUtils.isEmpty(person.displayName)) { //Vì sandbox có 1 vài user cũ không có zalopay info
                mView.setUserInfo(person);
            }

            hideLoading();
            checkShowButtonTransfer();
        }
    }

    private void updateTransferObject(Person person) {

        mTransferObject.zalopayId = person.zaloPayId;
        mTransferObject.zalopayName = person.zalopayname;

        if (!TextUtils.isEmpty(person.displayName)) {
            mTransferObject.displayName = person.displayName;
        }
        if (!TextUtils.isEmpty(person.avatar)) {
            mTransferObject.avatar = person.avatar;
        }

        mTransferObject.phoneNumber = PhoneUtil.formatPhoneNumber(person.phonenumber);
    }

    private void updateTransferObject(ZPPaymentResult paymentResult) {
        long amount = 0;
        String message = null;

        if (paymentResult != null && paymentResult.paymentInfo != null) {
            amount = paymentResult.paymentInfo.amount;
            message = paymentResult.paymentInfo.description;
        }

        Timber.d("Complete transfer zalo : amount [%s] message [%s]", amount, message);

        if (amount == 0) {
            amount = mView.getAmount();
        }

        if (message == null) {
            message = mView.getMessage();
        }

        mTransferObject.amount = amount;
        mTransferObject.message = message;

    }

    void shouldFinishTransfer() {
        if (mTransferObject.activateSource == Constants.ActivateSource.FromWebApp_QRType2) {
            handleFailedTransferWeb(mView.getActivity(),
                    2, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
        }
    }

    private boolean isTransferFixedMoney() {
        return (mTransferObject.activateSource == Constants.ActivateSource.FromQRCodeType2
                || mTransferObject.activateSource == Constants.ActivateSource.FromWebApp_QRType2);
    }

    void doClickTransfer(long amount) {
        Timber.d("Handle do transfer : amount [%s]", amount);
        doTransfer(amount);
        ZPAnalytics.trackEvent(amount == 0 ? ZPEvents.MONEYTRANSFER_INPUTNODESCRIPTION : ZPEvents.MONEYTRANSFER_INPUTDESCRIPTION);
        ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_TAPCONTINUE);
    }

    private void doTransfer(long amount) {
        if (mView == null) {
            Timber.d("mView is null");
            return;
        }

        if (TextUtils.isEmpty(mTransferObject.zalopayId)) {
            Timber.w("ZalopayId is empty");
            return;
        }

        if (mUser.zaloPayId.equals(mTransferObject.zalopayId)) {
            mView.showError(applicationContext.getString(R.string.exception_transfer_for_self));
            return;
        }

        transferMoney(amount);
    }

    private void transferMoney(long amount) {
        mPreviousTransferId = null;
        Subscription subscription = mZaloPayRepository.createwalletorder(BuildConfig.ZALOPAY_APP_ID,
                amount,
                ETransactionType.WALLET_TRANSFER.toString(),
                "1;" + mTransferObject.zalopayId,
                mView.getMessage(),
                mTransferObject.displayName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CreateWalletOrderSubscriber());

        mSubscription.add(subscription);
    }

    private final class CreateWalletOrderSubscriber extends DefaultSubscriber<Order> {

        @Override
        public void onStart() {
            showLoading();
        }

        @Override
        public void onNext(Order order) {
            TransferPresenter.this.onCreateWalletOrderSuccess(order);
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }
            TransferPresenter.this.onCreateWalletOrderError(e);
        }
    }

    private void onCreateWalletOrderError(Throwable e) {
        Timber.d(e, "Server responses with error");

        if (mView == null) {
            return;
        }

        hideLoading();

        if (e instanceof NetworkConnectionException) {
            if (mTransferObject.activateSource == Constants.ActivateSource.FromWebApp_QRType2) {
                handleFailedTransferWeb(mView.getActivity(),
                        3, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
            }
            mView.showNetworkErrorDialog();
        } else {
            String message = ErrorMessageFactory.create(applicationContext, e);
            if (mTransferObject.activateSource == Constants.ActivateSource.FromWebApp_QRType2) {
                handleFailedTransferWeb(mView.getActivity(), 0, message);
            }
            mView.showError(message);
        }
    }

    private void onCreateWalletOrderSuccess(Order order) {
        Timber.d("On create wallet order success : item [%s]", order.item);
        if (mView == null) {
            return;
        }

        mTransferObject.amount = order.amount;
        mTransferObject.message = order.description;

        paymentWrapper.transfer(mView.getActivity(), order, mTransferObject.displayName, mTransferObject.avatar, mTransferObject.phoneNumber, mTransferObject.zalopayName);
        hideLoading();
    }

    private void saveTransferRecent() {
        int transactionType;

        try {
            transactionType = Integer.valueOf(ETransactionType.WALLET_TRANSFER.toString());
        } catch (NumberFormatException e) {
            Timber.d(e, "Parse transaction type");
            return;
        }

        RecentTransaction recent = transform(mTransferObject);

        if (recent == null) {
            return;
        }

        Subscription subscription = mTransferRepository.append(recent, transactionType)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }


    private RecentTransaction transform(TransferObject object) {
        if (object == null || TextUtils.isEmpty(object.zalopayId) || TextUtils.isEmpty(object.displayName)) {
            return null;
        }

        RecentTransaction recent = new RecentTransaction();
        recent.amount = object.amount;
        recent.message = object.message;

        recent.displayName = object.displayName;
        recent.avatar = object.avatar;
        recent.zaloId = object.zaloId;
        recent.zaloPayId = object.zalopayId;
        recent.zaloPayName = object.zalopayName;
        recent.phoneNumber = object.phoneNumber;
        return recent;
    }

    private void ensureHaveProfile() {
        if (TextUtils.isEmpty(mUser.displayName)) {
            mZaloSdkApi.getProfile();
        }
    }

    private void initLimitAmount() {
        mMinAmount = CShareDataWrapper.getMinTranferValue();
        mMaxAmount = CShareDataWrapper.getMaxTranferValue();

        if (mMinAmount <= 0) {
            mMinAmount = Constants.MIN_TRANSFER_MONEY;
        }

        if (mMaxAmount <= 0) {
            mMaxAmount = Constants.MAX_TRANSFER_MONEY;
        }

        Timber.d("Limit transfer : min [%s] max [%s]", mMinAmount, mMaxAmount);

        // View sẽ sử dụng 2 tham số minAmount và maxAmount trong việc kiểm tra giá trị amount
        // mà người dùng nhập vào có phù hợp hay không
        if (mView != null) {
            mView.setMinMaxMoney(mMinAmount, mMaxAmount);
        }

    }

    private boolean validateDynamicMoney() {
        return mView != null && mView.getAmount() > 0;
    }

    private boolean validateFixedMoney() {
        if (mTransferObject.amount < mMinAmount) {
            String error = String.format(mView.getContext().getString(R.string.min_money),
                    CurrencyUtil.formatCurrency(mMinAmount));
            mView.showErrorTransferFixedMoney(error);
            return false;
        }

        if (mTransferObject.amount > mMaxAmount) {
            String error = String.format(mView.getContext().getString(R.string.max_money),
                    CurrencyUtil.formatCurrency(mMaxAmount));
            mView.showErrorTransferFixedMoney(error);
            return false;
        }

        mView.hideErrorTransferFixedMoney();
        return true;
    }

    private void checkShowButtonTransfer() {
        if (mView == null) {
            return;
        }

        boolean isValidate;

        if (TextUtils.isEmpty(mTransferObject.zalopayId)) {
            isValidate = false;
        } else if (isTransferFixedMoney()) {
            isValidate = validateFixedMoney();
        } else {
            isValidate = validateDynamicMoney();
        }

        Timber.d("enabled button transfer : isValidate [%s]", isValidate);

        mView.setEnabledTransfer(isValidate);
    }

    void navigateBack() {
        if (mView == null) {
            return;
        }

        mPreviousTransferId = mTransferObject.zalopayId;

        switch (mTransferObject.activateSource) {
            case FromZalo:
                Intent data = new Intent();
                data.putExtra("code", 0);  // mUser cancel
                mView.getActivity().setResult(Activity.RESULT_OK, data);
                return;
            case FromWebApp_QRType2:
                handleNavigateBackTransferWeb();
                return;
            case FromQRCodeType1:
                sendNotificationMessage(mTransferObject.zalopayId,
                        Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL, 0, null);
                break;
        }

        Intent intent = new Intent();
        intent.putExtra(Constants.ARG_AMOUNT, mTransferObject.amount);
        intent.putExtra(Constants.ARG_MESSAGE, mTransferObject.message);
        mView.getActivity().setResult(Activity.RESULT_CANCELED, intent);
    }

    private void setActivateSource() {
        switch (mTransferObject.activateSource) {
            case FromQRCodeType1:
                // Send notification to receiver
                sendNotificationMessage(mTransferObject.zalopayId,
                        Constants.MoneyTransfer.STAGE_PRETRANSFER, 0, null);
                break;
            case FromQRCodeType2:
                break;
            case FromWebApp_QRType2:
                break;
        }
    }

    private void showLoading() {
        if (mView == null) {
            return;
        }
        mView.showLoading();
    }

    private void hideLoading() {
        if (mView == null) {
            return;
        }
        mView.hideLoading();
    }

    private void showDialogThenClose(String error, @StringRes int cancelText, int dialogType) {
        if (mView == null) {
            return;
        }
        mView.showDialogThenClose(error, mView.getContext().getString(cancelText), dialogType);
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {

        @Override
        protected ILoadDataView getView() {
            return mView;
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            super.onResponseError(paymentError);

            if (mView == null || mView.getActivity() == null) {
                return;
            }

            if (mTransferObject.activateSource == Constants.ActivateSource.FromZalo) {
                handleFailedTransferZalo(mView.getActivity(), paymentError.value());
            } else if (mTransferObject.activateSource == Constants.ActivateSource.FromWebApp_QRType2) {
                handleFailedTransferWeb(mView.getActivity(),
                        paymentError.value(), PaymentError.getErrorMessage(paymentError));
            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult paymentResult) {
            super.onResponseSuccess(paymentResult);

            if (mView == null || mView.getActivity() == null) {
                return;
            }

            updateTransferObject(paymentResult);

            if (mTransferObject.activateSource == Constants.ActivateSource.FromZalo) {
                handleCompletedTransferZalo(mView.getActivity());
            } else if (mTransferObject.activateSource == Constants.ActivateSource.FromWebApp_QRType2) {
                handleCompletedTransferWeb(mView.getActivity());
            } else {
                mView.getActivity().setResult(Activity.RESULT_OK);
                mView.getActivity().finish();
            }

            saveTransferRecent();
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {
            Timber.d("Transaction is completed : success [%s] transId [%s]", isSuccessful, transId);
            super.onPreComplete(isSuccessful, transId, pAppTransId);
            transactionId = transId;
            if (mTransferObject.activateSource == Constants.ActivateSource.FromQRCodeType1) {
                if (isSuccessful) {
                    sendNotificationMessage(mTransferObject.zalopayId,
                            Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED, mTransferObject.amount, transId);
                } else {
                    sendNotificationMessage(mTransferObject.zalopayId,
                            Constants.MoneyTransfer.STAGE_TRANSFER_FAILED, 0, null);
                }
            }
        }


        private void handleFailedTransferZalo(Activity activity, int code) {
            Timber.d("Transfer zalo failed : code [%s]", code);
            Intent data = new Intent();
            data.putExtra("code", code);
            activity.setResult(Activity.RESULT_OK, data);
            if (code != PaymentError.ERR_CODE_USER_CANCEL.value()) {
                activity.finish();
            }
        }

        private void handleCompletedTransferZalo(Activity activity) {
            Intent data = new Intent();
            data.putExtra("code", 1);
            data.putExtra("amount", mTransferObject.amount);
            data.putExtra("message", mTransferObject.message);
            data.putExtra("transactionId", transactionId == null ? "" : transactionId);
            ZPAnalytics.trackEvent(ZPEvents.ZALO_PAYMENT_COMPLETED);
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }

        private String transactionId;
    }

    private void sendNotificationMessage(String toZaloPayId, int stage, long amount, String transId) {
        mSubscription.add(mTransferNotificationHelper.sendNotificationMessage(
                toZaloPayId, stage, amount, transId));
    }

    private void handleNavigateBackTransferWeb() {
        if (mTransferObject.amount < mMinAmount || mTransferObject.amount > mMaxAmount) {
            handleFailedTransferWeb(mView.getActivity(),
                    2, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
        } else {
            handleFailedTransferWeb(mView.getActivity(),
                    4, PaymentError.getErrorMessage(PaymentError.ERR_CODE_USER_CANCEL));
        }
    }

    private void handleCompletedTransferWeb(Activity activity) {
        Intent data = new Intent();
        data.putExtra("code", 1);
        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }

    private void handleFailedTransferWeb(Activity activity, int code, String param) {
        Timber.d("Handle failed transfer web : code [%s] param [%s]", code, param);
        Intent data = new Intent();
        data.putExtra("code", code);
        data.putExtra("param", param);
        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }
}
