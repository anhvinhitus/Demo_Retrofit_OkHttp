package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.bank.models.LinkBankType;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link LinkAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LinkAccountFragment extends AbstractLinkBankFragment implements ILinkAccountView {

    private LinkAccountAdapter mAdapter;

    @BindView(R.id.layoutLinkAccountEmpty)
    View mLayoutLinkCardEmpty;

    @BindView(R.id.link_account_empty_view)
    View mEmptyViewImage;

    @BindView(R.id.layoutContent)
    View mLayoutContent;

    @BindView(R.id.btn_add_more)
    View mBtnAddMore;

    @BindView(R.id.listView)
    SwipeMenuRecyclerView mRecyclerView;

//    @BindView(R.id.txt_note_support_only_vcb)
//    TextView mTxtNoteSupportOnlyVcb;

    @BindView(R.id.link_account_tv_phone_require)
    TextView tvPhoneRequireToMapHint;

    @OnClick(R.id.btn_add_account)
    public void onClickAddBankAccount() {
        mPresenter.addLinkAccount();
    }

    @Inject
    LinkAccountPresenter mPresenter;

    @OnClick(R.id.btn_add_more)
    public void onClickAddMoreBankAccount() {
        mPresenter.addLinkAccount();
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
        mAdapter = new LinkAccountAdapter(getContext());
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
//        initBankSupportFragment();

        mRecyclerView.setSwipeMenuCreator(mSwipeMenuCreator);
        mRecyclerView.setSwipeMenuItemClickListener(mMenuItemClickListener);

        getLinkedBankAccount();
        setEmptyViewHeight();
    }

    public void getLinkedBankAccount() {
        if (mPresenter != null) {
            mPresenter.refreshLinkedBankAccount();
        }
    }

//    private void initBankSupportFragment() {
//        try {
//            if (getFragmentManager().findFragmentById(R.id.fragmentInLinkAccount) == null) {
//                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
//                BankSupportFragment bankSupportFragment = BankSupportFragment.newInstance(LinkBankType.LINK_BANK_ACCOUNT);
//                ft.replace(R.id.fragmentInLinkAccount, bankSupportFragment);
//                ft.commit();
//            }
//        } catch (IllegalStateException e) {
//            Timber.w(e, "initBankSupportFragment exception [%s]", e.getMessage());
//        }
//    }
//
//    private Fragment getBankSupportFragment() {
//        return getFragmentManager().findFragmentById(R.id.fragmentInLinkAccount);
//    }

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
//        initBankSupportFragment();
        mLayoutLinkCardEmpty.setVisibility(View.VISIBLE);
        mLayoutContent.setVisibility(View.GONE);
    }

    private void hideLayoutEmpty() {
        mLayoutLinkCardEmpty.setVisibility(View.GONE);
        mLayoutContent.setVisibility(View.VISIBLE);
        mBtnAddMore.setVisibility(View.GONE);
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
        BankSupportDialog listBankDialog = BankSupportDialog.newInstance(cardSupportList);
        listBankDialog.setTargetFragment(this, Constants.REQUEST_CODE_BANK_DIALOG);
        listBankDialog.show(getChildFragmentManager(), BankSupportDialog.TAG);
    }

    @Override
    public void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener) {
        if (!isAdded()) {
            return;
        }
        super.showRetryDialog(message, listener);
    }

    @Override
    public void showConfirmPayAfterLinkBank() {
        super.showConfirmPayAfterLinkBank(LinkBankType.LINK_BANK_ACCOUNT);
    }

    @Override
    public void showSupportVcbOnly() {
//        mTxtNoteSupportOnlyVcb.setVisibility(View.VISIBLE);
        mBtnAddMore.setEnabled(false);
    }

    @Override
    public void hideSupportVcbOnly() {
//        mTxtNoteSupportOnlyVcb.setVisibility(View.GONE);
        mBtnAddMore.setEnabled(true);
    }

    @Override
    public void refreshBanksSupport() {
//        Fragment fragment = getBankSupportFragment();
//        if (fragment != null && fragment instanceof BankSupportFragment) {
//            ((BankSupportFragment) fragment).notifyDataChanged();
//        }
    }

    @Override
    public void setPhoneRequireToMapHint(String strPhoneNumber) {
        if (tvPhoneRequireToMapHint == null) {
            return;
        }

        tvPhoneRequireToMapHint.setText(
                String.format(getResources().getString(R.string.link_account_empty_bank_support_phone_require_hint),
                        strPhoneNumber));
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

    private OnSwipeMenuItemClickListener mMenuItemClickListener = this::showConfirmRemoveSaveCard;

    private void showConfirmRemoveSaveCard(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
        if (mAdapter == null) {
            return;
        }
        BankAccount bankAccount = mAdapter.getItem(adapterPosition);
        if (bankAccount == null) {
            return;
        }
        String message;
        if (ECardType.PVCB.toString().equalsIgnoreCase(bankAccount.mBankCode)) {
            message = getString(R.string.txt_confirm_remove_vcb_account);
        } else {
            message = getString(R.string.txt_confirm_remove_account);
        }
        super.showConfirmDialog(message,
                getString(R.string.btn_confirm),
                getString(R.string.btn_cancel),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onOKevent() {
                        closeable.smoothCloseMenu();
                        if (menuPosition == 0 && direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                            /*mAdapter.remove(adapterPosition);
                            mAdapter.notifyItemRemoved(adapterPosition);*/
                            mPresenter.removeLinkAccount(bankAccount);
                            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_DELETECARD);
                        }
                    }

                    @Override
                    public void onCancelEvent() {
                        closeable.smoothCloseMenu();
                    }
                });
    }

    private void setEmptyViewHeight() {
        Window window = getActivity().getWindow();
        if (window == null) {
            return;
        }

        int screenHeight = AndroidUtils.displaySize.y;
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = (int) (screenHeight * 0.45);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.gravity = Gravity.CENTER;

        mEmptyViewImage.setLayoutParams(params);
    }
}
