package vn.com.vng.zalopay.account.ui.presenter;

import android.content.Context;
import android.net.Uri;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IUpdateProfile3View;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ProfileInfo3;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

import static vn.com.vng.zalopay.utils.PhotoUtil.resizeImageByteArray;

/**
 * Created by AnhHieu on 7/1/16.
 * *
 */
public class UpdateProfile3Presenter extends AbstractPresenter<IUpdateProfile3View> {

    private AccountStore.Repository mAccountRepository;
    private Context mApplicationContext;

    @Inject
    UpdateProfile3Presenter(AccountStore.Repository accountRepository, Context applicationContext) {
        this.mAccountRepository = accountRepository;
        this.mApplicationContext = applicationContext;
    }

    public void updateProfile3(final String identityNumber,
                               final String email,
                               final Uri fimgPath,
                               final Uri bimgPath,
                               final Uri avatarPath) {
        update(identityNumber, email, fimgPath, bimgPath, avatarPath);
    }

    private Observable<byte[]> resizeImage(final Uri imgPath) {
        Timber.d("resizeImage path[%s]", imgPath);
        return ObservableHelper.makeObservable(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return resizeImageByteArray(mApplicationContext, imgPath);
            }
        }).flatMap(new Func1<byte[], Observable<byte[]>>() {
            @Override
            public Observable<byte[]> call(byte[] bytes) {
                if (bytes == null) {
                    return Observable.error(new NullPointerException());
                } else {
                    return Observable.just(bytes);
                }
            }
        });
    }

    private void update(final String identityNumber,
                        final String email,
                        final Uri fimgPath,
                        final Uri bimgPath,
                        final Uri avatarPath) {
        Observable<byte[]> obFimgBytes = resizeImage(fimgPath);
        Observable<byte[]> obBimgBytes = resizeImage(bimgPath);
        Observable<byte[]> obAvatarBytes = resizeImage(avatarPath);

        Observable.zip(obFimgBytes, obBimgBytes, obAvatarBytes, new Func3<byte[], byte[], byte[], List<byte[]>>() {
            @Override
            public List<byte[]> call(byte[] _fimgBytes, byte[] _bimgBytes, byte[] _avatarBytes) {
                return Arrays.asList(_fimgBytes, _bimgBytes, _avatarBytes);
            }
        }).flatMap(new Func1<List<byte[]>, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(List<byte[]> bytes) {
                Timber.d("update fimg[%s] bimg[%s] aimg[%s]",
                        bytes.get(0).length,
                        bytes.get(1).length,
                        bytes.get(2).length);
                return mAccountRepository.updateUserProfileLevel3(identityNumber, email,
                        bytes.get(0),
                        bytes.get(1),
                        bytes.get(2));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpdateSubscriber());
    }

    private void onUpdateSuccess() {
        if (mView != null) {
            mView.hideLoading();
            mView.updateSuccess();
        }
    }

    private void onUpdateError(Throwable e) {
        if (mView == null) {
            return;
        }

        mView.hideLoading();
        String message = ErrorMessageFactory.create(mApplicationContext, e);
        mView.showError(message);

        if (e instanceof BodyException) {
            if (((BodyException) e).errorCode == NetworkError.WAITING_APPROVE_PROFILE_LEVEL_3) {
                mView.finish();
            }
        }

    }

    private final class UpdateSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onStart() {
            if (mView != null) {
                mView.showLoading();
            }
        }

        @Override
        public void onNext(Boolean aBoolean) {
            if (aBoolean) {
                UpdateProfile3Presenter.this.onUpdateSuccess();
            } else {
                UpdateProfile3Presenter.this.onUpdateError(null);
            }
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }
            UpdateProfile3Presenter.this.onUpdateError(e);
        }
    }

    public void saveProfileInfo3(String email, String identity, Uri foregroundImg, Uri backgroundImg, Uri avatarImg) {

        String foregroundImgPath = null;
        if (foregroundImg != null) {
            foregroundImgPath = foregroundImg.toString();
        }


        String backgroundImgPath = null;
        if (backgroundImg != null) {
            backgroundImgPath = backgroundImg.toString();
        }

        String avatarImgPath = null;
        if (avatarImg != null) {
            avatarImgPath = avatarImg.toString();
        }

        Subscription subscription = mAccountRepository.saveProfileInfo3(email, identity, foregroundImgPath, backgroundImgPath, avatarImgPath)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    public void getProfileInfo() {
        Subscription subscription = mAccountRepository.getProfileInfo3Cache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProfileInfo3Subscriber());
        mSubscription.add(subscription);
    }

    private class ProfileInfo3Subscriber extends DefaultSubscriber<ProfileInfo3> {

        @Override
        public void onNext(ProfileInfo3 profile) {

            Timber.d("onNext: email [%s] cmnd [%s] avatar %s", profile.email, profile.identity, profile.avatarImg);

            mView.setProfileInfo(profile.email, profile.identity, profile.foregroundImg, profile.backgroundImg, profile.avatarImg);
        }
    }
}
