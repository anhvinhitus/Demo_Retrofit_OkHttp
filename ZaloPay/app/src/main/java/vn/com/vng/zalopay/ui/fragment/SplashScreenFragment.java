package vn.com.vng.zalopay.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.ui.presenter.SplashScreenPresenter;
import vn.com.vng.zalopay.ui.view.ISplashScreenView;
import vn.com.vng.zalopay.utils.IntroAppUtils;

/**
 * Created by AnhHieu on 5/13/16.
 */
public class SplashScreenFragment extends BaseFragment implements ISplashScreenView {


    public static SplashScreenFragment newInstance() {

        Bundle args = new Bundle();

        SplashScreenFragment fragment = new SplashScreenFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private boolean interstitialCanceled = false;

    @Inject
    SplashScreenPresenter presenter;

    @Override
    protected void setupFragmentComponent() {
        getAppComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_splashscreen;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();
        presenter.handleDeepLinks(intent);
        presenter.verifyUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (interstitialCanceled) {
            gotoHomeScreen();
        }
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void gotoHomeScreen() {
        interstitialCanceled = true;
        navigator.startHomeActivity(getContext());
        getActivity().finish();
    }

    @Override
    public void gotoLoginScreen() {
        navigator.startLoginActivity(getContext());
        getActivity().finish();
    }

    @Override
    public void gotoOnBoardingScreen() {
        navigator.startIntroAppActivity(getContext());
        getActivity().finish();
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }
}
