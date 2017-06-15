package vn.com.zalopay.wallet.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chucvv on 6/12/17.
 */

public abstract class BaseFragment extends Fragment {
    public final String TAG = getClass().getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onArguments();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        onViewBound(view);
        onDataBound(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onUnBound();
    }

    protected void onArguments() {
    }

    protected abstract int getLayoutId();

    /***
     * ready for initialize views
     * @param view
     */
    protected abstract void onViewBound(View view);

    /***
     * view ready for start bind data
     * @param view
     */
    protected abstract void onDataBound(View view);

    protected abstract void onUnBound();
}
