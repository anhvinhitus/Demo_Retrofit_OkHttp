package vn.com.vng.zalopay.scanners.sound;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.grd.crity.CrityWrapper;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.service.RecordService;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.FileUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanSoundFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanSoundFragment extends BaseFragment {
    @OnClick(R.id.btnStartScanSound)
    public void onClickStartScanSound(View view) {
        Timber.d("Start scan sound");
        String mRecordName = null;
        try {
            mRecordName = FileUtil.getFilename("Record_" + String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent myIntent = new Intent(getContext(), RecordService.class);
        myIntent.putExtra(Constants.COMMANDTYPE, Constants.STATE_START_RECORDING);
        myIntent.putExtra(Constants.RECORDNAME, mRecordName);
        getContext().startService(myIntent);
    }

    @OnClick(R.id.btnStopScanSound)
    public void onClickStopScanSound(View view) {
        Timber.d("Stop scan sound");
        Intent myIntent = new Intent(getContext(), RecordService.class);
        myIntent.putExtra(Constants.COMMANDTYPE, Constants.STATE_STOP_RECORDING);
        getContext().startService(myIntent);
    }

    @OnClick(R.id.btnTestJNI)
    public void onClickTestJNI(View view) {
        Timber.d("Click Test JNI");
        String crityStr = CrityWrapper.doCreateSecureKeyPart("Hello world", getContext());
        Timber.d("onClickTestJNI...........crityStr:%s", crityStr);
    }

    public ScanSoundFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScanSoundFragment.
     */
    public static ScanSoundFragment newInstance() {
        ScanSoundFragment fragment = new ScanSoundFragment();
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {

    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_scan_sound;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
