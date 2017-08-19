package vn.com.zalopay.wallet.view.custom;

import android.text.TextUtils;
import android.view.View;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.listener.OnSnackbarListener;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;

/***
 * snackbar wrapper
 */
public class PaymentSnackBar {
    private static PaymentSnackBar _object;

    private TSnackbar mSnackBar;

    private OnSnackbarListener mCloseListener;

    private View mRootView;

    private String mMessage;

    private String mActionMessage;

    private int mDuration;

    private int mBgColor;

    public PaymentSnackBar() {
    }

    public static PaymentSnackBar getInstance() {
        if (PaymentSnackBar._object == null)
            PaymentSnackBar._object = new PaymentSnackBar();

        return PaymentSnackBar._object.reset();
    }

    public void dismiss() {
        if (mSnackBar != null)
            mSnackBar.dismiss();
    }

    public PaymentSnackBar reset() {
        mMessage = null;
        mActionMessage = null;
        mDuration = TSnackbar.LENGTH_LONG;
        mCloseListener = null;
        mRootView = null;
        mBgColor = GlobalData.getAppContext().getResources().getColor(R.color.yellow_bg_popup_error);

        return this;
    }

    public PaymentSnackBar setRootView(View pRootView) {
        mRootView = pRootView;
        return this;
    }

    public PaymentSnackBar setMessage(String pMessage) {
        mMessage = pMessage;
        return this;
    }

    public PaymentSnackBar setActionMessage(String pActionMessage) {
        mActionMessage = pActionMessage;
        return this;
    }

    public PaymentSnackBar setDuration(int pDuration) {
        mDuration = pDuration;
        return this;
    }

    public PaymentSnackBar setOnCloseListener(OnSnackbarListener pListener) {
        mCloseListener = pListener;
        return this;
    }

    public PaymentSnackBar setBgColor(int pColor) {
        mBgColor = pColor;
        return this;
    }

    public void show() throws Exception {
        if (mRootView == null) {
            throw new Exception("Cần truyền rootview để sử dụng snackbar");
        }
        if (TextUtils.isEmpty(mMessage) && TextUtils.isEmpty(mActionMessage)) {
            throw new Exception("Cần truyền message để sử dụng snackbar");
        }

        if (mSnackBar == null || mSnackBar.getView().getId() != mRootView.getId())
            mSnackBar = TSnackbar.makeMessageBar(mRootView, mMessage, mActionMessage, mDuration, mCloseListener);
        else {
            mSnackBar.setMessage(mMessage);
            mSnackBar.setActionMessage(mActionMessage);
            mSnackBar.setDuration(mDuration);
            mSnackBar.setCloseListener(mCloseListener);
        }
        View snackbarView = mSnackBar.getView();
        snackbarView.setBackgroundColor(mBgColor);

        mSnackBar.show();
    }

}
