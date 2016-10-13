package vn.com.vng.zalopay.linkcard.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.GridSpacingItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link AssociatedBankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssociatedBankFragment extends BaseFragment {
    private final static int COLUMN_COUNT = 3;

    @BindView(R.id.bankRecyclerView)
    RecyclerView mRecyclerView;

    private AssociatedBankAdapter mAdapter;

    public AssociatedBankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AssociatedBankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AssociatedBankFragment newInstance() {
        return new AssociatedBankFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_associated_bank;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new AssociatedBankAdapter(getContext());
        mAdapter.setData(PaymentAppConfig.APP_RESOURCE_LIST);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(COLUMN_COUNT, 2, false));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setFocusable(false);
    }
}
