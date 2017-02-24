package vn.com.vng.zalopay.authentication;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by hieuvm on 2/14/17.
 */

public class FingerprintSuggestDialog extends DialogFragment {

    interface OnClickListener {
        void onClick(DialogFragment dialog, int which);
    }

    public static final String TAG = "FingerprintSuggestDialog";

    public static FingerprintSuggestDialog newInstance() {

        Bundle args = new Bundle();

        FingerprintSuggestDialog fragment = new FingerprintSuggestDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private Unbinder mUnbinder;

    @BindView(R.id.cancel_button)
    Button mCancelButton;

    @BindView(R.id.second_dialog_button)
    Button mSecondDialogButton;

    @BindView(R.id.checkBox)
    AppCompatCheckBox mShowSuggestCheckBox;

    @Inject
    SharedPreferences mPreferences;

    @Inject
    KeyTools mKeytool;

    private String mHashPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AlertDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.confirm));
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.fingerprint_dialog_suggest, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        setupFragmentComponent();
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCancelButton.setText(R.string.txt_close);
        mCancelButton.setVisibility(View.VISIBLE);
        mSecondDialogButton.setText(R.string.accept);
        mSecondDialogButton.setVisibility(View.VISIBLE);
    }

    private void setupFragmentComponent() {
        UserComponent userComponent = AndroidApplication.instance().getUserComponent();
        if (userComponent != null) {
            userComponent.inject(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setWidthDialog();
    }

    private void setWidthDialog() {
        if (getDialog() == null) {
            return;
        }

        Window window = getDialog().getWindow();
        if (window == null) {
            return;
        }

        int widthScreen = AndroidUtils.displaySize.x;
        int width = (int) (widthScreen * 0.85D);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        window.setLayout(width, height);
    }

    @Override
    public void onDestroyView() {
        Timber.d("onDestroyView: ");
        saveConfigShowSuggest();
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @OnClick(R.id.cancel_button)
    public void onCancelClick(View v) {
        dismiss();
    }

    @OnClick(R.id.second_dialog_button)
    public void onSecondClick(View v) {
        Timber.d("onSecondClick: %s", mHashPassword);
        mKeytool.updatePassword(mHashPassword);
        dismiss();
    }

    private void saveConfigShowSuggest() {
        mPreferences.edit()
                .putBoolean(Constants.PREF_SHOW_FINGERPRINT_SUGGEST, !mShowSuggestCheckBox.isChecked())
                .apply();
    }

    public void setPassword(String hashPassword) {
        this.mHashPassword = hashPassword;
    }
}
