package vn.com.vng.zalopay.zpc.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.facebook.drawee.view.SimpleDraweeView;
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
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.react.model.ZPCViewMode;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.zpc.adapter.ZPCFavoriteAdapter;
import vn.com.vng.zalopay.zpc.model.ZpcViewType;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public class ContactListFragment extends RuntimePermissionFragment implements ContactListView,
        SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.listview)
    ListView mListView;
    @BindView(R.id.swipeRefresh)
    MultiSwipeRefreshLayout mSwipeRefreshView;
    @BindView(R.id.progressContainer)
    View mLoadingView;
    ZPCFavoriteAdapter mAdapter;
    @Inject
    ContactListPresenter mPresenter;
    @BindView(R.id.tv_empty)
    TextView mTvEmptyView;
    @BindView(R.id.ll_empty)
    LinearLayout mLlEmptyView;
    @BindView(R.id.edtSearch)
    ZPEditText mEdtSearchView;
    @BindView(R.id.switchKeyboard)
    IconFont mSwitchKeyboardView;
    @BindView(R.id.itemNumberNotSaveYet)
    LinearLayout mItemNumberNotSaveYet;
    @BindView(R.id.not_save_yet_tv_phone_number)
    TextView mTvNumberNotSave;
    @BindView(R.id.not_save_yet_tv_display_name)
    TextView mTvDisplayNameNotSave;
    @BindView(R.id.not_save_yet_iv_avatar)
    SimpleDraweeView mAvatar;

    @ZpcViewType
    private int mViewType = ZpcViewType.ZPC_All;
    private String mKeySearch = null;
    private String mNavigatorTitle = null;
    private String mViewMode = ZPCViewMode.keyboardABC;
    private String mPhoneNumber = null;
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

    public static ContactListFragment newInstance(Bundle args) {
        ContactListFragment fragment = new ContactListFragment();
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initArgs(savedInstanceState == null ? getArguments() : savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mAdapter = new ZPCFavoriteAdapter(getContext(), mPresenter);
        mAdapter.setOnSwipeLayoutListener(mSwipeListener);

        mListView.setDivider(null);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mOnScrollListener);

        mSwipeRefreshView.setSwipeableChildren(R.id.listview);
        mSwipeRefreshView.setOnRefreshListener(this);
        mSwipeRefreshView.setColorSchemeResources(R.color.back_ground_blue);

        if (!TextUtils.isEmpty(mKeySearch)) {
            mEdtSearchView.setText(mKeySearch);
            mSwipeRefreshView.setEnabled(false);
            mSwitchKeyboardView.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mNavigatorTitle)) {
            setTitle(mNavigatorTitle);
        }

        if (!TextUtils.isEmpty(mPhoneNumber)) {
            mEdtSearchView.setText(mPhoneNumber);
        }

        boolean isNumPad = mViewMode != null && mViewMode.equals(ZPCViewMode.keyboardPhone);
        setKeyboard(isNumPad);

        showLoading();

        focusEdtSearchView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.initialize(mEdtSearchView.getText().toString(), mViewType);
    }

    @SuppressWarnings("ResourceType")
    private void initArgs(Bundle bundle) {
        mViewType = bundle.getInt(BundleConstants.ZPC_VIEW_TYPE, ZpcViewType.ZPC_All);
        mKeySearch = bundle.getString(BundleConstants.KEY_SEARCH, "");
        mNavigatorTitle = bundle.getString(BundleConstants.NAVIGATION_TITLE, "");
        mViewMode = bundle.getString(BundleConstants.ZPC_VIEW_MODE, ZPCViewMode.keyboardABC);
        mPhoneNumber = bundle.getString(BundleConstants.PHONE_NUMBER, "");
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
            menuItem.setIcon(new FontIconDrawable(getString(R.string.personal_setting), Color.WHITE,
                    getResources().getDimensionPixelSize(R.dimen.menu_overflow_size)));
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
    public boolean onBackPressed() {
        AndroidUtils.hideKeyboard(getActivity());
        return super.onBackPressed();
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
            mPresenter.onSelectContactItem(this, (Cursor) item);
        }
    }

    @OnClick(R.id.switchKeyboard)
    public void onSwitchKeyboard() {
        boolean isNumPad = mViewMode != null && mViewMode.equals(ZPCViewMode.keyboardPhone);
        mViewMode = isNumPad ? ZPCViewMode.keyboardABC : ZPCViewMode.keyboardPhone;
        isNumPad = !isNumPad;
        setKeyboard(isNumPad);
        focusEdtSearchView();
    }

    @OnClick(R.id.itemNumberNotSaveYet)
    public void onClickItemNumberNotSaveYet(View view) {
        Object tag = view.getTag();
        if (tag instanceof ZPProfile) {
            mPresenter.onSelectContactItem(this, (ZPProfile) tag);
        }
    }

    @Override
    public void closeAllSwipeItems(AbsListView listView) {
        Timber.d("close all items");
        closeAllExcept(listView, null);
    }

    @Override
    public void showNotificationDialog() {
        if (mAdapter == null || mAdapter.getMaxFavorite() < 0) {
            return;
        }

        showNotificationDialog(getString(R.string.friend_favorite_maximum_format, mAdapter.getMaxFavorite()));
    }

    @Override
    public void closeAllSwipeItems() {
        if (mListView == null) {
            return;
        }

        closeAllSwipeItems(mListView);
    }

    @Override
    public Fragment getFragment() {
        return this;
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
            mLlEmptyView.setVisibility(View.VISIBLE);
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
        if (mAdapter != null && mAdapter.getCount() != 0) {
            mTvEmptyView.setText(null);
            mLlEmptyView.setVisibility(View.GONE);
            mItemNumberNotSaveYet.setVisibility(View.GONE);
            mTvNumberNotSave.setText(null);
            return;
        }

        if (mEdtSearchView.length() == 0) {
            mTvEmptyView.setText(R.string.friend_list_empty);
            mItemNumberNotSaveYet.setVisibility(View.GONE);
            mTvNumberNotSave.setText(null);
            mLlEmptyView.setVisibility(View.VISIBLE);
        } else {
            String phoneNumber = mEdtSearchView.getText().toString();
            if (PhoneUtil.isMobileNumber(phoneNumber)) {
                mPresenter.getUserInfoNotInZPC(phoneNumber);
            } else {
                mTvEmptyView.setText(R.string.no_result_your_search);
                mItemNumberNotSaveYet.setVisibility(View.GONE);
                mLlEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setTitle(String title) {
        Activity activity = getActivity();

        if (!(activity instanceof UserBaseToolBarActivity)) {
            return;
        }
        ((UserBaseToolBarActivity) activity).setTitle(title);
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
        if (mAdapter == null) {
            return;
        }

        mAdapter.setMaxFavorite(maxFavorite);
    }

    @Override
    public void requestReadContactsPermission() {
        isPermissionGrantedAndRequest(Manifest.permission.READ_CONTACTS, PERMISSION_CODE.READ_CONTACTS);
    }

    @Override
    public void setFavorite(List<FavoriteData> persons) {
        if (mAdapter == null) {
            return;
        }

        mAdapter.setFavorite(persons);
    }

    @Override
    public void focusEdtSearchView() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mEdtSearchView.postDelayed(() -> {
            mEdtSearchView.requestFocus();
            mEdtSearchView.setSelection(mEdtSearchView.getText().length()); // set cursor to end
            imm.showSoftInput(mEdtSearchView, 0);
        }, 200);
    }

    @Override
    public void setProfileNotInZPC(@NonNull ZPProfile profile) {
        String phone = mEdtSearchView.getText().toString();
        if (!phone.equals(profile.phonenumber)) {
            return;
        }

        mItemNumberNotSaveYet.setTag(profile);
        mItemNumberNotSaveYet.setVisibility(View.VISIBLE);
        mTvDisplayNameNotSave.setText(profile.displayName);

        if (!TextUtils.isEmpty(profile.phonenumber)) {
            String formattedNumber = PhoneUtil.formatPhoneNumberZPC(profile.phonenumber);
            mTvNumberNotSave.setText(formattedNumber);
        }

        if (!TextUtils.isEmpty(profile.avatar)) {
            mAvatar.setImageURI(profile.avatar);
        }
        mLlEmptyView.setVisibility(View.GONE);
    }

    @Override
    public void showDefaultProfileNotInZPC(@NonNull ZPProfile profile) {
        String phone = mEdtSearchView.getText().toString();
        if (!phone.equals(profile.phonenumber)) {
            return;
        }

        mItemNumberNotSaveYet.setTag(profile);
        if (PhoneUtil.isMobileNumber(profile.phonenumber)) {
            mItemNumberNotSaveYet.setVisibility(View.VISIBLE);
            mTvDisplayNameNotSave.setText(profile.phonenumber);
            mTvNumberNotSave.setText(getString(R.string.not_number_save_yet));
            mAvatar.setImageResource(R.drawable.ic_avatar_default);
            mLlEmptyView.setVisibility(View.GONE);
        } else {
            mItemNumberNotSaveYet.setVisibility(View.GONE);
            mTvEmptyView.setText(R.string.no_result_your_search);
            mLlEmptyView.setVisibility(View.VISIBLE);
        }

    }
}
