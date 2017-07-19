package vn.com.vng.zalopay.transfer.ui;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

/**
 * Created by AnhHieu on 8/15/16.
 * *
 */
final class TransferHomePresenter extends AbstractPresenter<ITransferHomeView> {

    private final TransferStore.Repository mTransferRepository;

    @Inject
    TransferHomePresenter(TransferStore.Repository transferRepository) {
        this.mTransferRepository = transferRepository;
    }

    @Override
    public void resume() {
        this.getRecent();
    }

    private void getRecent() {
        Subscription subscription = mTransferRepository.getRecent()
                .doOnError(Timber::d)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RecentSubscriber());
        mSubscription.add(subscription);
    }

    private class RecentSubscriber extends DefaultSubscriber<List<RecentTransaction>> {

        RecentSubscriber() {
        }

        @Override
        public void onNext(List<RecentTransaction> recentTransactions) {
            Timber.d("Get list recent success : size [%s]", recentTransactions.size());
            if (mView != null) {
                mView.setData(recentTransactions);
            }
        }
    }

    private Observable<Drawable> getDrawableFromFile(String fileName) {
        return ObservableHelper.makeObservable(() -> {
            if (TextUtils.isEmpty(fileName) || mView == null || mView.getContext() == null) {
                return null;
            }

            return Drawable.createFromPath(ResourceHelper.getResource(mView.getContext(),
                    BuildConfig.ZALOPAY_APP_ID, fileName));
        });
    }

    void loadAnimationFromResource() {
        if (mView == null || mView.getContext() == null) {
            return;
        }

        try {
            Observable.zip(getDrawableFromFile(mView.getContext().getString(R.string.ic_chuyentien_ani_1)),
                    getDrawableFromFile(mView.getContext().getString(R.string.ic_chuyentien_ani_2)),
                    getDrawableFromFile(mView.getContext().getString(R.string.ic_chuyentien_ani_3)),
                    getDrawableFromFile(mView.getContext().getString(R.string.ic_chuyentien_ani_4)),
                    (frame1, frame2, frame3, frame4) -> {
                        AnimationDrawable animationDrawable = new AnimationDrawable();
                        if (frame1 == null || frame2 == null || frame3 == null || frame4 == null) {
                            return null;
                        }
                        animationDrawable.addFrame(frame1, 1000);
                        animationDrawable.addFrame(frame2, 100);
                        animationDrawable.addFrame(frame3, 100);
                        animationDrawable.addFrame(frame4, 2000);
                        return animationDrawable;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(animationDrawable -> {
                        if (animationDrawable == null) {
                            return;
                        }
                        animationDrawable.setOneShot(false);
                        if (mView != null) {
                            mView.setIntroductionAnimation(animationDrawable);
                        }
                        animationDrawable.start();
                        Timber.d("Load & start animation from resource successfully.");
                    });
        } catch (Exception e) {
            Timber.e(e, "Load animation from resource throw exception.");
        }
    }
}
