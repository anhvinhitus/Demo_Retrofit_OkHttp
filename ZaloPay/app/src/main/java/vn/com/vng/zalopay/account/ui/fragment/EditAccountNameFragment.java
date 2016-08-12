package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.InputZaloPayNameView;

/**
 * Created by AnhHieu on 8/12/16.
 */
public class EditAccountNameFragment extends BaseFragment {

    public static EditAccountNameFragment newInstance() {

        Bundle args = new Bundle();

        EditAccountNameFragment fragment = new EditAccountNameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @BindView(R.id.btnCheck)
    View mBtnCheckView;

    @BindView(R.id.inputZaloPayName)
    InputZaloPayNameView mInputAccountNameView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnCheckView.setEnabled(mInputAccountNameView.getString().length() != 0);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_edit_account_name_layout;
    }


    @OnClick(R.id.btnCheck)
    public void onClickCheck(View v) {
        
    }
}
