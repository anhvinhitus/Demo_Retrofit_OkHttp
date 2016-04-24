package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.presenter.LoginPresenter;
import vn.com.vng.zalopay.ui.view.ILoginView;

/**
 * Created by AnhHieu on 3/26/16.
 */
public class LoginFragment extends BaseFragment implements ILoginView {


    public static LoginFragment newInstance() {
        Bundle args = new Bundle();

        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        AndroidApplication.instance().getAppComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_login;
    }

    @Inject
    LoginPresenter mPresenter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.setView(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Timber.d("onActivityCreated " + mPresenter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();

        Timber.i("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.pause();
    }

    @Override
    public void onDestroyView() {
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

}
