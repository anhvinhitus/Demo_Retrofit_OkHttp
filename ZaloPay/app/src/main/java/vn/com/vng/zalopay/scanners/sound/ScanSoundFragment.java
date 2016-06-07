package vn.com.vng.zalopay.scanners.sound;

import android.support.v4.app.Fragment;
import android.view.View;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.sound.transcoder.DecoderListener;
import vn.com.vng.zalopay.sound.transcoder.RecordService;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.DebugUtils;
import vn.com.vng.zalopay.utils.FileUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanSoundFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanSoundFragment extends BaseFragment {
    private RecordService recordService;
    private final DecoderListener decoderListener;

    @OnClick(R.id.btnStartScanSound)
    public void onClickStartScanSound(View view) {
        Timber.d("Start scan sound");
        String mRecordName = null;
        try {
            mRecordName = FileUtil.getFilename("Record_" + String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (recordService == null) {
            this.recordService = new RecordService();
        }

        recordService.start(mRecordName, this.decoderListener);
    }

    @OnClick(R.id.btnStopScanSound)
    public void onClickStopScanSound(View view) {
        Timber.d("Stop scan sound");
        recordService.stop();
    }

    public ScanSoundFragment() {
        // Required empty public constructor
        this.decoderListener = new DecoderListener() {
            @Override
            public void didDetectData(byte[] data) {
                Timber.w("Detect data: %s", DebugUtils.bytesToHex(data));
                Timber.w("Detect data: %s", new String(data, Charset.defaultCharset()));

                recordService.reset();
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

    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_scan_sound;
    }
}
