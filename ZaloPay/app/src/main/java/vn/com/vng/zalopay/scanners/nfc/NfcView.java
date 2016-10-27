package vn.com.vng.zalopay.scanners.nfc;

import android.app.Activity;
import android.content.Context;

/**
 * Created by huuhoa on 6/1/16.
 * Interface for handling NFC content
 */
interface NfcView {
    int STATUS_NOT_AVAILABLE = 1;
    int STATUS_DISABLE = 2;
    int STATUS_ENABLE = 3;

    void onInitDone(int status);

    Context getContext();

    Activity getActivity();
}
