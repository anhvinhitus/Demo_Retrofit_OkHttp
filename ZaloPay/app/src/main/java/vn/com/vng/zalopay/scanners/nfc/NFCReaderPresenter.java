package vn.com.vng.zalopay.scanners.nfc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.MemoryUtils;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.scanners.models.PaymentRecord;
import vn.com.vng.zalopay.service.DefaultPaymentRedirectListener;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by huuhoa on 6/1/16.
 * Read NFC
 */
final class NFCReaderPresenter extends AbstractPresenter<NfcView> {
    private final String MIME_TEXT_PLAIN = "text/plain";

    private NfcAdapter mNfcAdapter;

    private ZaloPayRepository zaloPayRepository;

    private BalanceStore.Repository mBalanceRepository;

    private TransactionStore.Repository mTransactionRepository;

    private PaymentWrapper paymentWrapper;
    private Navigator mNavigator;
    private Context mApplicationContext;

    @Inject
    NFCReaderPresenter(Context context, Navigator navigator, ZaloPayRepository zaloPayRepository,
                       BalanceStore.Repository mBalanceRepository,
                       TransactionStore.Repository mTransactionRepository) {
        this.zaloPayRepository = zaloPayRepository;
        this.mBalanceRepository = mBalanceRepository;
        this.mTransactionRepository = mTransactionRepository;
        this.mNavigator = navigator;
        this.mApplicationContext = context;
        initPaymentWrapper();
    }

    public void initialize() {
        if (mView == null) {
            return;
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(mView.getContext());
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            mView.onInitDone(NfcView.STATUS_NOT_AVAILABLE);
            return;
        }

        if (mNfcAdapter.isEnabled()) {
            mView.onInitDone(NfcView.STATUS_ENABLE);
        } else {
            mView.onInitDone(NfcView.STATUS_DISABLE);
        }
    }

    public void setupForegroundDispatch() {
        Timber.d("setupForegroundDispatch");
        final Intent intent = new Intent(mView.getContext(), mView.getActivity().getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(mView.getContext(), 0, intent, 0);

        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(mView.getActivity(), pendingIntent, null, null);
        }
    }

    public void stopForegroundDispatch() {
        Timber.d("stopForegroundDispatch");
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(mView.getActivity());
        }
    }

    void handleDispatch(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                this.executeReader(tag);
            } else {
                Timber.d("Wrong mime type: %s", type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    this.executeReader(tag);
                    break;
                }
            }
        }
    }

    private void executeReader(Tag tag) {
        new NdefReaderTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tag);
    }

    @Override
    public void resume() {
        if (mNfcAdapter == null) {
            return;
        }
        setupForegroundDispatch();
    }

    @Override
    public void pause() {
        if (mNfcAdapter == null) {
            return;
        }

        stopForegroundDispatch();
    }

    @Override
    public void destroy() {
        mNfcAdapter = null;
        super.destroy();
    }

    private void onReceivePaymentRecord(PaymentRecord record) {
        if (record == null) {
            Timber.e("No payment record");
            return;
        }

        ((AndroidApplication) mApplicationContext).getAppComponent()
                .monitorTiming().finishEvent(MonitorEvents.NFC_SCANNING);

        if (paymentWrapper == null) {
            //mNFCStatus.setText("Something wrong. PaymentWrapper is still NULL");
            return;
        }

        Timber.i("appId: %d, token: [%s]", record.appId, record.transactionToken);
        paymentWrapper.payWithToken(mView.getActivity(), record.appId, record.transactionToken);
    }

    private void initPaymentWrapper() {
        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(mBalanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(mTransactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new DefaultPaymentRedirectListener(mNavigator) {
                    @Override
                    public Object getContext() {
                        if (mView == null) {
                            return null;
                        }
                        return mView.getFragment();
                    }
                })
                .build();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (paymentWrapper == null) {
            return;
        }
        paymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     */
    private static class NdefReaderTask extends AsyncTask<Tag, Void, PaymentRecord> {

        private WeakReference<NFCReaderPresenter> mPresenter;

        NdefReaderTask(NFCReaderPresenter listener) {
            mPresenter = new WeakReference<>(listener);
        }

        @Override
        protected PaymentRecord doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readPaymentRecord(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Timber.e(e, "Unsupported Encoding");
                    }
                }
            }

            return null;
        }

        private PaymentRecord readPaymentRecord(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            byte[] data = MemoryUtils.extractBytes(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1);
            return PaymentRecord.from(data);
        }

        @Override
        protected void onPostExecute(PaymentRecord result) {
            if (mPresenter.get() != null) {
                mPresenter.get().onReceivePaymentRecord(result);
            }
        }
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {
        @Override
        protected ILoadDataView getView() {
            return null;
        }

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
        public void onAppError(String msg) {

        }

    }

}
