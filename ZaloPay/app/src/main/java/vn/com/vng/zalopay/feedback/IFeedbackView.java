package vn.com.vng.zalopay.feedback;

import android.net.Uri;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 1/6/17.
 */

interface IFeedbackView extends ILoadDataView {
    void insertScreenshot(Uri uri);

    void finish();

    void setEmail(String email);

    void setTransaction(String category, String transId, String errorMessage);

}
