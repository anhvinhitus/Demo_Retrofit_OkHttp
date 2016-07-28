package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import java.security.MessageDigest;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IRecoveryPinView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 25/05/2016.
 */
public class RecoveryPinPresenter extends BaseUserPresenter implements IPresenter<IRecoveryPinView> {

    IRecoveryPinView mView;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void setView(IRecoveryPinView iProfileView) {
        mView = iProfileView;
    }

    @Override
    public void destroyView() {
        hideLoading();
        this.mView = null;
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(compositeSubscription);
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        this.unsubscribe();
    }

    public String sha256(String base) {
        if (TextUtils.isEmpty(base)) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void updateProfile(String pin, String phone) {
        showLoading();
        String pinSha256 = sha256(pin);

        Subscription subscription = accountRepository.recoveryPin(pinSha256, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RecoveryPassCodeSubscriber());
        compositeSubscription.add(subscription);
    }

    private final class RecoveryPassCodeSubscriber extends DefaultSubscriber<BaseResponse> {
        public RecoveryPassCodeSubscriber() {
        }

        @Override
        public void onNext(BaseResponse baseResponse) {
            RecoveryPinPresenter.this.onRecoveryPinSuccess();
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            if (e instanceof BodyException) {
                BodyException bodyException = (BodyException) e;
                RecoveryPinPresenter.this.onRecoveryPinError(bodyException.getMessage());
                return;
            }

            Timber.e(e, "onError " + e);
            RecoveryPinPresenter.this.onRecoveryPinError();
        }
    }

    private void onRecoveryPinError() {
        hideLoading();
        mView.showError("Thiết lập lại mã PIN thất bại.");
    }

    private void onRecoveryPinError(String msg) {
        hideLoading();
        mView.showError(msg);
    }

    private void onRecoveryPinSuccess() {
        hideLoading();
        mView.onRecoveryPinSuccess();
    }

    public void showLoading() {
        mView.showLoading();
    }

    public void hideLoading() {
        mView.hideLoading();
    }

    public void showRetry() {
        mView.showRetry();
    }

    public void hideRetry() {
        mView.hideRetry();
    }
}
