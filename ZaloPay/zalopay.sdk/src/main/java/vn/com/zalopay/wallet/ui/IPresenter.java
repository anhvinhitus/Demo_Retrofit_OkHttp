package vn.com.zalopay.wallet.ui;

/**
 * Created by chucvv on 6/12/17.
 */

public interface IPresenter<T> {
    void onAttach(T pView);

    void onDetach();

    void onStart(); //start doing the first task in presenter

    void onResume(); // as resume in host activity

    void onStop(); // as stop in host activity
}
