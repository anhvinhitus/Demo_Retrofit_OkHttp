package vn.com.vng.zalopay.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.TransferMoneyViaAccountNamePresenter;
import vn.com.vng.zalopay.ui.view.ITransferMoneyView;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 9/11/16.
 * *
 */
public class TransferMoneyDialog extends AlertDialog implements ITransferMoneyView {

    public TransferMoneyDialog(Context context) {
        super(context, R.style.alert_dialog);
        this.setCancelable(true);
        this.setCanceledOnTouchOutside(false);
    }

    private SweetAlertDialog mProgressDialog;

    @BindView(R.id.sweetDialogRootView)
    LinearLayout mRootView;

    @BindView(R.id.tvAccountName)
    EditText mAccountNameView;

    @BindView(R.id.content_text)
    TextView mMessageView;

    @Inject
    Navigator navigator;

    @Inject
    TransferMoneyViaAccountNamePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplication.instance().getUserComponent().inject(this);
        setContentView(R.layout.dialog_transfer);
        ButterKnife.bind(this, this);
        setWidthDialog();
        presenter.setView(this);

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mAccountNameView.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        });
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hideLoading();
            }
        });
    }

    @OnTextChanged(R.id.tvAccountName)
    public void onTextChanged(CharSequence s) {
        isValid(s.toString());
    }

    public void setWidthDialog() {
        Display display = this.getWindow().getWindowManager().getDefaultDisplay();
        int densityDpi = display.getWidth();
        android.view.ViewGroup.LayoutParams params = this.mRootView.getLayoutParams();
        params.width = (int) ((double) densityDpi * 0.85D);
        params.height = -2;
        this.mRootView.setLayoutParams(params);
    }

    @Override
    public void onDetachedFromWindow() {
        presenter.destroyView();
        super.onDetachedFromWindow();
    }

    private boolean isValid(String s) {
        boolean isValid = false;

        if (!ValidateUtil.isValidLengthZPName(s)) {
            showError(getContext().getString(R.string.exception_account_name_length));
        } else if (s.indexOf(" ") > 0) {
            showError(getContext().getString(R.string.exception_account_name_with_space));
        } else if (!ValidateUtil.isValidZaloPayName(s)) {
            showError(getContext().getString(R.string.exception_account_name_special_char));
        } else {
            showError(null);

            if (!TextUtils.isEmpty(s)) {
                isValid = true;
            }
        }
        return isValid;
    }

    @OnClick(R.id.cancel_button)
    public void onClickCancel() {
        dismiss();
    }

    @OnClick(R.id.confirm_button)
    public void onClickConfirm() {
        Timber.d("name %s", mAccountNameView.getText().toString());
        String s = mAccountNameView.getText().toString().trim();

        boolean isValid = isValid(s);
        if (isValid) {
            presenter.getUserInfo(s);
        }
    }

    public void showLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void show() {
        super.show();
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    @Override
    public void showError(String message) {
        mMessageView.setText(message);
    }

    @Override
    public void onGetProfileSuccess(Person person, String zaloPayName) {
        dismiss();

        RecentTransaction item = new RecentTransaction();
        item.avatar = person.avatar;
        item.zaloPayId = person.zaloPayId;
        item.displayName = person.displayName;
        item.phoneNumber = String.valueOf(person.phonenumber);
        item.zaloPayName = zaloPayName;

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, Parcels.wrap(item));
        navigator.startTransferActivity(getContext(), bundle);
    }
}
