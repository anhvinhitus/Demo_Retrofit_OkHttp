package vn.com.vng.zalopay.ui.presenter;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ObservableHelper;

/**
 * Created by huuhoa on 12/11/16.
 * Based presenter for implement common behaviours
 */

public abstract class AbstractPresenter<View> implements IPresenter<View> {
    protected CompositeSubscription mSubscription = new CompositeSubscription();
    protected View mView;


    /**
     * Call to attach a view to presenter.
     * <p>
     * The best time to call this function is when a view is created by Android framework
     */
    @Override
    public void attachView(View view) {
        Timber.d("attachView: %s", view);
        mView = view;
    }

    /**
     * Call to remove/detach the attached view from presenter.
     * This is done to break the memory reference between presenter and view, so the GC will
     * know how to collect them.
     * <p>
     * detachView is called when the view is about to be destroyed by Android framework
     */
    @Override
    public void detachView() {
        Timber.d("detachView: %s", mView);
        mSubscription.clear();
        mView = null;
    }

    /**
     * notify the presenter that view is destroyed (onDestroy on Activity, Fragment)
     */
    @Override
    public void destroy() {
        Timber.d("destroy is called");
        detachView();
    }

    /**
     * notify the presenter that view is resumed (onResume on Activity, Fragment)
     */
    @Override
    public void resume() {
    }

    /**
     * notify the presenter that view is paused (onPause on Activity, Fragment)
     */
    @Override
    public void pause() {
    }
}
