package vn.com.vng.zalopay.scanners.nfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.scanners.ui.FragmentLifecycle;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.RippleBackground;
import vn.com.vng.zalopay.ui.widget.WaveView;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link ScanNFCFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanNFCFragment extends BaseFragment implements NfcView, FragmentLifecycle {


    public ScanNFCFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScanNFCFragment.
     */
    public static ScanNFCFragment newInstance() {
        ScanNFCFragment fragment = new ScanNFCFragment();
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @BindView(R.id.imHand)
    ImageView mHandView;

    @BindView(R.id.waveView)
    RippleBackground mWareWaveView;

    @Inject
    NFCReaderPresenter readerPresenter;

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_scan_nfc;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readerPresenter.setView(this);
        starAnimation();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        readerPresenter.initialize();

        handleIntent(getActivity().getIntent());
    }

    private void starAnimation() {
        ViewCompat.setTranslationX(mHandView, 150);
        ViewCompat.setTranslationY(mHandView, 150);
        ViewCompat.animate(mHandView)
                .withLayer().setDuration(2000)
                .translationX(0).translationY(0)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (mWareWaveView != null) {
                            mWareWaveView.startRippleAnimation();
                        }
                    }
                })
                .start();
    }

    private void stopAnimation() {
        if (mWareWaveView != null) {
            mWareWaveView.stopRippleAnimation();
        }
    }

    @Override
    public void onDestroyView() {
        if (readerPresenter != null) {
            readerPresenter.destroyView();
        }

        ViewCompat.animate(mHandView).cancel();

        stopAnimation();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (readerPresenter != null) {
            readerPresenter.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (readerPresenter != null) {
            readerPresenter.pause();
        }
    }

    @Override
    public void onDestroy() {
        Timber.i("ScanNFCFragment is destroyed");
        super.onDestroy();
        if (readerPresenter != null) {
            readerPresenter.destroy();
        }
    }

    @Override
    public void onInitDone(int status) {
        switch (status) {
            case STATUS_NOT_AVAILABLE:
                //mBtnEmulateNfcReceive.setEnabled(true);
                //mNFCStatus.setText("NFC is not available");
                break;
            case STATUS_DISABLE:
                // mBtnEmulateNfcReceive.setVisibility(View.GONE);
                //mNFCStatus.setText("NFC is disabled");
                break;
            case STATUS_ENABLE:
                //mBtnEmulateNfcReceive.setVisibility(View.GONE);
                //mNFCStatus.setText("NFC ready");
                break;
        }
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onStartFragment() {
        if (readerPresenter != null) {
            readerPresenter.setupForegroundDispatch();
        }
    }

    @Override
    public void onStopFragment() {
        if (readerPresenter != null) {
            readerPresenter.stopForegroundDispatch();
        }
    }

    public void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (readerPresenter != null) {
            readerPresenter.handleDispatch(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_DEPOSIT) {
            if (resultCode == Activity.RESULT_OK) {
                readerPresenter.payPendingOrder();
            }
            return;
        } else if (requestCode == Constants.REQUEST_CODE_UPDATE_PROFILE_LEVEL_2) {
            if (resultCode == Activity.RESULT_OK) {
                readerPresenter.payPendingOrder();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
