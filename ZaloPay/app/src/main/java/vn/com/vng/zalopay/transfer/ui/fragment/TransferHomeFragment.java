package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.transfer.ui.adapter.TransferRecentRecyclerViewAdapter;
import vn.com.vng.zalopay.transfer.ui.presenter.TransferHomePresenter;
import vn.com.vng.zalopay.transfer.ui.view.ITransferHomeView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * A fragment representing a list of Items.
 * <p>
 */
public class TransferHomeFragment extends BaseFragment implements
        TransferRecentRecyclerViewAdapter.OnTransferRecentItemListener, ITransferHomeView {


    @Inject
    TransferHomePresenter presenter;

    private TransferRecentRecyclerViewAdapter mAdapter;

    @BindView(R.id.tvTileTransactionRecent)
    View mTvTileTransactionRecent;

    @BindView(R.id.list)
    RecyclerView mList;

    @BindView(R.id.viewSeparate)
    View viewSeparate;

    @BindView(R.id.layoutIntroduction)
    View layoutIntroduction;

    @BindView(R.id.imgIntroduction)
    ImageView imgIntroduction;

    @OnClick(R.id.layoutTransferAccZaloPay)
    public void onClickTransferAccZaloPay() {
        navigator.startZaloContactActivity(this);
    }

    @OnClick(R.id.layoutTransferViaAccount)
    public void onClickTransferViaAccountName() {

    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TransferHomeFragment() {
    }

    @SuppressWarnings("unused")
    public static TransferHomeFragment newInstance() {
        TransferHomeFragment fragment = new TransferHomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_transfer_home;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new TransferRecentRecyclerViewAdapter(getContext(), new ArrayList<RecentTransaction>(), this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);

        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setHasFixedSize(true);
        mList.setAdapter(mAdapter);


        mTvTileTransactionRecent.setVisibility(View.GONE);
        mList.setVisibility(View.GONE);
        layoutIntroduction.setVisibility(View.VISIBLE);


        imgIntroduction.setBackgroundResource(R.drawable.anim_transfer);
        AnimationDrawable animationDrawable = (AnimationDrawable) imgIntroduction.getBackground();
        animationDrawable.start();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.getRecent();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == vn.com.vng.zalopay.Constants.REQUEST_CODE_TRANSFER) {
            if (resultCode == Activity.RESULT_OK) {
                getActivity().setResult(Activity.RESULT_OK, null);
                getActivity().finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(RecentTransaction item) {
        if (item == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(vn.com.vng.zalopay.Constants.ARG_TRANSFERRECENT, Parcels.wrap(item));
        navigator.startTransferActivity(this, bundle);
    }

    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
    }

    @Override
    public void showRetry() {
    }

    @Override
    public void hideRetry() {
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void setData(List<RecentTransaction> list) {
        mAdapter.setData(list);
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        if (mAdapter.getItemCount() > 0) {
            mTvTileTransactionRecent.setVisibility(View.VISIBLE);
            mList.setVisibility(View.VISIBLE);
            viewSeparate.setVisibility(View.VISIBLE);
            layoutIntroduction.setVisibility(View.GONE);
            imgIntroduction.clearAnimation();
        } else {
            mTvTileTransactionRecent.setVisibility(View.GONE);
            mList.setVisibility(View.GONE);
            viewSeparate.setVisibility(View.GONE);
            layoutIntroduction.setVisibility(View.VISIBLE);
        }
    }
}
