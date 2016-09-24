package vn.com.vng.zalopay.account.ui.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.account.ui.view.IUpdateProfile3View;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ProfileInfo3;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

import static vn.com.vng.zalopay.utils.PhotoUtil.resizeImageByteArray;

/**
 * Created by AnhHieu on 7/1/16.
 * *
 */
public class UpdateProfile3Presenter extends BaseUserPresenter implements IPresenter<IUpdateProfile3View> {

    private IUpdateProfile3View mView;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private AccountStore.Repository mAccountRepository;
    private Context mApplicationContext;

    @Inject
    public UpdateProfile3Presenter(AccountStore.Repository accountRepository, Context applicationContext) {
        this.mAccountRepository = accountRepository;
        this.mApplicationContext = applicationContext;
    }

    @Override
    public void setView(IUpdateProfile3View iUpdateProfile3View) {
        mView = iUpdateProfile3View;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
    }

    public void updateProfile3(final String identityNumber,
                               final String email,
                               final Uri fimgPath,
                               final Uri bimgPath,
                               final Uri avatarPath) {

        mView.showLoading();
        AndroidApplication.instance().getAppComponent().threadExecutor()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        update(identityNumber, email, fimgPath, bimgPath, avatarPath);
                    }
                });
    }


    private void update(String identityNumber,
                        String email,
                        Uri fimgPath,
                        Uri bimgPath,
                        Uri avatarPath) {

        byte[] _fimgBytes = resizeImageByteArray(mApplicationContext, fimgPath);
        byte[] _bimgBytes = resizeImageByteArray(mApplicationContext, bimgPath);
        byte[] _avatarBytes = resizeImageByteArray(mApplicationContext, avatarPath);

        if (_fimgBytes != null && _bimgBytes != null && _avatarBytes != null) {
            Timber.d(" _fimg %s _bimg %s avatar %s", _fimgBytes.length, _bimgBytes.length, _avatarBytes.length);
            Subscription subscription = mAccountRepository.updateUserProfileLevel3(identityNumber, email,
                    _fimgBytes,
                    _bimgBytes,
                    _avatarBytes)

                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new UpdateSubscriber());
            compositeSubscription.add(subscription);
        } else {
            Observable.just(Boolean.FALSE)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new UpdateSubscriber());
        }
    }

    private void onUpdateSuccess() {
        mView.hideLoading();
        mView.updateSuccess();
    }

    private void onUpdateError(Throwable e) {
        if (e instanceof BodyException) {
            if (((BodyException) e).errorCode == NetworkError.WAITING_APPROVE_PROFILE_LEVEL_3) {
                mView.showError(ErrorMessageFactory.create(mApplicationContext, e));
                mView.waitingApproveProfileLevel3();
                return;
            }
        }
        mView.hideLoading();
        String message = ErrorMessageFactory.create(mApplicationContext, e);
        mView.showError(message);
    }

    private final class UpdateSubscriber extends DefaultSubscriber<Boolean> {
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

        mAccountRepository.saveProfileInfo3(email, identity, foregroundImgPath, backgroundImgPath, avatarImgPath)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    public void getProfileInfo() {
        Subscription subscription = mAccountRepository.getProfileInfo3Cache()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProfileInfo3Subscriber());
        compositeSubscription.add(subscription);
    }

    private class ProfileInfo3Subscriber extends DefaultSubscriber<ProfileInfo3> {

        @Override
        public void onNext(ProfileInfo3 profile) {

            Timber.d("onNext: email [%s] cmnd [%s] avatar %s", profile.email, profile.identity, profile.avatarImg);

            mView.setProfileInfo(profile.email, profile.identity, profile.foregroundImg, profile.backgroundImg, profile.avatarImg);
        }
    }
}
