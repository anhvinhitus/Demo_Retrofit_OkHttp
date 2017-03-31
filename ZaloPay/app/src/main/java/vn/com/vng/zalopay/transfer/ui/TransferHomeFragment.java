package vn.com.vng.zalopay.transfer.ui;

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

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;


/**
 * A fragment representing a list of Items.
 * <p>
 */
public class TransferHomeFragment extends BaseFragment implements
        TransferRecentAdapter.OnClickTransferRecentListener, ITransferHomeView {


    @Inject
    TransferHomePresenter presenter;

    private TransferRecentAdapter mAdapter;

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
        navigator.startTransferViaAccountName(this);
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
        mAdapter = new TransferRecentAdapter(getContext(), this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);

        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setHasFixedSize(true);
        mList.setAdapter(mAdapter);


        mTvTileTransactionRecent.setVisibility(View.GONE);
        mList.setVisibility(View.GONE);
        layoutIntroduction.setVisibility(View.VISIBLE);

        presenter.loadAnimationFromResource();
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
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    public void showError(String message) {
        showErrorDialog(message);
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

    @Override
    public void reloadIntroAnimation() {
        if (presenter != null) {
            presenter.loadAnimationFromResource();
        }
    }

    @Override
    public void setIntroductionAnimation(AnimationDrawable animationDrawable) {
        imgIntroduction.setBackground(animationDrawable);
    }

    @Override
    public void onItemRecentClick(RecentTransaction item) {

        TransferObject object = new TransferObject(item);
        object.activateSource = Constants.ActivateSource.FromTransferActivity;
        object.transferMode = Constants.TransferMode.TransferToZaloPayID;

        navigator.startActivityForResult(this, object, Constants.REQUEST_CODE_TRANSFER);
        ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_CHOOSERECENTTRANSACTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == Constants.REQUEST_CODE_TRANSFER) {
            getActivity().finish();
        } else if (requestCode == Constants.REQUEST_CODE_TRANSFER_VIA_ZALOPAYID) {
            getActivity().finish();
        }
    }
}
