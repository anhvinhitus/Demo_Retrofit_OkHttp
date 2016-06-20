package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

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
        ZaloContactPresenter.IZaloFriendListener,
        ZaloContactRecyclerViewAdapter.OnItemInteractionListener,
        UltimateRecyclerView.OnLoadMoreListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private final int LOADER_ZALO_FRIEND = 2;
    private final int PAGE_SIZE = 50;
    private final String LIMIT_ITEMS = "limit_items";
    private final String TEXT_SEARCH = "text_search";
    private int mColumnCount = 1;
    private ZaloContactRecyclerViewAdapter mAdapter;
    private Bundle mTransferState;

    @Inject
    Navigator navigator;

    @Inject
    ZaloContactPresenter presenter;

    @BindView(R.id.list)
    UltimateRecyclerView mList;

    @BindView(R.id.edtSearch)
    EditText edtSearch;

    @BindView(R.id.viewSeparate)
    View viewSeparate;

    @OnTextChanged(R.id.edtSearch)
    public void onTextChangedEdtSearch(CharSequence charSequence) {
        Bundle bundle = new Bundle();
        if (charSequence != null) {
            bundle.putString(TEXT_SEARCH, charSequence.toString());
        }
        getLoaderManager().restartLoader(LOADER_ZALO_FRIEND, bundle, this);
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
            mList.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            mList.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
        }
        mAdapter = new ZaloContactRecyclerViewAdapter(getContext(), new ArrayList<ZaloFriend>(), this);
        mList.setAdapter(mAdapter);
        presenter.getFriendList(this);
        mList.reenableLoadmore();
        mList.setOnLoadMoreListener(this);
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
        mList = null;
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
    public void onGetZaloFriendSuccess(List<ZaloFriend> zaloFriends) {
//        hideLoading();
        if (mAdapter != null) {
            getLoaderManager().restartLoader(LOADER_ZALO_FRIEND, new Bundle(), this);
        }
    }

    @Override
    public void onItemClick(ZaloFriend zaloFriend) {
        if (mTransferState == null) {
            mTransferState = new Bundle();
        }
        mTransferState.putParcelable(Constants.ARG_ZALO_FRIEND, zaloFriend);
        navigator.startTransferActivity(this, mTransferState);
    }

    @Override
    public void onGetZaloContactError() {
        hideLoading();
        if (!AndroidUtils.checkNetwork(getContext())) {
            SweetAlertDialog.OnSweetClickListener cancelListener = new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    getActivity().finish();
                    sweetAlertDialog.cancel();
                }
            };
            SweetAlertDialog.OnSweetClickListener retryListener = new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    presenter.getFriendList(ZaloContactFragment.this);
                    sweetAlertDialog.cancel();
                }
            };
            showRetryDialog(getString(R.string.exception_no_connection), getString(R.string.txt_close), cancelListener, getString(R.string.txt_retry), retryListener);
        } else {
            showErrorDialog(getString(R.string.get_zalo_contact_error), getString(R.string.txt_close), new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    if (mAdapter == null || mAdapter.getItemCount() <= 0) {
                        getActivity().finish();
                    }
                    sweetAlertDialog.cancel();
                }
            });
        }
    }

    @Override
    public void loadMore(int itemsCount, int maxLastVisiblePosition) {
        Timber.d("loadMore, itemsCount: %s maxLastVisiblePosition: %s", itemsCount, maxLastVisiblePosition);
        if (itemsCount <= maxLastVisiblePosition) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(LIMIT_ITEMS, itemsCount + PAGE_SIZE);
        bundle.putString(TEXT_SEARCH, edtSearch.getText().toString());
        getLoaderManager().restartLoader(LOADER_ZALO_FRIEND, bundle, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int limitItem = PAGE_SIZE;
        String txtSearch = "";
        if (args != null) {
            limitItem = args.getInt(LIMIT_ITEMS, PAGE_SIZE);
            txtSearch = args.getString(TEXT_SEARCH, "");
        }
        String selection = "";
        if (!TextUtils.isEmpty(txtSearch)) {
            selection+= ZaloFriendDao.Properties.DisplayName.columnName + " like '%" + txtSearch.toLowerCase() + "%'";
        }
        String orderByWithLimit = ZaloFriendDao.Properties.DisplayName.columnName +
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
}
