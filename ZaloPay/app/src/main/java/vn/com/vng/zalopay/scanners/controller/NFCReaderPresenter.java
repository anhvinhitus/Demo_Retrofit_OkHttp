package vn.com.vng.zalopay.scanners.controller;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import timber.log.Timber;
import vn.com.vng.zalopay.scanners.ui.NfcView;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by huuhoa on 6/1/16.
 * Read NFC
 */
public class NFCReaderPresenter extends BaseUserPresenter implements IPresenter<NfcView> {
    public static final String MIME_TEXT_PLAIN = "text/plain";

    NfcAdapter mNfcAdapter;
    Activity mActivity;

    public NFCReaderPresenter(Activity activity) {
        mActivity = activity;
    }

    public void initialize() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mActivity);
        if (mNfcView == null) {
            return;
        }

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(mActivity, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            mNfcView.onInitDone(false, "This device doesn't support NFC.");
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
            mNfcView.onInitDone(false, "NFC is disabled.");
        } else {
            mNfcView.onInitDone(true, "Put your mobile near POS device");
        }
    }

    public void setupForegroundDispatch() {
        final Intent intent = new Intent(mActivity.getApplicationContext(), mActivity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(mActivity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(mActivity, pendingIntent, filters, techList);
        }
    }

    public void stopForegroundDispatch() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(mActivity);
        }
    }

    public void handleDispatch(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Timber.d("Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    private NfcView mNfcView;

    @Override
    public void setView(NfcView nfcView) {
        mNfcView = nfcView;
    }

    @Override
    public void destroyView() {
        mNfcView = null;
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
        mActivity = null;
        mNfcAdapter = null;
        mNfcView = null;
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
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
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Timber.e(e, "Unsupported Encoding");
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
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

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && mNfcView != null) {
                mNfcView.onReceiveString(result);
            }
        }
    }

}
