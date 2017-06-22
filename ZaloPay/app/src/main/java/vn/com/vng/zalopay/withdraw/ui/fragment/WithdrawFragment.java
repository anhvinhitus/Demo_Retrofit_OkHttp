package vn.com.vng.zalopay.withdraw.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.withdraw.ui.adapter.WithdrawAdapter;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawView;


public class WithdrawFragment extends BaseFragment implements IWithdrawView, WithdrawAdapter.OnClickDenominationListener {

    private final static int SPAN_COUNT_APPLICATION = 2;

    public static WithdrawFragment newInstance() {

        Bundle args = new Bundle();

        WithdrawFragment fragment = new WithdrawFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.listview)
    RecyclerView listview;

    @BindView(R.id.tvMoney)
    TextView mMoneyView;

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_withdraw;
    }

    @Inject
    WithdrawPresenter mPresenter;

    WithdrawAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new WithdrawAdapter(this);
        mAdapter.setSpanCount(SPAN_COUNT_APPLICATION);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        listview.setHasFixedSize(true);
        listview.addItemDecoration(new VerticalGridCardSpacingDecoration());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION);
        gridLayoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup());
        listview.setLayoutManager(gridLayoutManager);
        listview.setAdapter(mAdapter);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.loadView();
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
    public void showError(String message) {
        showErrorDialog(message);
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void setBalance(long balance) {
        mMoneyView.setText(CurrencyUtil.formatCurrency(balance, false));
        mAdapter.setBalance(balance);

    }

    @Override
    public void addDenominationMoney(List<Long> val) {
        mAdapter.insertItems(val);
    }

    @Override
    public void finish(int result) {
        getActivity().setResult(result);
        getActivity().finish();
    }

    @Override
    public void onClickDenomination(long money) {
        mPresenter.withdraw(money);
    }
}
