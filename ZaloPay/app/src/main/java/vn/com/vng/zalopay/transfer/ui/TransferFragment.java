package vn.com.vng.zalopay.transfer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.KeyboardFrameLayout;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;
import com.zalopay.ui.widget.edittext.ZPEditText;
import com.zalopay.ui.widget.layout.OnKeyboardStateChangeListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.MoneyEditText;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.ImageLoader;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferFragment extends BaseFragment implements ITransferView, OnKeyboardStateChangeListener {

    public static TransferFragment newInstance(Bundle bundle) {
        TransferFragment fragment = new TransferFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private boolean mDisableEditData = false;

    @Inject
    TransferPresenter mPresenter;

    @Inject
    ImageLoader mImageLoader;

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

    @BindView(R.id.edtAmount)
    MoneyEditText mAmountView;

    @BindView(R.id.edtTransferMsg)
    ZPEditText mEdtMessageView;

    @BindView(R.id.btnContinue)
    View btnContinue;

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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle argument = getArguments();
        if (argument == null) {
            return;
        }

        mPresenter.attachView(this);
        mRootView.setOnKeyboardStateListener(this);

        mPresenter.initView((ZaloFriend) argument.getParcelable(Constants.ARG_ZALO_FRIEND),
                (RecentTransaction) argument.getParcelable(Constants.ARG_TRANSFERRECENT),
                argument.getLong(Constants.ARG_AMOUNT),
                argument.getString(Constants.ARG_MESSAGE));

        int transferMode = argument.getInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_DEFAULT);
        mPresenter.setTransferMode(transferMode);

        int transferType = argument.getInt(Constants.ARG_MONEY_TRANSFER_TYPE);
        if (transferType == Constants.QRCode.RECEIVE_FIXED_MONEY) {
            mDisableEditData = true;
        }

        if (mDisableEditData) {
            disableEditAmountAndMessage();
        }
        mPresenter.onViewCreated();
    }

    private void disableEditAmountAndMessage() {
        mAmountView.setFocusable(false);
        mAmountView.setFocusableInTouchMode(false);
        mAmountView.setInputType(InputType.TYPE_NULL);
        mEdtMessageView.setFocusable(false);
        mEdtMessageView.setFocusableInTouchMode(false);
        mEdtMessageView.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void onKeyBoardShow(int height) {
        if (mEdtMessageView == null || mScrollView == null) {
            return;
        }
        Timber.d("onKeyBoardShow: mEdtMessageView.isFocused() %s", mEdtMessageView.isFocused());
        if (mEdtMessageView.isFocused()) {
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            mEdtMessageView.requestFocusFromTouch();
        } else {
            //Scroll down 24dp (height of error text)
            mScrollView.scrollBy(0, AndroidUtils.dp(24));
        }
    }

    @Override
    public void onKeyBoardHide() {
        Timber.d("onKeyBoardHide");
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
    public void OnAfterAmountChanged(Editable s) {
        Timber.d("OnTextChangedAmount %s", mAmountView.isValid());
        setEnableBtnContinue(mAmountView.isValid() && !TextUtils.isEmpty(mPresenter.getZaloPayId()));
    }

    @OnTextChanged(R.id.edtTransferMsg)
    public void onTextChanged(CharSequence s) {
        mPresenter.updateMessage(TextUtils.isEmpty(s) ? "" : s.toString());
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {
        if (!mAmountView.validate()) {
            return;
        }

        if (!TextUtils.isEmpty(mPresenter.getZaloPayId())) {
            mPresenter.doTransfer(mAmountView.getAmount());
            ZPAnalytics.trackEvent(mEdtMessageView.length() == 0 ? ZPEvents.MONEYTRANSFER_INPUTNODESCRIPTION : ZPEvents.MONEYTRANSFER_INPUTDESCRIPTION);
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_TAPCONTINUE);
        }
    }

    @Override
    public void setInitialValue(long currentAmount, String currentMessage) {
        mEdtMessageView.setText(currentMessage);

        if (currentAmount > 0) {
            mAmountView.setText(String.valueOf(currentAmount));
        } else {
            mAmountView.setText("");
        }

        mAmountView.setSelection(mAmountView.length());

    }

    @Override
    public void showDialogThenClose(String message, String cancelText, int dialogType) {
        ZPWOnEventDialogListener onClickCancel = new ZPWOnEventDialogListener() {
            @Override
            public void onOKevent() {
                getActivity().finish();
            }
        };
        if (dialogType == SweetAlertDialog.ERROR_TYPE) {
            super.showErrorDialog(message, cancelText, onClickCancel);
        } else if (dialogType == SweetAlertDialog.WARNING_TYPE) {
            super.showWarningDialog(message, cancelText, onClickCancel);
        } else if (dialogType == SweetAlertDialog.NO_INTERNET) {
            super.showNetworkErrorDialog(new ZPWOnSweetDialogListener() {
                @Override
                public void onClickDiaLog(int i) {
                    getActivity().finish();
                }
            });
        }
    }

    /**
     * Set Receiver info when view had created
     *
     * @param displayName displayName
     * @param avatar      avatar
     * @param zalopayName If zaloPayName isn't not null or empty then set zaloPayName to view
     */
    @Override
    public void setReceiverInfo(String displayName, String avatar, String zalopayName) {
        Timber.d("setReceiverInfo displayName %s avatar %s", displayName, avatar);
        setDisplayName(displayName);
        setAvatar(avatar);
        setZaloPayName(zalopayName);
    }

    /**
     * Set Receiver info when server return user info
     *
     * @param displayName displayName
     * @param avatar      avatar
     * @param zalopayName If zaloPayName isn't not null or empty then set zaloPayName to view else invisible zaloPayName
     */
    @Override
    public void updateReceiverInfo(String displayName, String avatar, String zalopayName) {
        Timber.d("updateReceiverInfo displayName %s avatar %s", displayName, avatar);
        setDisplayName(displayName);
        setAvatar(avatar);
        udpateZaloPayName(zalopayName);

    }

    private void setZaloPayName(String zalopayName) {
        if (TextUtils.isEmpty(zalopayName)) {
            mTextViewZaloPayName.setVisibility(View.INVISIBLE);
        } else if (!TextUtils.isEmpty(zalopayName)) {
            mTextViewZaloPayName.setText(zalopayName);
            mTextViewZaloPayName.setVisibility(View.VISIBLE);
        }
    }

    private void udpateZaloPayName(String zalopayName) {
        if (TextUtils.isEmpty(zalopayName)) {
            mTextViewZaloPayName.setText(getString(R.string.not_update_zalopay_id));
        } else if (!TextUtils.isEmpty(zalopayName)) {
            mTextViewZaloPayName.setText(zalopayName);
        }
        mTextViewZaloPayName.setVisibility(View.VISIBLE);
    }

    private void setAvatar(String avatar) {
        if (TextUtils.isEmpty(avatar)) {
            return;
        }
        mImageLoader.loadImage(imgAvatar, avatar);
    }

    private void setDisplayName(String displayName) {
        if (TextUtils.isEmpty(displayName)) {
            return;
        }
        tvDisplayName.setText(displayName);
    }

    @Override
    public void setMinMaxMoney(long min, long max) {
        if (mAmountView != null) {
            mAmountView.setMinMaxMoney(min, max);
        }
    }

    @Override
    public void setEnableBtnContinue(boolean isEnable) {
        if (btnContinue != null) {
            btnContinue.setEnabled(isEnable);
        }
    }

    @Override
    public void confirmTransferUnRegistryZaloPay() {
        showConfirmDialog("Người nhận chưa đăng ký sử dụng Zalo Pay. Bạn có muốn tiếp tục chuyển tiền không?",
                getString(R.string.btn_confirm), getString(R.string.btn_cancel), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {

                    }

                    @Override
                    public void onOKevent() {
                        if (mPresenter == null) {
                            return;
                        }
                        mPresenter.transferMoney();
                    }
                });
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
        showErrorDialog(message);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        mImageLoader.loadImage(imgAvatar, "");
        mPresenter.initView((ZaloFriend) bundle.getParcelable(Constants.ARG_ZALO_FRIEND),
                (RecentTransaction) bundle.getParcelable(Constants.ARG_TRANSFERRECENT),
                bundle.getLong(Constants.ARG_AMOUNT),
                bundle.getString(Constants.ARG_MESSAGE));

        mPresenter.onViewCreated();
    }
}
