package vn.com.zalopay.wallet.dialog;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import vn.com.zalopay.wallet.R;

/*
 * Created by lytm on 14/07/2017.
 */

public abstract class BaseDialogFragment extends DialogFragment {

    protected abstract void initData();

    protected abstract void initViews(View v);

    protected abstract int getLayout();

    protected abstract int getLayoutSize();

    protected abstract int getWidthLayout();

    protected abstract void getArgument();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CoffeeDialog);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getArgument();
        initData();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() == null || getDialog().getWindow() == null) {
            return;
        }
        if (getLayoutSize() > 0) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, getLayoutSize());
        } else {
            getDialog().getWindow().setLayout(getWidthLayout(), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

    }
}
