package vn.com.vng.zalopay.feedback;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class FeedbackFragment extends BaseFragment {

    public FeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WarningRootedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Override
    protected void setupFragmentComponent() {
//        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_send_feedback;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
