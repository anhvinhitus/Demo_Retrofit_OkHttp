package vn.com.vng.zalopay.account.ui.presenter;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.nio.ByteBuffer;

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
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ProfileInfo3;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by AnhHieu on 7/1/16.
 */
public class UpdateProfile3Presenter extends BaseUserPresenter implements IPresenter<IUpdateProfile3View> {

    IUpdateProfile3View mView;
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    public UpdateProfile3Presenter() {
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

    public void update(String identityNumber,
                       String email,
                       String fimgPath,
                       String bimgPath,
                       String avatarPath) {

        Timber.d("identityNumber %s email %s fimgPath %s bimgPath %s avatarPath %s", identityNumber, email, fimgPath, bimgPath, avatarPath);

        mView.showLoading();
        Subscription subscription = accountRepository.updateUserProfileLevel3(identityNumber, email, fimgPath, bimgPath, avatarPath)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpdateSubscriber());
        compositeSubscription.add(subscription);
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

        byte[] _fimgBytes = resizeImageByteArray(fimgPath);
        byte[] _bimgBytes = resizeImageByteArray(bimgPath);
        byte[] _avatarBytes = resizeImageByteArray(avatarPath);

        if (_fimgBytes != null && _bimgBytes != null && _avatarBytes != null) {
            Timber.d(" _fimg %s _bimg %s avatar %s", _fimgBytes.length, _bimgBytes.length, _avatarBytes.length);
            Subscription subscription = accountRepository.updateUserProfileLevel3(identityNumber, email,
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
                userConfig.setWaitingApproveProfileLevel3(true);
                mView.showError(((BodyException) e).message);
                mView.waitingApproveProfileLevel3();
                return;
            }
        }
        mView.hideLoading();
        String message = ErrorMessageFactory.create(applicationContext, e);
        mView.showError(message);
    }

    private final class UpdateSubscriber extends DefaultSubscriber<Boolean> {
        public UpdateSubscriber() {
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

/*

    protected int byteSizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return data.getByteCount();
        } else {
            return data.getAllocationByteCount();
        }
    }


    protected byte[] bitmap2byteArray(Bitmap b) {
        int bytes = byteSizeOf(b);
        //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
        //int bytes = b.getWidth()*b.getHeight()*4;

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        b.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        //  Timber.d("bytes %s", bytes);

        byte[] array = buffer.array();
        return array;
    }

    private Bitmap resizeImage(Uri uri) throws Exception {

        Bitmap bitmap = Glide.with(applicationContext).loadFromMediaStore(uri)
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .fitCenter()
                .override(480, 480)
                .fitCenter()
                .into(480, 480)
                .get();

        Timber.d("bitmap width %s height %s ", bitmap.getWidth(), bitmap.getHeight());
        resizeImageByteArray(uri);
        return bitmap;
    }
*/

    private byte[] resizeImageByteArray(Uri uri) {
        byte[] ret = null;
        try {
            ret = Glide.with(applicationContext).loadFromMediaStore(uri)
                    .asBitmap()
                    .toBytes(Bitmap.CompressFormat.JPEG, 100)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .fitCenter()
                    .override(480, 480)
                    .fitCenter()
                    .into(480, 480)
                    .get();

        } catch (Exception ex) {
            Timber.w(ex, "exception resize");
        }

        return ret;
    }

/*    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    private byte[] getByteArrayFromUri(Uri uri) {
        byte[] ret = null;
        Bitmap bitmap = null;
        try {
            bitmap = resizeImage(uri);
            ret = bitmap2byteArray(bitmap);
        } catch (Exception ex) {
            Timber.w(ex, "exception resize");
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        return ret;
    }*/

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

        accountRepository.saveProfileInfo3(email, identity, foregroundImgPath, backgroundImgPath, avatarImgPath)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    public void getProfileInfo() {
        Subscription subscription = accountRepository.getProfileInfo3Cache()
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
