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


    public SplashScreenFragment() {
        mLinks = null;
    }

    private boolean interstitialCanceled = false;

    private String mLinks;

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

        handleDeepLinks();
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
        navigator.startHomeActivity(getContext(), false);
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

    private void handleDeepLinks() {
        // Test : adb shell 'am start -d "zalopay-1://post?appid={}&zptranstoken={}"'
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            mLinks = String.valueOf(intent.getData());
            Timber.d("handleDeepLinks: mLinks %s", mLinks);
            if (this.mLinks.startsWith("zalopay-1://")) {
                String appid = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.APPID);
                String zptranstoken = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.ZPTRANSTOKEN);
                if (TextUtils.isEmpty(appid) || !TextUtils.isDigitsOnly(appid) || TextUtils.isEmpty(zptranstoken)) {
                    showToast(R.string.exception_data_invalid);
                } else {
                    EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();
                    eventBus.postSticky(new PaymentDataEvent(Long.parseLong(appid), zptranstoken));
                    Timber.d("postSticky payment");
                }
            }
        }
    }
}
