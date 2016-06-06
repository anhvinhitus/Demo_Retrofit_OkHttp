package vn.com.vng.zalopay.scanners.ui.fragment;

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
 * Activities that contain this fragment must implement the
 * {@link ScanSoundFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScanSoundFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanSoundFragment extends BaseFragment {
    private OnFragmentInteractionListener mListener;

    @OnClick(R.id.btnStartScanSound)
    public void onClickStartScanSound(View view) {
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
        Timber.tag(TAG).d("onClickStopScanSound...");
        Intent myIntent = new Intent(getContext(), RecordService.class);
        myIntent.putExtra(Constants.COMMANDTYPE, Constants.STATE_STOP_RECORDING);
        getContext().startService(myIntent);
    }

    @OnClick(R.id.btnTestJNI)
    public void onClickTestJNI(View view) {
        String crityStr = CrityWrapper.doCreateSecureKeyPart("Hello world", getContext());
        Timber.tag(TAG).d("onClickTestJNI...........crityStr:%s", crityStr);
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
    // TODO: Rename and change types and number of parameters
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
//        if (getArguments() != null) {
//        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
