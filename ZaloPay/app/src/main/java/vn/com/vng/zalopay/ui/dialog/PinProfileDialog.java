package vn.com.vng.zalopay.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.PinProfilePresenter;
import vn.com.vng.zalopay.ui.view.IPinProfileView;
import vn.com.zalopay.wallet.view.custom.pinview.GridPasswordView;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 9/9/16.
 * *
 */
public class PinProfileDialog extends AlertDialog implements IPinProfileView {

    public interface PinProfileListener {
        void onPinSuccess();

        void onPinError();
    }

    @BindView(R.id.tvHint)
    TextView tvHint;

    @BindView(R.id.sweetDialogRootView)
    LinearLayout mRootView;

    @BindView(R.id.passCodeInput)
    GridPasswordView passCodeInput;

    @Inject
    PinProfilePresenter presenter;

    @Inject
    Navigator navigator;

    private SweetAlertDialog mProgressDialog;

    private Intent pendingIntent;

    private PinProfileListener listener;

    private boolean pinSuccess = false;

    public PinProfileDialog(Context context, Intent pendingIntent) {
        super(context, R.style.alert_dialog);
        this.setCancelable(true);
        this.setCanceledOnTouchOutside(false);
        this.pendingIntent = pendingIntent;
    }

    public PinProfileDialog(Context context) {
        this(context, null);
    }

    public void setListener(PinProfileListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplication.instance().getUserComponent().inject(this);
        setContentView(R.layout.dialog_pin_profile);
        ButterKnife.bind(this, this);
        setWidthDialog();
        presenter.setView(this);

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                passCodeInput.forceInputViewGetFocus();
            }
        });
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hideLoading();
                if (!pinSuccess) {
                    if (listener != null) {
                        listener.onPinError();
                    }
                }
            }
        });

        passCodeInput.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String s) {

            }

            @Override
            public void onInputFinish(String s) {
                presenter.validatePin(s);
            }
        });
    }

    private void setWidthDialog() {
        Display display = this.getWindow().getWindowManager().getDefaultDisplay();
        int densityDpi = display.getWidth();
        android.view.ViewGroup.LayoutParams params = this.mRootView.getLayoutParams();
        params.width = (int) ((double) densityDpi * 0.85D);
        params.height = -2;
        this.mRootView.setLayoutParams(params);
    }

    @Override
    public void onDetachedFromWindow() {
        Timber.d("onDetachedFromWindow");
        hideLoading();
        presenter.destroyView();
        listener = null;
        super.onDetachedFromWindow();
    }

    @Override
    public void setError(String message) {
        tvHint.setText(message);
    }

    @Override
    public void clearPin() {
        passCodeInput.clearPassword();
    }

    @Override
    public void onPinSuccess() {
        Timber.d("onPinSuccess");
        pinSuccess = true;
        navigator.setLastTimeCheckPin(System.currentTimeMillis());

        if (pendingIntent != null) {
            getContext().startActivity(pendingIntent);
        }

        if (listener != null) {
            listener.onPinSuccess();
        }
        dismiss();
    }

    @Override
    public void showLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    @Override
    public void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    @OnClick(R.id.cancel_button)
    public void onClickCancel() {
        dismiss();
    }

    @Override
    public void show() {
        super.show();
        if (getWindow() != null) {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }
}
