package vn.com.vng.zalopay.bank.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
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

    @Override
    public void setData(List<BankAccount> list) {

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
