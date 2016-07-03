package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendDao;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;
import vn.com.vng.zalopay.transfer.provider.ZaloFriendContentProviderImpl;
import vn.com.vng.zalopay.transfer.ui.adapter.ZaloContactRecyclerViewAdapter;
import vn.com.vng.zalopay.transfer.ui.presenter.ZaloContactPresenter;
import vn.com.vng.zalopay.transfer.ui.view.IZaloContactView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {}
 * interface.
 */
public class ZaloContactFragment extends BaseFragment implements IZaloContactView,
        ZaloContactRecyclerViewAdapter.OnItemInteractionListener,
        ZaloContactRecyclerViewAdapter.OnLoadMoreListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private final int LOADER_ZALO_FRIEND = 2;
    private final int PAGE_SIZE = 50;
    private final String LIMIT_ITEMS = "limit_items";
    private final String TEXT_SEARCH = "text_search";
    private int mColumnCount = 1;
    private ZaloContactRecyclerViewAdapter mAdapter;
    private Bundle mTransferState;
    private CountDownTimer mSearchTimer;
    private int mCurrentItem = 0;

    @Inject
    Navigator navigator;

    @Inject
    ZaloContactPresenter presenter;

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

        mAdapter = new ZaloContactRecyclerViewAdapter(getContext(), this, this);
        mAdapter.setLinearLayoutManager((LinearLayoutManager) mRecyclerView.getLayoutManager());
        mAdapter.setRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefresh.setOnRefreshListener(this);

        initSearchTimer();
        presenter.retrieveZaloFriendsAsNeeded();
    }

    private void initSearchTimer() {
        mSearchTimer = new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                String textSearch = edtSearch.getText().toString();
                Bundle bundle = new Bundle();
                bundle.putString(TEXT_SEARCH, textSearch);
                getLoaderManager().restartLoader(LOADER_ZALO_FRIEND, bundle, ZaloContactFragment.this);
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getLoaderManager().initLoader(LOADER_ZALO_FRIEND, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getLoaderManager().destroyLoader(LOADER_ZALO_FRIEND);
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
        mAdapter = null;
        mRecyclerView = null;
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
    public void onItemClick(ZaloFriend zaloFriend) {
        if (mTransferState == null) {
            mTransferState = new Bundle();
        }
        mTransferState.putParcelable(Constants.ARG_ZALO_FRIEND, zaloFriend);
        navigator.startTransferActivity(this, mTransferState);
    }

    public void onZaloFriendUpdated() {
        Timber.d("onZaloFriendUpdated");
        mSwipeRefresh.setRefreshing(false);
    }

    public void onGetZaloFriendFinish() {
        Timber.d("onGetZaloFriendFinish");
        mSwipeRefresh.setRefreshing(false);
        if (mAdapter != null) {
            getLoaderManager().restartLoader(LOADER_ZALO_FRIEND, new Bundle(), this);
        }
    }

    @Override
    public void onGetZaloFriendTimeout() {
        onGetZaloFriendError();
    }

    public void onGetZaloFriendError() {
        Timber.d("onGetZaloContactError");
        hideLoading();
        mSwipeRefresh.setRefreshing(false);
        mAdapter.setMoreLoading(false);
        mAdapter.setProgressMore(false);
        if (!AndroidUtils.checkNetwork(getContext())) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int limitItem = PAGE_SIZE;
        String txtSearch = "";
        if (args != null) {
            limitItem = args.getInt(LIMIT_ITEMS, PAGE_SIZE);
            txtSearch = args.getString(TEXT_SEARCH, "");
        }
        mCurrentItem = limitItem;
        String selection = "";
        if (!TextUtils.isEmpty(txtSearch)) {
            selection += ZaloFriendDao.Properties.Fulltextsearch.columnName + " like '%" + txtSearch.toLowerCase() + "%'";
        }
        String orderByWithLimit = ZaloFriendDao.Properties.Fulltextsearch.columnName +
                " ASC" +
                " LIMIT " +
                limitItem;
        Timber.d("onCreateLoader, selection: %s", selection);
        Timber.d("onCreateLoader, orderByWithLimit: %s", orderByWithLimit);
        return new CursorLoader(getActivity(), ZaloFriendContentProviderImpl.CONTENT_URI, null, selection, null, orderByWithLimit);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Timber.d("onLoadFinished.... cursor: %s", cursor);
        List<ZaloFriend> zaloFriends = convertCursorToList(cursor);
        if (zaloFriends == null || zaloFriends.size() <= 0) {
            onGetDataDBEmpty();
        } else {
            onGetDataDBSuccess(zaloFriends);
        }
        mSwipeRefresh.setRefreshing(false);
        mAdapter.setMoreLoading(false);
        mAdapter.setProgressMore(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Timber.d("onLoaderReset");
        if (mAdapter != null) {
            mAdapter.setData(new ArrayList<ZaloFriend>());
        }
    }

    private List<ZaloFriend> convertCursorToList(Cursor cursor) {
        List<ZaloFriend> transferRecents = new ArrayList<>();
        if (cursor == null || cursor.getCount() <= 0) {
            Timber.d("onLoadFinished.... cursor null/empty");
            return transferRecents;
        }
        Timber.d("convertCursorToList.... cursor: %s", cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                transferRecents.add(new ZaloFriend(cursor));
            } while (cursor.moveToNext());
        }
        return transferRecents;
    }

    private void onGetDataDBSuccess(List<ZaloFriend> zaloFriends) {
        hideLoading();
        if (mAdapter == null) {
            return;
        }
        mAdapter.setData(zaloFriends);
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
        mAdapter.setData(null);
        viewSeparate.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        Timber.d("onRefresh start");
        presenter.getFriendListServer();
    }

    @Override
    public void onLoadMore() {
        Timber.d("onLoadMore start");
        final int itemsCount = mAdapter == null ? 0 : mAdapter.getItemCount();
        Timber.d("onLoadMore itemsCount:%s", itemsCount);
        Timber.d("onLoadMore mCurrentItem:%s", mCurrentItem);
        if ((itemsCount + 1) < PAGE_SIZE || mCurrentItem > itemsCount) {
            Timber.d("onLoadMore cancel");
            return;
        }
        Timber.d("onLoadMore show progress");
        mAdapter.setProgressMore(true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Bundle bundle = new Bundle();
                mCurrentItem = (itemsCount + 1) + PAGE_SIZE;
                bundle.putInt(LIMIT_ITEMS, mCurrentItem);
                bundle.putString(TEXT_SEARCH, edtSearch.getText().toString());
                getLoaderManager().restartLoader(LOADER_ZALO_FRIEND, bundle, ZaloContactFragment.this);
            }
        }, 500);
    }
}
