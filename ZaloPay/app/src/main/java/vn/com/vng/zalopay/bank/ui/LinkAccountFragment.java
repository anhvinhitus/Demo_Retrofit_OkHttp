package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.listener.OnClickBankAccListener;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link LinkAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LinkAccountFragment extends BaseFragment implements ILinkAccountView,
        OnClickBankAccListener {

    private Dialog mBottomSheetDialog;
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
        mPresenter.addBankAccount();
    }

    @Inject
    LinkAccountPresenter mPresenter;

    @OnClick(R.id.btn_add_more)
    public void onClickAddMoreBankAccount() {
        mPresenter.addBankAccount();
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
    public static LinkAccountFragment newInstance(Bundle bundle) {
        LinkAccountFragment fragment = new LinkAccountFragment();
        fragment.setArguments(bundle);
        return fragment;
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
        mAdapter = new LinkAccountAdapter(getContext(), this);
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPresenter != null) {
                mPresenter.getMapBankAccount();
            }
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Timber.d("setUserVisibleHint visible[%s]", isVisibleToUser);
        if (isVisibleToUser) {
            AndroidUtils.runOnUIThread(mRunnable, 200);
        } else {
            AndroidUtils.cancelRunOnUIThread(mRunnable);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mPresenter.initData(getArguments());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator != null) {
            if (animator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
            }
        }
        mRecyclerView.setAdapter(mAdapter);
        initBankSupportFragment();
    }

    private void initBankSupportFragment() {
        if (getFragmentManager().findFragmentById(R.id.fragmentInLinkAccount) == null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            mBankSupportFragment = BankSupportFragment.newInstance(false);
            ft.replace(R.id.fragmentInLinkAccount, mBankSupportFragment);
            ft.commit();
        } else {
            mBankSupportFragment = (BankSupportFragment)
                    getFragmentManager().findFragmentById(R.id.fragmentInLinkAccount);
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
        Timber.d("Show layout link account empty.");
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
    public void refreshLinkedAccount() {
        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            mAdapter.notifyDataSetChanged();
        }
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
    public void showListBankDialog(ArrayList<ZPCard> cardSupportList) {
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
    public void showConfirmPayAfterLinkAcc() {
        DialogHelper.showNoticeDialog(getActivity(),
                getString(R.string.confirm_continue_pay),
                getString(R.string.btn_continue),
                getString(R.string.btn_cancel_transaction),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        getActivity().setResult(Constants.RESULT_END_PAYMENT);
                        getActivity().finish();
                    }

                    @Override
                    public void onOKevent() {
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_BANK_DIALOG) {
            Timber.d("onActivityResult REQUEST_CODE_BANK_DIALOG");
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    return;
                }
                ZPCard zpCard = data.getParcelableExtra(Constants.ARG_BANK);
                mPresenter.linkAccountIfNotExist(zpCard);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        Timber.d("onResume");
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
        if (mRunnable != null) {
            AndroidUtils.cancelRunOnUIThread(mRunnable);
            mRunnable = null;
        }
        mPresenter.destroy();
        super.onDestroy();
    }

    private void showBottomSheet(final BankAccount bankAccount) {
        if (mBottomSheetDialog == null) {
            mBottomSheetDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar);
            mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mBottomSheetDialog.setContentView(R.layout.bottom_sheet_link_acc_layout);
            mBottomSheetDialog.setTitle("");
            final Window window = mBottomSheetDialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }

        View root = mBottomSheetDialog.findViewById(R.id.root);
        View layoutRemoveLink = mBottomSheetDialog.findViewById(R.id.layoutRemoveLink);
        View verticalLine = mBottomSheetDialog.findViewById(R.id.line);

        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemId = v.getId();
                if (itemId == R.id.layoutRemoveLink) {
                    showConfirmRemoveSaveCard(bankAccount);
                    ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_DELETECARD);
                } else if (itemId == R.id.root) {
                    mBottomSheetDialog.dismiss();
                }
            }
        };

        root.setOnClickListener(mOnClickListener);
        layoutRemoveLink.setOnClickListener(mOnClickListener);

        ImageView imgLogo = (ImageView) mBottomSheetDialog.findViewById(R.id.iv_logo);
        if (mAdapter != null) {
            mAdapter.bindBankAccount(verticalLine, imgLogo, bankAccount);
        }

        TextView tvAccountName = (TextView) mBottomSheetDialog.findViewById(R.id.tvAccountName);
        if (bankAccount != null && tvAccountName != null) {
            tvAccountName.setText(bankAccount.getAccountNo());
        }
        mBottomSheetDialog.show();
    }

    @Override
    public void onClickBankAccount(BankAccount bankAccount) {
        showBottomSheet(bankAccount);
    }

    private void showConfirmRemoveSaveCard(final BankAccount bankAccount) {
        super.showConfirmDialog(getString(R.string.txt_confirm_remove_account),
                getString(R.string.btn_confirm),
                getString(R.string.btn_cancel),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onOKevent() {
                        mPresenter.removeLinkAccount(bankAccount);
                        ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_DELETECARD);
                        mBottomSheetDialog.dismiss();
                    }

                    @Override
                    public void onCancelEvent() {
                    }
                });
    }
}
