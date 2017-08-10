package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.recyclerview.HorizontalDividerDecoration;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.merchant.entities.ZPBank;

/**
 * Created by datnt10 on 5/25/17.
 * Fragment list bank support
 */

public class BankSupportSelectionFragment extends BaseFragment implements IBankSupportSelectionView
        , BankSupportSelectionAdapter.OnClickBankSupportListener {

    private static final int PADDING_ITEM = 60;
    @BindView(R.id.listview)
    RecyclerView mListView;
    @BindView(R.id.progressContainer)
    View mLoadingView;
    @Inject
    BankSupportSelectionPresenter mPresenter;
    private BankSupportSelectionAdapter mAdapter;

    public static BankSupportSelectionFragment newInstance(Bundle args) {
        BankSupportSelectionFragment fragment = new BankSupportSelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_recycleview;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new BankSupportSelectionAdapter(getContext(), this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListView.addItemDecoration(new HorizontalDividerDecoration(getActivity(), AndroidUtils.dp(PADDING_ITEM), R.drawable.line_divider));
        mListView.setAdapter(mAdapter);
        mLoadingView.setVisibility(View.GONE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.listBankSupport();
        mPresenter.loadMaxCcLinNum();
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
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void setData(List<ZPBank> banks) {
        mAdapter.setData(banks);
    }

    @Override
    public void showDialogThenClose(String message, String cancelText, int dialogType) {
        if (dialogType == SweetAlertDialog.ERROR_TYPE) {
            super.showErrorDialog(message, cancelText, () -> getActivity().finish());
        } else if (dialogType == SweetAlertDialog.WARNING_TYPE) {
            super.showWarningDialog(message, cancelText, () -> getActivity().finish());
        } else if (dialogType == SweetAlertDialog.NO_INTERNET) {
            super.showNetworkErrorDialog(i -> getActivity().finish());
        }
    }

    @Override
    public void showError(String message) {
        showErrorDialog(message);
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
    public void showMessageDialog(String message, ZPWOnEventDialogListener closeDialogListener) {
        DialogHelper.showCustomDialog(getActivity(), message, getString(R.string.btn_retry_select_bank), SweetAlertDialog.INFO_TYPE, closeDialogListener);
    }

    @Override
    public void showWarningDialog(String message, String btnText) {
        DialogHelper.showWarningDialog(getActivity(), message, btnText, null);
    }

    @Override
    public void onClickBankSupport(ZPBank card) {
        mPresenter.linkBank(card);
    }

    @Override
    public boolean onBackPressed() {
        ZPAnalytics.trackEvent(ZPEvents.LINKBANK_ADD_TOUCH_BACK);
        return super.onBackPressed();
    }
}
