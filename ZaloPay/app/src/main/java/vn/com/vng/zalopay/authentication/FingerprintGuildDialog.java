package vn.com.vng.zalopay.authentication;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by hieuvm on 2/14/17.
 */

public class FingerprintGuildDialog extends DialogFragment {

    public static final String TAG = "FingerprintGuildDialog";

    public static FingerprintGuildDialog newInstance() {

        Bundle args = new Bundle();

        FingerprintGuildDialog fragment = new FingerprintGuildDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private Unbinder mUnbinder;

    @BindView(R.id.cancel_button)
    Button mCancelButton;

    @BindView(R.id.second_dialog_button)
    Button mSecondDialogButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.alert_dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.confirm));
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.fingerprint_dialog_guide, container, false);
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
        mUnbinder.unbind();
        super.onDestroyView();
    }
}
