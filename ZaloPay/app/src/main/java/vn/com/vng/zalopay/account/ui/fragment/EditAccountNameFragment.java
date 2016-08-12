package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

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

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_edit_account_name_layout;
    }
}
