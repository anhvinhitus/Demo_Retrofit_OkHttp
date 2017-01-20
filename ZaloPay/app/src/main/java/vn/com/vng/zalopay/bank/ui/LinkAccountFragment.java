package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link LinkAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LinkAccountFragment extends BaseFragment implements ILinkAccountView {

    private Dialog mBottomSheetDialog;
    private BankAccount mCurrentBankAccount;
    private LinkAccountAdapter mAdapter;
    private BankSupportFragment mBankSupportFragment;

    @BindView(R.id.layoutLinkAccountEmpty)
    View mLayoutLinkCardEmpty;

    @BindView(R.id.layoutContent)
    View mLayoutContent;

    @BindView(R.id.btn_add_more)
    View mBtnAddMore;

    @BindView(R.id.listView)
    RecyclerView mRecyclerView;

    @OnClick(R.id.btn_add_account)
    public void onClickAddBankAccount() {
        mPresenter.showListBankSupportLinkAcc();
    }

    @Inject
    LinkAccountPresenter mPresenter;

    @OnClick(R.id.btn_add_more)
    public void onClickAddMoreBankAccount() {

    }

    public LinkAccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LinkAccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LinkAccountFragment newInstance() {
        return new LinkAccountFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_link_account;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAdapter = new LinkAccountAdapter(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator != null) {
            if (animator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
            }
        }
        mRecyclerView.setAdapter(mAdapter);

        mBankSupportFragment = (BankSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.bankSupportFragment);
        initBottomSheet();
    }

    private void initBottomSheet() {

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
        super.showErrorDialog(message, null);
    }

    private void showOrHideLayoutEmpty() {
        if (mAdapter == null || mAdapter.getItemCount() <= 0) {
            showLayoutEmpty();
        } else {
            hideLayoutEmpty();
        }
    }

    private void showLayoutEmpty() {
        if (mBankSupportFragment.getCountLinkCardSupport() <= 0
                || mBankSupportFragment.getCountLinkAccountSupport() <= 0) {
            mBankSupportFragment.getCardSupport();
        }
        mLayoutLinkCardEmpty.setVisibility(View.VISIBLE);
        mLayoutContent.setVisibility(View.GONE);
    }

    private void hideLayoutEmpty() {
        mLayoutLinkCardEmpty.setVisibility(View.GONE);
        mLayoutContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void refreshLinkedAccount(List<BankAccount> bankAccounts) {
        mAdapter.setData(bankAccounts);
        showOrHideLayoutEmpty();
    }

    @Override
    public void insertData(BankAccount bankAccounts) {
        mAdapter.insert(bankAccounts);
        showOrHideLayoutEmpty();
    }

    @Override
    public void removeData(BankAccount bankAccounts) {
        mAdapter.remove(bankAccounts);
        showOrHideLayoutEmpty();
    }

    @Override
    public void removeLinkAccount(BankAccount bankAccount) {

    }

    @Override
    public void onAddAccountSuccess(DBaseMap mappedCreditCard) {

    }

    @Override
    public void showListBankDialog(ArrayList<ZPCard> cardSupportList) {
        Timber.d("show list bank dialog.");
        ListBankDialog listBankDialog = ListBankDialog.newInstance(cardSupportList);
        listBankDialog.setTargetFragment(this, Constants.REQUEST_CODE_BANK_DIALOG);
        listBankDialog.show(getChildFragmentManager(), ListBankDialog.TAG);
    }

    @Override
    public void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener) {
        if (!isAdded()) {
            return;
        }
        super.showRetryDialog(message, listener);
    }

    @Override
    public void onEventUpdateVersion(boolean forceUpdate, String latestVersion, String message) {
        Timber.d("cardSupportHashMap forceUpdate [%s] latestVersion [%s] message [%s]",
                forceUpdate, latestVersion, message);
        if (!isAdded()) {
            return;
        }
        AppVersionUtils.handleEventUpdateVersion(getActivity(), forceUpdate, latestVersion, message);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_BANK_DIALOG) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    return;
                }
                ZPCard zpCard = data.getParcelableExtra(Constants.ARG_BANK);
                mPresenter.linkAccount(zpCard);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
}
