package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.utility.PlayStoreUtils;
import vn.com.zalopay.wallet.constants.BankStatus;
import vn.com.zalopay.wallet.merchant.entities.ZPBank;

/**
 * Created by datnt10 on 5/25/17.
 * Fragment list bank support
 */

public class BankSupportSelectionFragment extends BaseFragment implements IBankSupportSelectionView
        , BankSupportSelectionAdapter.OnClickBankSupportListener {

    @BindView(R.id.bank_support_selection_list_bank)
    RecyclerView rcvListBankSupport;
    @BindView(R.id.bank_support_selection_dash_line)
    View mDashLine;
    @Inject
    BankSupportSelectionPresenter presenter;
    private BankSupportSelectionAdapter mAdapter;

    public static BankSupportSelectionFragment newInstance() {
        Bundle args = new Bundle();
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
        return R.layout.fragment_bank_support_selection;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);

        mAdapter = new BankSupportSelectionAdapter(getContext(), this);

        rcvListBankSupport.setHasFixedSize(true);
        rcvListBankSupport.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcvListBankSupport.setNestedScrollingEnabled(false);
        //mBankRecyclerView.addItemDecoration(new GridSpacingItemDecoration(COLUMN_COUNT, 2, false));
        rcvListBankSupport.setAdapter(mAdapter);
        rcvListBankSupport.setFocusable(false);

        presenter.getBankSupport();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onPause() {
        presenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void fetchListBank(List<ZPBank> cardSupportList) {
        if (!isAdded()) {
            Timber.d("Refresh Bank Supports error because fragment didn't add.");
            return;
        }
        hideProgressDialog();
        if (mAdapter == null) {
            Timber.d("Refresh Bank Supports error because adapter is null.");
            return;
        }

        if (Lists.isEmptyOrNull(cardSupportList)) {
            mAdapter.setData(Collections.emptyList());
            mDashLine.setVisibility(View.GONE);
        } else {
            mAdapter.setData(cardSupportList);
            mDashLine.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showRetryDialog(String retryMessage, ZPWOnEventConfirmDialogListener retryListener) {
        if (!isAdded()) {
            return;
        }
        super.showRetryDialog(retryMessage, retryListener);
    }

    @Override
    public void showDialogThenClose(String message, String cancelText, int dialogType) {
        ZPWOnEventDialogListener onClickCancel = () -> getActivity().finish();
        if (dialogType == SweetAlertDialog.ERROR_TYPE) {
            super.showErrorDialog(message, cancelText, onClickCancel);
        } else if (dialogType == SweetAlertDialog.WARNING_TYPE) {
            super.showWarningDialog(message, cancelText, onClickCancel);
        } else if (dialogType == SweetAlertDialog.NO_INTERNET) {
            super.showNetworkErrorDialog(i -> getActivity().finish());
        }
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
    public void onClickBankSupportListener(ZPBank card, int position) {
        if(!NetworkHelper.isNetworkAvailable(getActivity())) {
            super.showNetworkErrorDialog();
            return;
        }

        if (card.bankStatus == BankStatus.MAINTENANCE) {
            showMessageDialog(card.bankMessage, null);
        } else if (card.bankStatus == BankStatus.UPVERSION) {
            showConfirmDialog(card.bankMessage, getString(R.string.txt_update), getString(R.string.txt_close), new ZPWOnEventConfirmDialogListener() {
                @Override
                public void onCancelEvent() {
                }

                @Override
                public void onOKevent() {
                    PlayStoreUtils.openPlayStoreForUpdate(getActivity(), vn.com.vng.zalopay.BuildConfig.PACKAGE_IN_PLAY_STORE,
                            AndroidApplication.instance().getResources().getString(R.string.app_name), "force-app-update", "bank-future");
                }
            });
        } else if (card.isBankAccount()) {
            presenter.linkAccount(card.bankCode);
        } else {
            presenter.linkCard();
        }
    }
}
