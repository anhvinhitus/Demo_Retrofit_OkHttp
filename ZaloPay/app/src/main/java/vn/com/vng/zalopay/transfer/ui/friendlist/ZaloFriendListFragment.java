package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.view.View;
import android.widget.ListView;

import com.zalopay.ui.widget.MultiSwipeRefreshLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public class ZaloFriendListFragment extends BaseFragment implements IZaloFriendListView, SwipeRefreshLayout.OnRefreshListener {


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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ZaloFriendAdapter(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.setView(this);

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
        mPresenter.destroyView();
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
            try {
                String displayName = cursor.getString(2);
                Timber.d("onItemClick:  %s", displayName);
            } catch (Exception e) {
                //empty
            }
        }
    }


    @Override
    public void swapCursor(Cursor cursor) {
        mAdapter.swapCursor(cursor);
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
    public void onRefresh() {
        mPresenter.getFriendList();
    }

    @Override
    public void setRefreshing(boolean var) {
        mSwipeRefreshView.setRefreshing(var);
    }
}
