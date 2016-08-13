package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import java.security.MessageDigest;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IPinProfileView;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 25/05/2016.
 *
 */
public class PinProfilePresenter extends BaseUserPresenter implements IPresenter<IPinProfileView> {

    IPinProfileView mView;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public PinProfilePresenter() {
    }

    @Override
    public void setView(IPinProfileView iProfileView) {
        mView = iProfileView;
    }

    @Override
    public void destroyView() {
        hideLoading();
        this.mView = null;
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(mCompositeSubscription);
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
            Timber.e(ex, "exception sha256");
            return "";
        }
    }

    public void updateProfile(String pin, String phone, String zalopayName) {
        showLoading();
        String pinSha256 = sha256(pin);

        Subscription subscriptionLogin = accountRepository.updateUserProfileLevel2(pinSha256, phone, zalopayName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpdateProfileSubscriber(phone));
        mCompositeSubscription.add(subscriptionLogin);
    }

    public void checkZaloPayName(String zaloPayName) {
        if (TextUtils.isEmpty(zaloPayName)) {
            return;
        }
        Subscription subscription = accountRepository.checkZaloPayNameExist(zaloPayName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetUserInfoByZPNameSubscriber());
        mCompositeSubscription.add(subscription);
    }

    private final class UpdateProfileSubscriber extends DefaultSubscriber<Boolean> {
        private String phone;

        public UpdateProfileSubscriber(String phone) {
            this.phone = phone;
        }

        @Override
        public void onNext(Boolean result) {
            Timber.d("updateProfile success " + result);
            PinProfilePresenter.this.onUpdateProfileSuccess(phone);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            Timber.e(e, "update Profile Subscriber onError [%s]", e.getMessage());
            if (e instanceof BodyException) {
                if (((BodyException)e).errorCode == NetworkError.USE_EXISTED) {
                    mView.onCheckFail();
                } else {
                    PinProfilePresenter.this.onUpdateProfileError(e.getMessage());
                }
            } else {
                PinProfilePresenter.this.onUpdateProfileError("Cập nhật thông tin người dùng thất bại.");
            }
        }
    }

    private void onUpdateProfileError(String error) {
        hideLoading();
        mView.showError(error);
    }

    private void onUpdateProfileSuccess(String phone) {
        hideLoading();
        mView.updateProfileSuccess(phone);
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

    private class GetUserInfoByZPNameSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            if (e instanceof BodyException) {
                mView.onCheckFail();
            } else {
                mView.showError("Lỗi xảy ra trong quá trình kiểm tra tên tài khoản Zalo Pay.\nVui lòng thử lại.");
            }
        }

        @Override
        public void onNext(Boolean isValid) {
            if (isValid) {
                mView.onCheckSuccess();
            } else {
                mView.onCheckFail();
            }
        }
    }
}
