package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.zalopay.ui.widget.MultiSwipeRefreshLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public class ZaloFriendListFragment extends RuntimePermissionFragment implements IZaloFriendListView, SwipeRefreshLayout.OnRefreshListener {


    public static ZaloFriendListFragment newInstance() {

        Bundle args = new Bundle();

        ZaloFriendListFragment fragment = new ZaloFriendListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_zalo_friend_list;
    }

    @BindView(R.id.listview)
    ListView mListView;

    @BindView(R.id.swipeRefresh)
    MultiSwipeRefreshLayout mSwipeRefreshView;

    @BindView(R.id.progressContainer)
    View mLoadingView;

    ZaloFriendAdapter mAdapter;

    @Inject
    ZaloFriendListPresenter mPresenter;

    @BindView(R.id.tv_empty)
    TextView mEmptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ZaloFriendAdapter(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);

        mListView.setDivider(null);

        mListView.setAdapter(mAdapter);
        mSwipeRefreshView.setSwipeableChildren(R.id.listview);
        mSwipeRefreshView.setOnRefreshListener(this);

        showLoading();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.getFriendList();
        isPermissionGrantedAndRequest(Manifest.permission.READ_CONTACTS, PERMISSION_CODE.READ_CONTACTS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_TRANSFER:
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mListView.setAdapter(null);
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Cursor cursor = mAdapter.getCursor();

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        mPresenter.destroy();
        super.onDestroy();
    }

    @OnTextChanged(value = R.id.edtSearch, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable editable) {
        String keySearch = Strings.trim(editable.toString());
        Timber.d("afterTextChanged keySearch %s", keySearch);

        mPresenter.doSearch(keySearch);
    }

    @OnItemClick(R.id.listview)
    public void onItemClick(android.widget.AdapterView<?> parent, View v, int position, long id) {
        Timber.d("onItemClick: position %s", position);
        Object item = mAdapter.getItem(position);
        if (item instanceof Cursor) {
            Cursor cursor = (Cursor) item;
            mPresenter.startTransfer(this, cursor);
        }
    }


    @Override
    public void swapCursor(Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mLoadingView.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void onRefresh() {
        mPresenter.refreshFriendList();
    }

    @Override
    public void setRefreshing(boolean var) {
        mSwipeRefreshView.setRefreshing(var);
    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
        if (!isGranted) {
            return;
        }

        switch (permissionRequestCode) {
            case PERMISSION_CODE.READ_CONTACTS:
                mPresenter.syncContact();
                break;
        }

    }

    @Override
    public void checkIfEmpty() {
        if (mAdapter.getCount() == 0) {
            mEmptyView.setText(R.string.no_data);
        } else {
            mEmptyView.setText(null);
        }
    }
}
