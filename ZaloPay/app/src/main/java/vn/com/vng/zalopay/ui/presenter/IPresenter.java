package vn.com.vng.zalopay.ui.presenter;

/**
 * Created by AnhHieu on 3/26/16.
 * Generic interface for presenter
 */
public interface IPresenter<View> {

    /**
     * Call to attach a view to presenter.
     *
     * The best time to call this function is when a view is created by Android framework
     */
    void attachView(View view);

    /**
     * Call to remove/detach the attached view from presenter.
     * This is done to break the memory reference between presenter and view, so the GC will
     * know how to collect them.
     *
     * detachView is called when the view is about to be destroyed by Android framework
     */
    void detachView();

    /**
     * notify the presenter that view is resumed (onResume on Activity, Fragment)
     */
    void resume();

    /**
     * notify the presenter that view is paused (onPause on Activity, Fragment)
     */
    void pause();

    /**
     * notify the presenter that view is destroyed (onDestroy on Activity, Fragment)
     */
    void destroy();
}
