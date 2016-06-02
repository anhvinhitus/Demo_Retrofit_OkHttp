package vn.com.vng.zalopay.scanners.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.scanners.controller.NFCReaderPresenter;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link ScanNFCFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanNFCFragment extends BaseFragment implements NfcView {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private NFCReaderPresenter readerPresenter;

    public ScanNFCFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.scan_nfc_content)
    TextView mNFCContent;

    @BindView(R.id.scan_nfc_status)
    TextView mNFCStatus;

    @BindView(R.id.btn_read_nfc)
    View mBtnEmulateNfcReceive;

    @OnClick(R.id.btn_read_nfc)
    void onReadNFC() {
        String emulateContent = "3:nd0raT2d2tLAi567+5cXog==";

        processOrder(emulateContent);
    }

    private boolean processOrder(String orderToken) {
        String[] contents = orderToken.split(":");
        if (contents.length < 2) {
            new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                    .setContentText(String.format("Nội dung thẻ Nfc không hợp lệ.\nNội dung: [%s]", orderToken))
                    .show();
            return false;
        }

        long appId = Long.valueOf(contents[0]);
        String token = contents[1];
        // TODO: Call payment SDK

        return true;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScanNFCFragment.
     */
    public static ScanNFCFragment newInstance(String param1, String param2) {
        ScanNFCFragment fragment = new ScanNFCFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void setReaderPresenter(NFCReaderPresenter presenter) {
        readerPresenter = presenter;
    }
    @Override
    protected void setupFragmentComponent() {

    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_scan_nfc;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onViewCreated(view, savedInstanceState);

        mNFCContent.setText(mParam1);

        readerPresenter.setView(this);
        readerPresenter.initialize();
    }

    @Override
    public void onDestroyView() {
        readerPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        readerPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        readerPresenter.pause();
    }

    @Override
    public void onDestroy() {
        Timber.i("ScanNFCFragment is destroyed");
        super.onDestroy();
        readerPresenter.destroy();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onReceiveString(String content) {
        mNFCContent.setText(content);
        if (!processOrder(content)) {
            new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                    .setContentText("Xác nhận thanh toán.\nBạn cần thanh toán 30.000 VND")
                    .show();
        }

    }

    @Override
    public void onInitDone(boolean isEnable, String status) {
        mNFCStatus.setText(status);
        mBtnEmulateNfcReceive.setEnabled(!isEnable);
    }
}
