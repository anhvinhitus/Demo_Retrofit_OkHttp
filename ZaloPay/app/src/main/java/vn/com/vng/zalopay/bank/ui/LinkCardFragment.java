package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.ViewGroup;

import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.LinkBankType;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by AnhHieu on 5/10/16.
 * *
 */
public class LinkCardFragment extends AbstractLinkBankFragment implements ILinkCardView {

    private ILinkCardListener mListener;
    private LinkCardAdapter mAdapter;

    @BindView(R.id.layoutLinkCardEmpty)
    View mLayoutLinkCardEmpty;

    @BindView(R.id.layoutContent)
    View mLayoutContent;

    @BindView(R.id.btn_add_more)
    View mBtnAddMore;

    @BindView(R.id.listView)
    SwipeMenuRecyclerView mRecyclerView;

//    @BindView(R.id.cardSupportLayout)
//    View mCardSupportLayout;
//
//    @OnClick(R.id.cardSupportLayout)
//    public void onClickBankSupport() {
//        mPresenter.getListBankSupport();
//    }

    @Override
    public void showListBankSupportDialog(ArrayList<ZPCard> cardSupportList) {
        BankSupportLinkCardDialog dialog = BankSupportLinkCardDialog.newInstance(cardSupportList);
        dialog.show(getChildFragmentManager(), BankSupportLinkCardDialog.TAG);
    }

    @Override
    public void gotoTabLinkAccAndReloadLinkedAcc() {
        if (mListener != null) {
            mListener.gotoTabLinkAccAndReloadLinkedAcc();
        }
    }

    @Override
    public void gotoTabLinkAccAndShowDialog(String message) {
        if (mListener != null) {
            mListener.gotoTabLinkAccAndShowDialog(message);
        }
    }

    @Override
    public void showConfirmPayAfterLinkBank(LinkBankType linkBankType) {
        super.showConfirmPayAfterLinkBank(linkBankType);
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
        mAdapter = new LinkCardAdapter(getContext());
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

        mRecyclerView.setSwipeMenuCreator(swipeMenuCreator);
        mRecyclerView.setSwipeMenuItemClickListener(menuItemClickListener);

        mPresenter.getListCard();
        initBankSupportFragment();
    }

    private void initBankSupportFragment() {
        if (getFragmentManager().findFragmentById(R.id.fragmentInLinkCard) == null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            BankSupportFragment bankSupportFragment = BankSupportFragment.newInstance(LinkBankType.LINK_BANK_CARD);
            ft.replace(R.id.fragmentInLinkCard, bankSupportFragment);
            ft.commit();
        }
    }

    private Fragment getBankSupportFragment() {
        return getFragmentManager().findFragmentById(R.id.fragmentInLinkCard);
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
//        initBankSupportFragment();
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
//            mCardSupportLayout.setPadding(0, 0, 0, (int) paddingBottom);
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
        mPresenter.destroy();
        DialogHelper.closeAllDialog();
        // break circular link between this and mAdapter
        mAdapter = null;
        super.onDestroy();
    }

    @Override
    public void refreshBanksSupport() {
        Timber.d("Refresh support banks");
        Fragment fragment = getBankSupportFragment();
        if (fragment != null && fragment instanceof BankSupportFragment) {
            ((BankSupportFragment) fragment).notifyDataChanged();
        }
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
    public void showNotificationDialog(String message) {
        super.showNotificationDialog(message);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CARD_SUPPORT) {
            if (resultCode == Activity.RESULT_OK) {
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private SwipeMenuCreator swipeMenuCreator = (swipeLeftMenu, swipeRightMenu, viewType) -> {
        int width = getResources().getDimensionPixelSize(R.dimen.link_card_remove_width);
        int height = ViewGroup.LayoutParams.MATCH_PARENT;

        SwipeMenuItem deleteItem = new SwipeMenuItem(getContext())
                .setBackgroundDrawable(R.color.red)
                .setText(getString(R.string.delete))
                .setTextColor(Color.WHITE)
                .setWidth(width)
                .setHeight(height);
        swipeRightMenu.addMenuItem(deleteItem);
    };

    private OnSwipeMenuItemClickListener menuItemClickListener = this::showConfirmRemoveSaveCard;

    private void showConfirmRemoveSaveCard(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
        super.showConfirmDialog(getString(R.string.txt_confirm_remove_card),
                getString(R.string.btn_confirm),
                getString(R.string.btn_cancel),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onOKevent() {
                        closeable.smoothCloseMenu();
                        if (menuPosition == 0 && direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                            /*mAdapter.remove(adapterPosition);
                            mAdapter.notifyItemRemoved(adapterPosition);*/
                            mPresenter.removeLinkCard(mAdapter.getItem(adapterPosition));
                            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_DELETECARD);
                        }
                    }

                    @Override
                    public void onCancelEvent() {
                        closeable.smoothCloseMenu();
                    }
                });
    }

    interface ILinkCardListener {
        void gotoTabLinkAccAndReloadLinkedAcc();

        void gotoTabLinkAccAndShowDialog(String message);
    }
}
