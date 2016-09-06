package vn.com.vng.zalopay.scanners.nfc;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.scanners.models.PaymentRecord;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.WaveView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link ScanNFCFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanNFCFragment extends BaseFragment implements NfcView {
    private PaymentWrapper paymentWrapper;
    private NFCReaderPresenter readerPresenter;

    @Inject
    ZaloPayRepository zaloPayRepository;

    @Inject
    BalanceStore.Repository mBalanceRepository;

    @Inject
    TransactionStore.Repository mTransactionRepository;


    public ScanNFCFragment() {
        // Required empty public constructor
    }

    private boolean processOrder(String orderToken) {
        String[] contents = orderToken.split(":");
        if (contents.length < 2) {
            new SweetAlertDialog(getActivity(), 0, SweetAlertDialog.ERROR_TYPE)
                    .setContentText(String.format("Nội dung thẻ Nfc không hợp lệ.\nNội dung: [%s]", orderToken))
                    .show();
            return false;
        }

        long appId = Long.valueOf(contents[0]);
        String token = contents[1];

        if (paymentWrapper == null) {
            //mNFCStatus.setText("Có lỗi phát sinh");
            Timber.w("PaymentWrapper is NULL");
            return false;
        }

        Timber.i("appId: %d, token: %s", appId, token);
        paymentWrapper.payWithToken(appId, token);
        return true;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScanNFCFragment.
     */
    public static ScanNFCFragment newInstance() {
        ScanNFCFragment fragment = new ScanNFCFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
        return fragment;
    }

    public void setReaderPresenter(NFCReaderPresenter presenter) {
        readerPresenter = presenter;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);

        paymentWrapper = new PaymentWrapper(mBalanceRepository, zaloPayRepository, mTransactionRepository,
                new PaymentWrapper.IViewListener() {
                    @Override
                    public Activity getActivity() {
                        return ScanNFCFragment.this.getActivity();
                    }
                },
                new PaymentWrapper.IResponseListener() {
                    @Override
                    public void onParameterError(String param) {
                        //mNFCStatus.setText("Tham số hoá đơn không hợp lệ");
                    }

                    @Override
                    public void onResponseError(PaymentError paymentError) {
                        //mNFCStatus.setText("");
//                        //mNFCStatus.setText(String.format("Response error: %d", status));
                    }

                    @Override
                    public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                        //mNFCStatus.setText("Thanh toán thành công");

                    }

                    @Override
                    public void onResponseTokenInvalid() {

                    }

                    @Override
                    public void onAppError(String msg) {

                    }

                    @Override
                    public void onPreComplete(boolean isSuccessful,String pTransId) {

                    }

                    @Override
                    public void onNotEnoughMoney() {
                        navigator.startDepositActivity(ScanNFCFragment.this.getContext());
                    }
                });
    }

    @BindView(R.id.imHand)
    ImageView mHandView;

    @BindView(R.id.waveView)
    WaveView mWareWaveView;

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
        // Inflate the layout for this fragment
        super.onViewCreated(view, savedInstanceState);

        if (readerPresenter != null) {
            readerPresenter.setView(this);
            readerPresenter.initialize();
        }

        starAnimation();

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
    public void onReceivePaymentRecord(PaymentRecord paymentRecord) {
        if (paymentRecord == null) {
            Timber.e("No payment record");
            return;
        }

        getAppComponent().monitorTiming().finishEvent(MonitorEvents.NFC_SCANNING);

        if (paymentWrapper == null) {
            //mNFCStatus.setText("Something wrong. PaymentWrapper is still NULL");
            return;
        }

        Timber.i("appId: %d, token: [%s]", paymentRecord.appId, paymentRecord.transactionToken);
        paymentWrapper.payWithToken(paymentRecord.appId, paymentRecord.transactionToken);
    }
}
