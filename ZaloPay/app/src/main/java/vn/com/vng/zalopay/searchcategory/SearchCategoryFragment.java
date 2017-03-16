package vn.com.vng.zalopay.searchcategory;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.InsideApp;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.GridSpacingItemDecoration;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.utils.SearchViewFormatter;

/**
 * Created by khattn on 3/10/17.
 * Search Category Fragment
 */

public class SearchCategoryFragment extends BaseFragment implements ISearchCategoryView, SearchView.OnQueryTextListener,
        SearchListFriendAdapter.OnClickFriendListener, SearchListFriendAdapter.OnClickSeeMoreAppListener,
        SearchListAppAdapter.OnClickAppListener, SearchListAppAdapter.OnClickSeeMoreAppListener,
        ListInsideAppRecyclerAdapter.OnClickAppListener {

    public static SearchCategoryFragment newInstance(Bundle bundle) {
        SearchCategoryFragment fragment = new SearchCategoryFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_search_category;
    }

    private final static int SPAN_COUNT_APPLICATION = 3;

    @BindView(R.id.listView)
    RecyclerView mRecyclerView;

    @BindView(R.id.appListView)
    RecyclerView mAppRecyclerView;

    @BindView(R.id.friendListView)
    RecyclerView mFriendRecyclerView;

    @BindView(R.id.layoutNoResultFound)
    View mNoResultView;

    @BindView(R.id.fragmentSearchResult)
    View mSearchResultView;

    @BindView(R.id.layoutAppResult)
    View layoutAppResult;

    @BindView(R.id.layoutFriendResult)
    View layoutFriendResult;

    @Inject
    SearchCategoryPresenter mPresenter;

    private ListInsideAppRecyclerAdapter mAdapter;
    private SearchListAppAdapter mAppResultAdapter;
    private SearchListFriendAdapter mFriendResultAdapter;
    private SearchView mSearchView;

    private final Handler mHandler = new Handler();
    private Runnable mRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        initSearchView();
        mAdapter = new ListInsideAppRecyclerAdapter(getContext(), this);
        mAppResultAdapter = new SearchListAppAdapter(getContext(), this, this);
//        mFriendResultAdapter = new SearchListFriendAdapter(getContext(), this, this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(SPAN_COUNT_APPLICATION, 2, false));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setFocusable(false);

        mAppRecyclerView.setHasFixedSize(true);
        mAppRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAppRecyclerView.setAdapter(mAppResultAdapter);
        mAppRecyclerView.setFocusable(false);

//        mFriendRecyclerView.setHasFixedSize(true);
//        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//        mFriendRecyclerView.setAdapter(mFriendResultAdapter);
//        mFriendRecyclerView.setFocusable(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.getListAppResource();
//        mPresenter.getFriendList();
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
        showToast(message);
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    private void initSearchView() {
        getActivity().setTitle("");

        mSearchView = (SearchView) getActivity().findViewById(R.id.searchview);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        new SearchViewFormatter()
                .setIconFont(getActivity(),
                        R.string.general_search,
                        R.color.hint,
                        R.dimen.searchbox_icon,
                        true,
                        false)
                .setTextSize(14)
                .setTextColorResource(R.color.black)
                .setHintTextResource(R.string.search)
                .setHintColorResource(R.color.hint)
                .setCloseIconFont(getActivity(),
                        R.string.general_del,
                        R.color.hint,
                        R.dimen.searchbox_icon)
                .format(mSearchView);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mPresenter.filter(mAppResultAdapter, mFriendResultAdapter, s);
        mSearchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mHandler.removeCallbacks(mRunnable);
        mRunnable = () -> mPresenter.filter(mAppResultAdapter, mFriendResultAdapter, s);
        mHandler.postDelayed(mRunnable, 300);
        return true;
    }

    @Override
    public void showSearchResultView(boolean isShow) {
        if (mSearchResultView == null) {
            return;
        }

        if (isShow) {
            mSearchResultView.setVisibility(View.VISIBLE);

            if (mAppResultAdapter.getItems().size() == 0) {
                layoutAppResult.setVisibility(View.GONE);
            } else {
                layoutAppResult.setVisibility(View.VISIBLE);
            }


//            if (mFriendResultAdapter.getItems().size() == 0) {
//                layoutFriendResult.setVisibility(View.GONE);
//            } else {
//                layoutFriendResult.setVisibility(View.VISIBLE);
//            }
        } else {
            mSearchResultView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showNoResultView(boolean isShow) {
        if (mNoResultView == null) {
            return;
        }

        if (isShow) {
            mNoResultView.setVisibility(View.VISIBLE);
        } else {
            mNoResultView.setVisibility(View.GONE);
        }
    }

    @Override
    public void refreshInsideApps(List<InsideApp> list) {
        Timber.d("refreshInsideApps list: [%s]", list.size());
        if (mAdapter == null) {
            return;
        }
        mAdapter.setData(list);
    }

    @Override
    public void handleClickApp(InsideApp app, int position) {
        mPresenter.handleLaunchApp(app);
    }

    @Override
    public void handleClickFriend(ZaloFriend app, int position) {

    }

    @Override
    public void handleClickSeeMoreApp() {
        mPresenter.handleClickSeeMore(mAppResultAdapter);
    }

    @Override
    public void handleClickSeeMoreFriend() {
        mPresenter.handleClickSeeMore(mFriendResultAdapter);
    }

    @Override
    public void showConfirmDialog(String message,
                                  String btnConfirm,
                                  String btnCancel,
                                  ZPWOnEventConfirmDialogListener listener) {
        DialogHelper.showNoticeDialog(getActivity(), message, btnConfirm, btnCancel, listener);
    }
}