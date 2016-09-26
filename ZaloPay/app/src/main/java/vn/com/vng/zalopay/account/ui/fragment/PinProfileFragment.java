package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
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
import com.zalopay.ui.widget.layout.OnKeyboardStateChangeListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.PinProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IPinProfileView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.ClearableEditText;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.IPassCodeFocusChanged;
import vn.com.vng.zalopay.ui.widget.IPassCodeMaxLength;
import vn.com.vng.zalopay.ui.widget.PassCodeView;
import vn.com.vng.zalopay.ui.widget.intro.CircleEraser;
import vn.com.vng.zalopay.ui.widget.intro.IntroProfileView;
import vn.com.vng.zalopay.ui.widget.intro.RectangleEraser;
import vn.com.vng.zalopay.ui.widget.intro.ViewTarget;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.ValidateUtil;
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
public class PinProfileFragment extends BaseFragment implements IPinProfileView {
    private OnPinProfileFragmentListener mListener;

    @Inject
    PinProfilePresenter presenter;

    @BindView(R.id.rootView)
    KeyboardFrameLayout rootView;

    @BindView(R.id.ScrollView)
    ScrollView mScrollView;

    @BindView(R.id.passcodeInput)
    PassCodeView passCode;

    @BindView(R.id.textInputPhone)
    TextInputLayout textInputPhone;

    @BindView(R.id.edtPhone)
    ClearableEditText edtPhone;

    @BindView(R.id.tvTermsOfUser3)
    TextView tvTermsOfUser3;

    @OnTextChanged(R.id.edtPhone)
    public void onTextChangedPhone() {
        hidePhoneError();
        checkShowHideBtnContinue();
    }

    @BindView(R.id.btnContinue)
    View btnContinue;

    private void showPhoneError() {
        if (textInputPhone == null) {
            return;
        }
        textInputPhone.setErrorEnabled(true);
        if (TextUtils.isEmpty(ClearableEditText.optText(edtPhone))) {
            textInputPhone.setError(getString(R.string.invalid_phone_empty));
        } else {
            textInputPhone.setError(getString(R.string.invalid_phone));
        }
        edtPhone.setBackgroundResource(R.drawable.txt_bottom_error_style);
    }

    private void hidePhoneError() {
        if (textInputPhone == null) {
            return;
        }
        textInputPhone.setErrorEnabled(false);
        textInputPhone.setError(null);
        if (edtPhone.isFocused()) {
            edtPhone.setBackgroundResource(R.drawable.txt_bottom_default_focused);
        } else {
            edtPhone.setBackgroundResource(R.drawable.txt_bottom_default_normal);
        }
    }

    public boolean isValidPhone() {
        if (edtPhone == null) {
            return false;
        }

        String phone = edtPhone.getString();
        return !TextUtils.isEmpty(phone) && ValidateUtil.isMobileNumber(phone);
    }

    private boolean isValidPin() {
        if (passCode == null) {
            return false;
        }

        String pin = passCode.getText();
        return !TextUtils.isEmpty(pin) && pin.length() == passCode.getMaxLength();
    }

    IPassCodeFocusChanged passcodeFocusChanged = new IPassCodeFocusChanged() {
        @Override
        public void onFocusChangedPin(boolean isFocus) {
            if (isFocus) {
                return;
            }
            if (passCode == null) {
                return;
            }

            if (!isValidPin()) {
                passCode.showError(getString(R.string.invalid_pin));
            } else {
                passCode.hideError();
            }
        }
    };

    IPassCodeMaxLength passCodeMaxLength = new IPassCodeMaxLength() {
        @Override
        public void hasMaxLength() {
            Timber.d("hasMaxLength");
            if (edtPhone != null) {
                edtPhone.requestFocus();
            }
        }
    };

    TextWatcher passcodeChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (passCode == null) {
                return;
            }

            if (isValidPin()) {
                passCode.hideError();
            }
            checkShowHideBtnContinue();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public PinProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PinProfileFragment.
     */
    public static PinProfileFragment newInstance() {
        return new PinProfileFragment();
    }

    private void checkShowHideBtnContinue() {
        if (isShowBtnContinue()) {
            btnContinue.setBackgroundResource(R.drawable.bg_btn_blue);
            btnContinue.setOnClickListener(mOnClickContinueListener);
        } else {
            btnContinue.setBackgroundResource(R.color.bg_btn_gray);
            btnContinue.setOnClickListener(null);
        }
    }

    private boolean isShowBtnContinue() {
        if (!isValidPin()) {
            //passCode.showError(getString(R.string.invalid_pin));
            return false;
        } else {
            if (passCode != null) {
                passCode.hideError();
            }
        }

        if (!isValidPhone()) {
            //showPhoneError();
            return false;
        } else {
            hidePhoneError();
        }
        return true;
    }

    private View.OnClickListener mOnClickContinueListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickContinue();
        }
    };

    public void onClickContinue() {
        presenter.updateProfile(passCode.getText(), edtPhone.getString().toLowerCase());
        ZPAnalytics.trackEvent(ZPEvents.UPDATEPROFILE2_ZPN_EMPTY);
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_pin_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);

        passCode.setPassCodeMaxLength(passCodeMaxLength);
        passCode.addTextChangedListener(passcodeChanged);
        passCode.setPassCodeFocusChanged(passcodeFocusChanged);

        edtPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Timber.d("EdtPhone onFocusChange focus %s", hasFocus);
                if (hasFocus) {
                    int[] location = new int[2];
                    edtPhone.getLocationInWindow(location);
                    if (mScrollView!= null) {
                        Timber.d("edtPhone onFocusChange y [%s]", (location[1] - AndroidUtils.dp(100)));
                        mScrollView.smoothScrollBy(0, (location[1] - AndroidUtils.dp(100)));
                    }
                    edtPhone.setBackgroundResource(R.drawable.txt_bottom_default_focused);
                    return;
                }
                if (!isValidPhone()) {
                    showPhoneError();
                } else {
                    hidePhoneError();
                }
            }
        });

        AndroidUtils.setSpannedMessageToView(tvTermsOfUser3, R.string.agree_term_of_use, R.string.term_of_use,
                false, false, R.color.colorPrimary,
                new ClickableSpanNoUnderline() {
                    @Override
                    public void onClick(View widget) {
                        navigator.startTermActivity(getContext());
                    }
                });

        rootView.setOnKeyboardStateListener(new OnKeyboardStateChangeListener() {
            @Override
            public void onKeyBoardShow(int height) {
                if (mScrollView == null) {
                    return;
                }
                Timber.d("onKeyBoardShow: passCode.isFocused() %s", passCode.isFocused());
                Timber.d("onKeyBoardShow: edtPhone.isFocused() %s", edtPhone.isFocused());
                int[] location = new int[2];
                if (passCode.isFocused()) {
                    Timber.d("onKeyBoardShow scroll to Top");
                    mScrollView.smoothScrollBy(0, 0);
                } else if (edtPhone.isFocused()) {
                    edtPhone.getLocationInWindow(location);
                    Timber.d("onKeyBoardShow: edtPhone.y %s", location[1]);
                    mScrollView.smoothScrollBy(0, (location[1] - AndroidUtils.dp(100)));
                }
            }

            @Override
            public void onKeyBoardHide() {
                Timber.d("onKeyBoardHide");
            }
        });

        AndroidUtils.runOnUIThread(mIntroRunnable, 300);
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
        presenter.resume();
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
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

        passCode.hideError();
        hidePhoneError();


        getActivity().getWindow().getDecorView().clearFocus();

        IntroProfileView intro = new IntroProfileView(getActivity(), new IntroProfileView.IIntroListener() {
            @Override
            public void hideIntroListener() {
            }
        });

        intro.addShape(new RectangleEraser(new ViewTarget(passCode.getPassCodeView()), AndroidUtils.dp(4f)));
        intro.addShape(new CircleEraser(new ViewTarget(passCode.getButtonShow())));
        intro.addShape(new RectangleEraser(new ViewTarget(edtPhone), AndroidUtils.dp(4f)));

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPinProfileFragmentListener {
        void onUpdatePinSuccess(String phone);
    }
}
