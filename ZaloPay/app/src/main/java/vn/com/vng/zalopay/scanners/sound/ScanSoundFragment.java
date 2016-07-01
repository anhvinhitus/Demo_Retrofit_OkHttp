package vn.com.vng.zalopay.scanners.sound;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.repository.BalanceRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.scanners.models.PaymentRecord;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.sound.transcoder.DecoderListener;
import vn.com.vng.zalopay.sound.transcoder.RecordService;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.WaveView;
import vn.com.vng.zalopay.utils.DebugUtils;
import vn.com.vng.zalopay.utils.FileUtil;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanSoundFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanSoundFragment extends BaseFragment {
    private RecordService recordService;
    private final DecoderListener decoderListener;
    private PaymentWrapper paymentWrapper;

    @Inject
    ZaloPayRepository zaloPayRepository;

    @Inject
    BalanceRepository mBalanceRepository;

    @Inject
    Navigator mNavigator;

    @OnClick(R.id.btnStartScanSound)
    public void onClickStartScanSound(View view) {
        Timber.d("Start scan sound");
        startTranscoder();
    }

    @OnClick(R.id.btnStopScanSound)
    public void onClickStopScanSound(View view) {
        Timber.d("Stop scan sound");
        stopTranscoder();
    }


    @BindView(R.id.waveView)
    WaveView waveView;

    private void startTranscoder() {
        if (!checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, 100)) {
            return;
        }

        getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (recordService == null) {
                    recordService = new RecordService();
                }

                String mRecordName = null;
                try {
                    mRecordName = FileUtil.getFilename("Record_" + String.valueOf(System.currentTimeMillis()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                recordService.start(mRecordName, decoderListener);
            }
        });
    }

    private void stopTranscoder() {
        getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (recordService != null) {
                    recordService.stop();
                }
            }
        });
    }

    public ScanSoundFragment() {
        // Required empty public constructor
        this.decoderListener = new DecoderListener() {
            @Override
            public void didDetectData(byte[] data) {
                Timber.w("Detect data: %s", DebugUtils.bytesToHex(data));
                Timber.w("Detect data: %s", new String(data, Charset.defaultCharset()));

                recordService.reset();
                if (data.length < 24) {
                    return;
                }

                PaymentRecord paymentRecord = PaymentRecord.from(data);
                if (paymentRecord == null) {
                    return;
                }

                stopTranscoder();
                paymentWrapper.payWithToken(paymentRecord.appId, paymentRecord.transactionToken);
            }

            @Override
            public void errorDetectData() {
                Timber.w("Error in detecting data");
                recordService.reset();
            }
        };
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScanSoundFragment.
     */
    public static ScanSoundFragment newInstance() {
        return new ScanSoundFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);

        paymentWrapper = new PaymentWrapper(mBalanceRepository,
                zaloPayRepository,
                new PaymentWrapper.IViewListener() {
                    @Override
                    public Activity getActivity() {
                        return ScanSoundFragment.this.getActivity();
                    }
                },
                new PaymentWrapper.IResponseListener() {
                    @Override
                    public void onParameterError(String param) {
                        if ("token".equalsIgnoreCase(param)) {
                            startTranscoder();
                        } else {
                            showToast(String.format("Parameter error: %s", param));
                        }
                    }

                    @Override
                    public void onResponseError(int status) {
                        startTranscoder();
                    }

                    @Override
                    public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                        ScanSoundFragment.this.getActivity().finish();
                    }

                    @Override
                    public void onResponseTokenInvalid() {

                    }

                    @Override
                    public void onResponseCancel() {

                    }

                    @Override
                    public void onNotEnoughMoney() {
                        mNavigator.startDepositActivity(ScanSoundFragment.this.getContext());
                    }
                }
        );
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_scan_sound;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTranscoder();
    }

    @Override
    public void onResume() {
        super.onResume();
//        startTranscoder();
    }

    public void startRecording() {
        startTranscoder();
    }

    public void stopRecording() {
        stopTranscoder();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        waveView.startRippleAnimation();
    }

    @Override
    public void onDestroyView() {
        waveView.stopRippleAnimation();
        super.onDestroyView();
    }
}
