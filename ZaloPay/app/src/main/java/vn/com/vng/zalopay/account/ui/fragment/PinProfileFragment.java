package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zalopay.ui.widget.KeyboardFrameLayout;
import com.zalopay.ui.widget.edittext.ZPEditText;
import com.zalopay.ui.widget.layout.OnKeyboardStateChangeListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.PinProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IPinProfileView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.IPassCodeFocusChanged;
import vn.com.vng.zalopay.ui.widget.PassCodeView;
import vn.com.vng.zalopay.ui.widget.intro.CircleEraser;
import vn.com.vng.zalopay.ui.widget.intro.IntroProfileView;
import vn.com.vng.zalopay.ui.widget.intro.RectangleEraser;
import vn.com.vng.zalopay.ui.widget.intro.ViewTarget;
import vn.com.vng.zalopay.ui.widget.validate.VNPhoneValidate;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PinProfileFragment.OnPinProfileFragmentListener} interface
 * to handle interaction events.
 * Use the {@link PinProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PinProfileFragment extends BaseFragment implements IPinProfileView, OnKeyboardStateChangeListener {

    public interface OnPinProfileFragmentListener {
        void onUpdatePinSuccess(String phone);
    }

    public static PinProfileFragment newInstance() {
        return new PinProfileFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_pin_profile;
    }

    private OnPinProfileFragmentListener mListener;

    @Inject
    PinProfilePresenter mPresenter;

    @BindView(R.id.rootView)
    KeyboardFrameLayout mRootView;

    @BindView(R.id.ScrollView)
    ScrollView mScrollView;

    @BindView(R.id.txtTitle)
    View txtTitle;

    @BindView(R.id.passcodeInput)
    PassCodeView mPassCodeView;

    @BindView(R.id.edtPhone)
    ZPEditText mEdtPhoneView;

    @BindView(R.id.tvTermsOfUser3)
    TextView tvTermsOfUser3;

    @BindView(R.id.btnContinue)
    View mBtnContinueView;

    @OnTextChanged(value = R.id.edtPhone, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChangedPhone() {
        mBtnContinueView.setEnabled(mEdtPhoneView.isValid() && mPassCodeView.isValid());
    }

    IPassCodeFocusChanged mPassCodeFocusChanged = new IPassCodeFocusChanged() {
        @Override
        public void onFocusChangedPin(boolean isFocus) {

            if (mBtnContinueView != null) {
                mBtnContinueView.setEnabled(mEdtPhoneView.isValid() && mPassCodeView.isValid());
            }

            mPassCodeView.setError(mPassCodeView.isValid() || isFocus ? null : getString(R.string.invalid_pin));
        }
    };

    TextWatcher mPassCodeChange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mPassCodeView.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void setPhoneNumber(String phone) {
        if (mEdtPhoneView != null) {
            mEdtPhoneView.setText(phone);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.setView(this);

        mEdtPhoneView.addValidator(new VNPhoneValidate(getString(R.string.invalid_phone)));
        mPassCodeView.addTextChangedListener(mPassCodeChange);
        mPassCodeView.setPassCodeFocusChanged(mPassCodeFocusChanged);

        AndroidUtils.setSpannedMessageToView(tvTermsOfUser3, R.string.agree_term_of_use, R.string.term_of_use,
                false, false, R.color.colorPrimary,
                new ClickableSpanNoUnderline() {
                    @Override
                    public void onClick(View widget) {
                        navigator.startTermActivity(getContext());
                    }
                });

        mRootView.setOnKeyboardStateListener(this);

        mBtnContinueView.setEnabled(mEdtPhoneView.isValid() && mPassCodeView.isValid());

        AndroidUtils.runOnUIThread(mIntroRunnable, 300);
    }

    @Override
    public void onKeyBoardShow(int height) {
        if (mPassCodeView.isFocused()) {
            mScrollView.smoothScrollTo(0, txtTitle.getHeight());
        } else if (mEdtPhoneView.isFocused()) {
            mScrollView.fullScroll(View.FOCUS_DOWN);
        }
    }

    @Override
    public void onKeyBoardHide() {
        //empty
    }

    private Runnable mIntroRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                showIntro();
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPinProfileFragmentListener) {
            mListener = (OnPinProfileFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        AndroidUtils.cancelRunOnUIThread(mIntroRunnable);
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onDestroyView() {
        mRootView.setOnKeyboardStateListener(null);
        mPassCodeView.setPassCodeFocusChanged(null);
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.upd_profile2, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_intro) {
            showIntro();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        mPresenter.saveProfileInfo2Cache(mEdtPhoneView.getText().toString());
        return super.onBackPressed();
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
    public void showRetry() {
    }

    @Override
    public void hideRetry() {
    }

    private void showIntro() {

        AndroidUtils.hideKeyboard(getActivity());

        mPassCodeView.setError(null);

        getActivity().getWindow().getDecorView().clearFocus();

        IntroProfileView intro = new IntroProfileView(getActivity(), null);

        intro.addShape(new RectangleEraser(new ViewTarget(mPassCodeView.getPassCodeView()), AndroidUtils.dp(4f)));
        intro.addShape(new CircleEraser(new ViewTarget(mPassCodeView.getButtonShow())));
        intro.addShape(new RectangleEraser(new ViewTarget(mEdtPhoneView), new Rect(-AndroidUtils.dp(12), -AndroidUtils.dp(12), -AndroidUtils.dp(12), 0)));

        intro.show(getActivity());
    }


    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void updateProfileSuccess(String phone) {
        if (mListener != null) {
            mListener.onUpdatePinSuccess(phone);
        }
    }

    @OnFocusChange(R.id.edtPhone)
    public void onFocusChange(View v, boolean hasFocus) {
        if (mBtnContinueView != null) {
            mBtnContinueView.setEnabled(mEdtPhoneView.isValid() && mPassCodeView.isValid());
        }
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {

        if (!mPassCodeView.isValid()) {
            mPassCodeView.setError(getString(R.string.invalid_pin));
            return;
        }

        if (!mEdtPhoneView.validate()) {
            return;
        }

        mPresenter.updateProfile(mPassCodeView.getText(), mEdtPhoneView.getText().toString().toLowerCase());
        ZPAnalytics.trackEvent(ZPEvents.OTP_LEVEL2_REQUEST);
    }
}
