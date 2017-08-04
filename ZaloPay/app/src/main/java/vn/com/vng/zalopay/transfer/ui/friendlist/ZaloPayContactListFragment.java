package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.shamanland.fonticon.FontIconDrawable;
import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.MultiSwipeRefreshLayout;
import com.zalopay.ui.widget.edittext.ZPEditText;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.BundleConstants;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public class ZaloPayContactListFragment extends RuntimePermissionFragment implements IZaloFriendListView,
        SwipeRefreshLayout.OnRefreshListener,
        OnFavoriteListener {


    public static ZaloPayContactListFragment newInstance(Bundle args) {
        ZaloPayContactListFragment fragment = new ZaloPayContactListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_zalopay_contact_list;
    }

    @BindView(R.id.listview)
    ListView mListView;

    @BindView(R.id.swipeRefresh)
    MultiSwipeRefreshLayout mSwipeRefreshView;

    @BindView(R.id.progressContainer)
    View mLoadingView;

    ZPCFavoriteAdapter mAdapter;

    @Inject
    ZaloPayContactListPresenter mPresenter;

    @BindView(R.id.tv_empty)
    TextView mTvEmptyView;

    @BindView(R.id.iv_empty)
    View mEmptyView;

    @BindView(R.id.edtSearch)
    ZPEditText mEdtSearchView;

    @BindView(R.id.switchKeyboard)
    IconFont mSwitchKeyboardView;

    @ZpcViewType
    private int mViewType = ZpcViewType.ZPC_All;
    private String mKeySearch = null;
    private boolean mIsNumberPad = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new ZPCFavoriteAdapter(getContext(), this);
        mAdapter.setOnSwipeLayoutListener(mSwipeListener);

        initArgs(savedInstanceState == null ? getArguments() : savedInstanceState);
    }

    @SuppressWarnings("ResourceType")
    private void initArgs(Bundle bundle) {
        mViewType = bundle.getInt(BundleConstants.ZPC_VIEW_TYPE, ZpcViewType.ZPC_All);
        mKeySearch = bundle.getString(BundleConstants.KEY_SEARCH, "");
        mIsNumberPad = bundle.getBoolean(BundleConstants.NUMBER_KEYBOARD, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);

        mListView.setDivider(null);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mOnScrollListener);

        mSwipeRefreshView.setSwipeableChildren(R.id.listview);
        mSwipeRefreshView.setOnRefreshListener(this);

        if (!TextUtils.isEmpty(mKeySearch)) {
            mEdtSearchView.setText(mKeySearch);
            mSwipeRefreshView.setEnabled(false);
            mSwitchKeyboardView.setVisibility(View.GONE);
        }

        if (mIsNumberPad) {
            setKeyboard(true);
        }

        showLoading();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.initialize(mEdtSearchView.getText().toString(), mViewType);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contact_list, menu);
        MenuItem menuItem = menu.findItem(R.id.action_update);
        if (menuItem != null) {
            menuItem.setIcon(new FontIconDrawable(getString(R.string.personal_setting), Color.WHITE, AndroidUtils.dp(20)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_update) {
            navigator.startSyncContact(getContext());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BundleConstants.ZPC_VIEW_TYPE, mViewType);
        //  outState.putBoolean(BundleConstants.NUMBER_KEYBOARD, mIsNumberPad);
    }

    @Override
    public void onDestroyView() {
        mAdapter.setOnSwipeLayoutListener(null);
        mListView.setOnScrollListener(null);
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

    private void setKeyboard(boolean isNumberPad) {
        if (isNumberPad) {
            mSwitchKeyboardView.setIcon(R.string.ct_list_ABC);
            mEdtSearchView.setInputType(EditorInfo.TYPE_CLASS_PHONE | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        } else {
            mSwitchKeyboardView.setIcon(R.string.ct_list_123);
            mEdtSearchView.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS);
        }
    }

    @OnTextChanged(value = R.id.edtSearch, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable editable) {
        String keySearch = Strings.trim(editable.toString());
        mSwipeRefreshView.setEnabled(editable.length() == 0);
        mSwitchKeyboardView.setVisibility(editable.length() == 0 ? View.VISIBLE : View.GONE);
        mPresenter.doSearch(keySearch);
    }

    @OnItemClick(R.id.listview)
    public void onItemClick(android.widget.AdapterView<?> parent, View v, int position, long id) {
        Timber.d("onItemClick: position %s", position);
        Object item = mAdapter.getItem(position - 1);
        if (item instanceof Cursor) {
            mPresenter.clickItemContact(this, (Cursor) item);
        }
    }

    @OnClick(R.id.switchKeyboard)
    public void onSwitchKeyboard(View view) {
        mIsNumberPad = !mIsNumberPad;
        setKeyboard(mIsNumberPad);
    }

    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                return;
            }

            closeAllSwipeItems(view);
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    };

    private SwipeLayout.SwipeListener mSwipeListener = new SimpleSwipeListener() {
        @Override
        public void onStartOpen(SwipeLayout layout) {
            closeAllExcept(mListView, layout);
        }
    };

    protected void closeAllSwipeItems(AbsListView listView) {
        Timber.d("close all items");
        closeAllExcept(listView, null);
    }

    protected void closeAllExcept(AbsListView listView, @Nullable SwipeLayout layout) {
        if (listView == null) {
            return;
        }

        int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = listView.getChildAt(i);
            if (!(view instanceof SwipeLayout)) {
                continue;
            }

            SwipeLayout swipeLayout = (SwipeLayout) view;

            if (swipeLayout == layout) {
                continue;
            }

            if (swipeLayout.getOpenStatus() != SwipeLayout.Status.Close) {
                swipeLayout.close();
            }
        }
    }

    @Override
    public void swapCursor(Cursor cursor) {
        mAdapter.setKeySearch(mEdtSearchView.getText().toString());
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

        if (mAdapter.getCount() <= 0) {
            mTvEmptyView.setText(message);
            mEmptyView.setVisibility(View.GONE);
        }
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
        if (mAdapter.getCount() != 0) {
            mTvEmptyView.setText(null);
            mEmptyView.setVisibility(View.GONE);
            return;
        }

        if (mEdtSearchView.length() == 0) {
            mTvEmptyView.setText(R.string.friend_list_empty);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mTvEmptyView.setText(R.string.no_result_your_search);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSubTitle(String subTitle) {
        Activity activity = getActivity();

        if (!(activity instanceof UserBaseToolBarActivity)) {
            return;
        }
        ((UserBaseToolBarActivity) activity).setSubTitle(subTitle);
    }

    @Override
    public void setMaxFavorite(int maxFavorite) {
        if (mAdapter != null) {
            mAdapter.setMaxFavorite(maxFavorite);
        }
    }

    @Override
    public void requestReadContactsPermission() {
        isPermissionGrantedAndRequest(Manifest.permission.READ_CONTACTS, PERMISSION_CODE.READ_CONTACTS);
    }

    @Override
    public void setFavorite(List<FavoriteData> persons) {
        if (mAdapter != null) {
            mAdapter.setFavorite(persons);
        }
    }

    @Override
    public void onRemoveFavorite(FavoriteData f) {
        mPresenter.favorite(false, f);
        closeAllSwipeItems(mListView);
      /*  mAdapter.notifyDataSetChanged();*/
    }

    @Override
    public void onAddFavorite(FavoriteData f) {
        mPresenter.favorite(true, f);
    }

    @Override
    public void onMaximumFavorite() {
        showNotificationDialog(getString(R.string.friend_favorite_maximum_format, mAdapter.getMaxFavorite()));
    }
}
