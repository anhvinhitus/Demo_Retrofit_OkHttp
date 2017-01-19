package vn.com.vng.zalopay.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by longlv on 1/19/17.
 * *
 */

public abstract class BaseDialogFragment extends DialogFragment {

    protected abstract void setupFragmentComponent();

    protected abstract int getResLayoutId();

    protected Unbinder mUnbinder;

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
        View view = inflater.inflate(getResLayoutId(), container, false);
        mUnbinder = ButterKnife.bind(this, view);
        setupFragmentComponent();
        return view;
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
