package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import java.security.MessageDigest;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IPinProfileView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 25/05/2016.
 */
public class PinProfilePresenter extends BaseUserPresenter implements IPresenter<IPinProfileView> {

    IPinProfileView mView;
    private Subscription subscriptionLogin;
    private UserConfig mUserConfig;

    public PinProfilePresenter(UserConfig userConfig) {
        mUserConfig = userConfig;
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
        unsubscribeIfNotNull(subscriptionLogin);
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
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            Timber.e(ex, "exception sha256");
            return "";
        }
    }

    public void updateProfile(String pin, String phone) {
        showLoading();
        String pinSha256 = sha256(pin);

        subscriptionLogin = accountRepository.updateUserProfileLevel2(pinSha256, phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new updateProfileSubscriber(phone));
    }

    private final class updateProfileSubscriber extends DefaultSubscriber<Boolean> {
        private String phone;

        public updateProfileSubscriber(String phone) {
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

            Timber.e(e, "onError " + e);
            PinProfilePresenter.this.onUpdateProfileError(e);
        }
    }

    private void onUpdateProfileError(Throwable e) {
        hideLoading();
        mView.showError("Cập nhật thông tin người dùng thất bại.");
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
}
