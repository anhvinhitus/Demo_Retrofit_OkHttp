package vn.com.vng.zalopay.requestsupport;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

import static android.app.Activity.RESULT_OK;

public class ChooseCategoryFragment extends BaseFragment implements
        ChooseCategoryAdapter.OnClickAppListener, IChooseCategoryView {

    public static ChooseCategoryFragment newInstance() {
        return new ChooseCategoryFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getAppComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_recycleview;
    }

    private ChooseCategoryAdapter mAdapter;

    @BindView(R.id.listview)
    RecyclerView mRecyclerView;

    @BindView(R.id.progressContainer)
    View mLoadingView;

    @Inject
    ChooseCategoryPresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mAdapter = new ChooseCategoryAdapter(getContext(), this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.fetchAppResource();
    }

    @Override
    public void onClickAppListener(AppResource app) {
        Intent intent = new Intent();
        intent.putExtra("category", app.appname);
        getActivity().setIntent(intent);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        mRecyclerView.setAdapter(null);
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
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
    public void setData(List<AppResource> data) {
        mAdapter.setData(data);
    }
}
