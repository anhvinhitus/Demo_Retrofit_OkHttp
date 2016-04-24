package vn.com.vng.zalopay.ui.presenter;

/**
 * Created by AnhHieu on 3/26/16.
 */
public interface Presenter<View> {

    void setView(View view);

    void destroyView();

    void resume();

    void pause();

    void destroy();
}
