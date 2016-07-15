package vn.com.vng.zalopay.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.SplashScreenPresenter;
import vn.com.vng.zalopay.ui.view.ISplashScreenView;

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
    Navigator navigator;

    @Inject
    SplashScreenPresenter presenter;

    @Override
    protected void setupFragmentComponent() {
        AndroidApplication.instance().getAppComponent().inject(this);
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
        if (!onOpenDeepLinks()) {
            navigator.startHomeActivity(getContext(), false);
            getActivity().finish();
        }
    }

    private boolean onOpenDeepLinks() {
        // Test : adb shell 'am start -d "zalopay-1://post?appid={}&zptranstoken={}"'
        Uri data = getActivity().getIntent().getData();
        if (data != null
            //&& data.isHierarchical()
                ) {

            String appid = data.getQueryParameter(Constants.APPID);
            String zptranstoken = data.getQueryParameter(Constants.ZPTRANSTOKEN);
            if (TextUtils.isEmpty(appid) && TextUtils.isEmpty(zptranstoken)) {
            }else{
                Timber.d("appid %s zptranstoken %s ", appid, zptranstoken);
                if (TextUtils.isEmpty(appid) || !TextUtils.isDigitsOnly(appid) || TextUtils.isEmpty(zptranstoken)) {
                    showToast(R.string.exception_data_invalid);
                } else {
                    navigator.startHomeActivity(getActivity(), Long.parseLong(appid), zptranstoken);
                }

                getActivity().finish();
                return true;
            }
        }
        return false;
    }

    @Override
    public void gotoUpdateProfileLevel2() {
        navigator.startUpdateProfileLevel2Activity(getContext(), false);
        getActivity().finish();
    }

    @Override
    public void gotoLoginScreen() {
        navigator.startLoginActivity(getContext(), false);
        getActivity().finish();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }
}
