package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnTextChanged;
import de.greenrobot.dao.query.LazyList;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.ui.adapter.ZaloContactRecyclerViewAdapter;
import vn.com.vng.zalopay.transfer.ui.presenter.ZaloContactPresenter;
import vn.com.vng.zalopay.transfer.ui.view.IZaloContactView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {}
 * interface.
 */
public class ZaloContactFragment extends BaseFragment implements IZaloContactView,
        ZaloContactRecyclerViewAdapter.OnItemInteractionListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private ZaloContactRecyclerViewAdapter mAdapter;
    private Bundle mTransferState;
    private CountDownTimer mSearchTimer;

    @Inject
    Navigator navigator;

    @Inject
    ZaloContactPresenter presenter;

    @Inject
    FriendStore.Repository mFriendRepository;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    @BindView(R.id.edtSearch)
    EditText edtSearch;

    @BindView(R.id.viewSeparate)
    View viewSeparate;

    @OnTextChanged(R.id.edtSearch)
    public void onTextChangedEdtSearch() {
        if (mSearchTimer != null) {
            mSearchTimer.cancel();
            mSearchTimer.start();
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ZaloContactFragment() {
    }

    @SuppressWarnings("unused")
    public static ZaloContactFragment newInstance(int columnCount) {
        ZaloContactFragment fragment = new ZaloContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_zalo_contact;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        // Set the adapter
        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
        }

        mAdapter = new ZaloContactRecyclerViewAdapter(getContext(), null, this);
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefresh.setOnRefreshListener(this);

        initSearchTimer();
        presenter.retrieveZaloFriendsAsNeeded();
        presenter.getFriedListFromDB(null);
    }

    private void initSearchTimer() {
        mSearchTimer = new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                getFriedListFromDB();
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onPause() {
        presenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        mAdapter.setLazyList(null);
        mAdapter = null;
        mRecyclerView = null;
        if (mSearchTimer != null) {
            mSearchTimer.cancel();
            mSearchTimer = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_TRANSFER) {
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data != null) {
                    mTransferState = data.getExtras();
                } else {
                    mTransferState = null;
                }
                return;
            } else if (resultCode == Activity.RESULT_OK) {
                getActivity().setResult(Activity.RESULT_OK, null);
                getActivity().finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showLoading() {
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
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
    public void onItemClick(ZaloFriendGD zaloFriendGD) {
        if (zaloFriendGD == null) {
            return;
        }
        if (mTransferState == null) {
            mTransferState = new Bundle();
        }
        ZaloFriend zaloFriend = mFriendRepository.convertZaloFriendGD(zaloFriendGD);
        mTransferState.putParcelable(Constants.ARG_ZALO_FRIEND, zaloFriend);
        navigator.startTransferActivity(this, mTransferState);
    }

    @Override
    public void onGetZaloFriendFinish(LazyList<ZaloFriendGD> items) {
        Timber.d("onGetZaloFriendFinish.... items: %s", items);
        if (items == null || items.size() <= 0) {
            onGetDataDBEmpty();
        } else {
            onGetDataDBSuccess(items);
        }
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onGetZaloFriendFinish() {
        Timber.d("onGetZaloFriendFinish");
        mSwipeRefresh.setRefreshing(false);
        getFriedListFromDB();
    }

    @Override
    public void onGetZaloFriendTimeout() {
        onGetZaloFriendError();
    }

    public void onGetZaloFriendError() {
        Timber.d("onGetZaloContactError");
        hideLoading();
        mSwipeRefresh.setRefreshing(false);
        if (!NetworkHelper.isNetworkAvailable(getContext())) {
            SweetAlertDialog.OnSweetClickListener cancelListener = new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.cancel();
                }
            };
            SweetAlertDialog.OnSweetClickListener retryListener = new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    mSwipeRefresh.setRefreshing(true);
                    presenter.retrieveZaloFriendsAsNeeded();
                    sweetAlertDialog.cancel();
                }
            };
            showRetryDialog(getString(R.string.exception_no_connection_try_again), getString(R.string.txt_close), cancelListener, getString(R.string.txt_retry), retryListener);
        } else {
            showErrorDialog(getString(R.string.get_zalo_contact_error), getString(R.string.txt_close), new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.cancel();
                }
            });
        }
    }

    private void getFriedListFromDB() {
        String txtSearch = null;
        if (edtSearch.getText() != null) {
            txtSearch = edtSearch.getText().toString();
        }
        Timber.d("getFriedListFromDB, txtSearch: %s", txtSearch);
        if (!TextUtils.isEmpty(txtSearch)) {
            presenter.getFriedListFromDB(txtSearch.toLowerCase());
        } else {
            presenter.getFriedListFromDB(null);
        }
    }

    private void onGetDataDBSuccess(LazyList<ZaloFriendGD> zaloFriends) {
        Timber.d("onGetDataDBSuccess mAdapter %s", mAdapter);
        hideLoading();
        if (mAdapter == null) {
            return;
        }
        mAdapter.setLazyList(zaloFriends);
        if (zaloFriends != null && zaloFriends.size() > 0) {
            viewSeparate.setVisibility(View.VISIBLE);
        } else {
            viewSeparate.setVisibility(View.GONE);
        }
    }

    private void onGetDataDBEmpty() {
        hideLoading();
        if (mAdapter == null) {
            return;
        }
        mAdapter.setLazyList(null);
        viewSeparate.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        Timber.d("onRefresh start");
        presenter.getFriendListServer();
    }

}
