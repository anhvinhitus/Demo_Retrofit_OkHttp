package vn.com.vng.zalopay.transfer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.KeyboardFrameLayout;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.edittext.ZPEditText;
import com.zalopay.ui.widget.layout.OnKeyboardStateChangeListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.MoneyEditText;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.utility.CurrencyUtil;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferFragment extends BaseFragment implements ITransferView, OnKeyboardStateChangeListener {

    @Inject
    TransferPresenter mPresenter;

    @BindView(R.id.rootView)
    KeyboardFrameLayout mRootView;

    @BindView(R.id.ScrollView)
    ScrollView mScrollView;

    @BindView(R.id.imgAvatar)
    SimpleDraweeView imgAvatar;

    @BindView(R.id.tvDisplayName)
    TextView tvDisplayName;

    @BindView(R.id.tvZaloPayName)
    TextView mTextViewZaloPayName;

    @BindView(R.id.layout_profile)
    View mLayoutProfile;

    @BindView(R.id.txtError)
    TextView mTxtError;

    /* Layout Transfer dynamic money start.*/
    @BindView(R.id.layout_dynamic_money)
    View mLayoutDynamicMoney;

    @BindView(R.id.edtAmount)
    MoneyEditText mAmountView;

    @BindView(R.id.edtTransferMsg)
    ZPEditText mEdtMessageView;
    /* Layout Transfer dynamic money end.*/

    /* Layout Transfer fixed money start.*/
    @BindView(R.id.layout_fixed_money)
    View mLayoutFixedMoney;

    @BindView(R.id.txtAmount)
    TextView mTxtAmount;

    @BindView(R.id.txtTransferMsg)
    TextView mTxtTransferMsg;
    /* Layout Transfer fixed money end.*/

    @BindView(R.id.btnContinue)
    View btnContinue;

    TransferObject mTransferObject;

    public static TransferFragment newInstance(TransferObject object) {
        TransferFragment fragment = new TransferFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("transfer", object);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_transfer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleData(savedInstanceState == null ? getArguments() : savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mPresenter.setTransferObject(mTransferObject);
        mRootView.setOnKeyboardStateListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mTransferObject.amount = getAmount();
        mTransferObject.message = getMessage();
        outState.putParcelable("transfer", mTransferObject);
    }

    @Override
    public void onKeyBoardShow(int height) {
        if (mEdtMessageView == null || mScrollView == null) {
            return;
        }

        if (mEdtMessageView.isFocused()) {
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            mEdtMessageView.requestFocusFromTouch();
        } else {
            mScrollView.scrollBy(0, AndroidUtils.dp(24));
        }
    }

    @Override
    public void onKeyBoardHide() {
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public boolean onBackPressed() {
        mPresenter.navigateBack();
        return false;
    }


    @Override
    public void onDestroyView() {
        mRootView.setOnKeyboardStateListener(null);
        mAmountView.clearValidators();
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @OnTextChanged(callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED, value = R.id.edtAmount)
    public void onAfterAmountChanged(Editable s) {
        Timber.d("OnTextChangedAmount %s", mAmountView.isValid());
        setEnabledTransfer(mAmountView.isValid());
    }

    @OnFocusChange(R.id.edtAmount)
    public void onFocusChangeAmount(View v) {
        setEnabledTransfer(mAmountView.isValid());
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {
        if (mAmountView.validate()) {
            mPresenter.doClickTransfer(getAmount());
        }
    }

    @Override
    public void showErrorTransferFixedMoney(String error) {
        mTxtError.setText(error);
        mTxtError.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideErrorTransferFixedMoney() {
        mTxtError.setText("");
        mTxtError.setVisibility(View.GONE);
    }

    private void setInitialFixedValue(long currentAmount, String currentMessage) {
        mEdtMessageView.setText(currentMessage);
        mAmountView.setText(currentAmount <= 0 ? "" : String.valueOf(currentAmount));
        mAmountView.setEnabled(false);
        mEdtMessageView.setEnabled(false);

        mTxtAmount.setText(CurrencyUtil.formatCurrency(currentAmount, false));
        mTxtTransferMsg.setText(currentMessage);

        mLayoutProfile.setBackgroundResource(R.color.white);
        mLayoutDynamicMoney.setVisibility(View.GONE);
        mLayoutFixedMoney.setVisibility(View.VISIBLE);
    }

    private void setInitialDynamicValue(long currentAmount, String currentMessage) {

        mAmountView.setEnabled(true);
        mEdtMessageView.setEnabled(true);

        mEdtMessageView.setText(currentMessage);
        mAmountView.setText(currentAmount <= 0 ? "" : String.valueOf(currentAmount));

        mAmountView.setSelection(mAmountView.length());
        mAmountView.validate();

        mLayoutProfile.setBackgroundResource(R.color.background);
        mLayoutDynamicMoney.setVisibility(View.VISIBLE);
        mLayoutFixedMoney.setVisibility(View.GONE);
    }

    @Override
    public long getAmount() {
        if (mAmountView != null) {
            return mAmountView.getAmount();
        }
        return 0;
    }

    @Override
    public String getMessage() {
        if (mEdtMessageView != null) {
            return mEdtMessageView.getText().toString();
        }
        return "";
    }

    @Override
    public void setTransferInfo(TransferObject object, boolean amountDynamic) {

        setUserView(object.displayName, object.avatar, object.zalopayName);

        if (amountDynamic) {
            setInitialDynamicValue(object.amount, object.message);
        } else {
            setInitialFixedValue(object.amount, object.message);
        }

    }

    @Override
    public void setUserInfo(Person object) {
        setUserView(object.displayName, object.avatar, object.zalopayname);
    }

    private void setUserView(String displayName, String avatar, String zalopayName) {
        if (tvDisplayName != null) {
            tvDisplayName.setText(displayName);
        }

        if (imgAvatar != null) {
            imgAvatar.setImageURI(avatar);
        }

        String name = TextUtils.isEmpty(zalopayName) ? getString(R.string.not_update) : zalopayName;

        if (mTextViewZaloPayName != null) {
            mTextViewZaloPayName.setText(String.format(getString(R.string.account_format), name));
        }
    }

    @Override
    public void showDialogThenClose(String message, String cancelText, int dialogType) {
        ZPWOnEventDialogListener onClickCancel = () -> getActivity().finish();
        if (dialogType == SweetAlertDialog.ERROR_TYPE) {
            super.showErrorDialog(message, cancelText, onClickCancel);
        } else if (dialogType == SweetAlertDialog.WARNING_TYPE) {
            super.showWarningDialog(message, cancelText, onClickCancel);
        } else if (dialogType == SweetAlertDialog.NO_INTERNET) {
            super.showNetworkErrorDialog(i -> getActivity().finish());
        }
    }

    @Override
    public void setMinMaxMoney(long min, long max) {
        if (mAmountView != null) {
            mAmountView.setMinMaxMoney(min, max);
        }
    }

    @Override
    public void setEnabledTransfer(boolean enabled) {
        if (btnContinue != null) {
            btnContinue.setEnabled(enabled);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void showLoading() {
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
    }

    @Override
    public void showError(String message) {
        showErrorDialog(message, () -> mPresenter.shouldFinishTransfer());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        handleData(bundle);
        mPresenter.setTransferObject(mTransferObject);
    }

    private void handleData(Bundle bundle) {
        TransferObject object = bundle.getParcelable("transfer");
        if (object != null) {
            mTransferObject = object;
        }
    }
}
