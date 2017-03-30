package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by AnhHieu on 5/10/16.
 * *
 */
public class LinkCardFragment extends BaseFragment implements ILinkCardView,
        LinkCardAdapter.OnClickBankCardListener, View.OnClickListener {

    private ILinkCardListener mListener;
    private Dialog mBottomSheetDialog;
    private BankCard mCurrentBankCard;
    private LinkCardAdapter mAdapter;
    private BankSupportFragment mBankSupportFragment;

    @BindView(R.id.layoutLinkCardEmpty)
    View mLayoutLinkCardEmpty;

    @BindView(R.id.layoutContent)
    View mLayoutContent;

    @BindView(R.id.btn_add_more)
    View mBtnAddMore;

    @BindView(R.id.listView)
    RecyclerView mRecyclerView;

    @BindView(R.id.cardSupportLayout)
    View mCardSupportLayout;

    @OnClick(R.id.cardSupportLayout)
    public void onClickBankSupport() {
        mPresenter.getListBankSupport();
    }

    @Override
    public void showListBankSupportDialog(ArrayList<ZPCard> cardSupportList) {
        BankSupportLinkCardDialog dialog = BankSupportLinkCardDialog.newInstance(cardSupportList);
        dialog.show(getChildFragmentManager(), BankSupportLinkCardDialog.TAG);
    }

    @Override
    public void gotoTabLinkAccount() {
        if (mListener != null) {
            mListener.gotoTabLinkAccount();
        }
    }

    @OnClick(R.id.btn_add_card)
    public void onClickAddBankCard() {
        mPresenter.addLinkCard();
    }

    @OnClick(R.id.btn_add_more)
    public void onClickAddMoreBankCard() {
        mPresenter.addLinkCard();
        ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_TAPADDCARD);
    }

    @Inject
    LinkCardPresenter mPresenter;

//    @BindView(R.id.progressContainer)
//    View mLoadingView;

    public static LinkCardFragment newInstance(Bundle bundle) {
        LinkCardFragment fragment = new LinkCardFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (ILinkCardListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnReviewSelectedListener");
        }
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_link_card;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAdapter = new LinkCardAdapter(getContext(), this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        mRecyclerView.setHasFixedSize(true);
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator != null) {
            if (animator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
            }
            //animator.setSupportsChangeAnimations(false);
        }
//        mRecyclerView.addItemDecoration(new SpacesItemDecoration(AndroidUtils.dp(12), AndroidUtils.dp(8)));
        mRecyclerView.setAdapter(mAdapter);

        initBankSupportFragment();
        initBottomSheet();
    }

    private void initBankSupportFragment() {
        if (getFragmentManager().findFragmentById(R.id.fragmentInLinkCard) == null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            mBankSupportFragment = BankSupportFragment.newInstance(false);
            ft.replace(R.id.fragmentInLinkCard, mBankSupportFragment);
            ft.commit();
        } else {
            mBankSupportFragment = (BankSupportFragment)
                    getFragmentManager().findFragmentById(R.id.fragmentInLinkCard);
        }
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPresenter != null) {
                mPresenter.getListCard();
            }
        }
    };

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            AndroidUtils.runOnUIThread(mRunnable, 200);
        } else {
            AndroidUtils.cancelRunOnUIThread(mRunnable);
        }
    }

    private void initBottomSheet() {
        if (mBottomSheetDialog == null) {
            mBottomSheetDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar);
            mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mBottomSheetDialog.setContentView(R.layout.bottom_sheet_link_card_layout);
            mBottomSheetDialog.setTitle("");
            final Window window = mBottomSheetDialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            View root = mBottomSheetDialog.findViewById(R.id.root);
            View layoutRemoveLink = mBottomSheetDialog.findViewById(R.id.layoutRemoveLink);

            root.setOnClickListener(this);
            layoutRemoveLink.setOnClickListener(this);
        }

        View layoutLinkCard = mBottomSheetDialog.findViewById(R.id.layoutLinkCard);
        ImageView imgLogo = (ImageView) mBottomSheetDialog.findViewById(R.id.iv_logo);
        if (mAdapter != null) {
            mAdapter.bindBankCard(layoutLinkCard, imgLogo, mCurrentBankCard, false);
        }
//        mBottomSheetDialog.show();
//        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//            }
//        });
    }

    private void showOrHideLinkCardEmpty() {
        if (mAdapter == null || mAdapter.getItemCount() <= 0) {
            showLinkCardEmpty();
        } else {
            hideLinkCardEmpty();
        }
    }

    private void showLinkCardEmpty() {
        Timber.d("Show layout link card empty.");
        if (mBankSupportFragment.getCountLinkCardSupport() <= 0
                || mBankSupportFragment.getCountLinkAccountSupport() <= 0) {
            mBankSupportFragment.getCardSupport();
        }
        mLayoutLinkCardEmpty.setVisibility(View.VISIBLE);
        mLayoutContent.setVisibility(View.GONE);
    }

    private void hideLinkCardEmpty() {
        if (mAdapter != null) {
            float paddingBottom;
            if (mAdapter.getItemCount() >= 3) {
                paddingBottom = getResources().getDimension(R.dimen.text_support_margin_small);
            } else {
                paddingBottom = getResources().getDimension(R.dimen.text_support_margin_normal);
            }
            mCardSupportLayout.setPadding(0, 0, 0, (int) paddingBottom);
        }
        mLayoutLinkCardEmpty.setVisibility(View.GONE);
        mLayoutContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        navigator.showSuggestionDialog(getActivity());
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
        if (mRunnable != null) {
            AndroidUtils.cancelRunOnUIThread(mRunnable);
            mRunnable = null;
        }
        mPresenter.destroy();
        if (mBottomSheetDialog != null && mBottomSheetDialog.isShowing()) {
            mBottomSheetDialog.dismiss();
        }
        mBottomSheetDialog = null;
        mCurrentBankCard = null;
        // break circular link between this and mAdapter
        mAdapter = null;
        mBankSupportFragment = null;
        super.onDestroy();
    }

    public void refreshLinkedCard() {
        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setData(List<BankCard> bankCards) {
        mAdapter.setData(bankCards);
        showOrHideLinkCardEmpty();
    }

    @Override
    public void updateData(BankCard bankCard) {
        mAdapter.insert(bankCard);
        showOrHideLinkCardEmpty();
    }

    @Override
    public void removeData(BankCard bankCard) {
        mAdapter.remove(bankCard);
        showOrHideLinkCardEmpty();
    }

    @Override
    public void onAddCardSuccess(DBaseMap card) {
        if (card == null) {
            return;
        }
        BankCard bankCard = new BankCard(card.getCardKey(), card.getFirstNumber(),
                card.getLastNumber(), card.bankcode);
        try {
            Timber.d("onAddCardSuccess first6CardNo: %s", card.getFirstNumber());
            bankCard.type = mPresenter.detectCardType(card.bankcode, card.getFirstNumber());
            Timber.d("onAddCardSuccess bankCard.type: %s", bankCard.type);
        } catch (Exception e) {
            Timber.w(e, "onAddCardSuccess detectCardType exception [%s]", e.getMessage());
        }
        updateData(bankCard);
    }

    @Override
    public void showWarningView(String error) {
        showWarningDialog(error, getString(R.string.txt_close), null);
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
    public void onClickMenu(BankCard bankCard) {
        mCurrentBankCard = bankCard;

        showBottomSheetDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CARD_SUPPORT) {
            if (resultCode == Activity.RESULT_OK) {
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        int itemId = v.getId();
        if (itemId == R.id.layoutRemoveLink) {
            showConfirmRemoveSaveCard();
            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_DELETECARD);
        } else if (itemId == R.id.root) {
            mBottomSheetDialog.dismiss();
        }
    }

    private void showConfirmRemoveSaveCard() {
        super.showConfirmDialog(getString(R.string.txt_confirm_remove_card),
                getString(R.string.btn_confirm),
                getString(R.string.btn_cancel),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onOKevent() {
                        mPresenter.removeLinkCard(mCurrentBankCard);
                        ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_DELETECARD);
                        mBottomSheetDialog.dismiss();
                    }

                    @Override
                    public void onCancelEvent() {
                    }
                });
    }

    private void showBottomSheetDialog() {
        if (mBottomSheetDialog == null) {
            initBottomSheet();
        }
        TextView tvCardNum = (TextView) mBottomSheetDialog.findViewById(R.id.tv_num_acc);
        if (mCurrentBankCard != null && tvCardNum != null) {
            tvCardNum.setText(BankUtils.formatBankCardNumber(mCurrentBankCard.first6cardno, mCurrentBankCard.last4cardno));
        }
        initBottomSheet();
        mBottomSheetDialog.show();
    }

    public interface ILinkCardListener {
        void gotoTabLinkAccount();
    }
}
