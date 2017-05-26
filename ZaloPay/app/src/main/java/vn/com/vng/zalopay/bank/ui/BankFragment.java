package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.zalopay.ui.widget.IconFontDrawable;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;

/**
 * Created by datnt10 on 5/25/17.
 * Fragment bank: handle bank ui and events
 */

public class BankFragment extends BaseFragment implements IBankView, BankAdapter.IBankListener {
    public static BankFragment newInstance() {
        Bundle args = new Bundle();
        BankFragment fragment = new BankFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    BankPresenter mPresenter;

    BankAdapter mAdapter;

    @BindView(R.id.link_card_empty_view)
    View mEmptyViewImage;

    @BindView(R.id.listView)
    SwipeMenuRecyclerView mRecyclerView;

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_bank;
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
        mAdapter = new BankAdapter(getContext(), mPresenter.getCurrentUser(), this);
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

        mRecyclerView.setSwipeMenuCreator(mSwipeMenuCreator);
        mRecyclerView.setSwipeMenuItemClickListener(mMenuItemClickListener);

        mPresenter.getLinkedBank();

        setEmptyViewHeight();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bank, menu);
        MenuItem menuItem = menu.findItem(R.id.action_add_more_bank);
        View view = menuItem.getActionView();
        view.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                onClickAddMoreBank();
            }
        });
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
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
        if (requestCode == Constants.REQUEST_CODE_SELECT_BANk) {
            mPresenter.getLinkedBank();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showLinkCardEmpty() {
        mEmptyViewImage.setVisibility(View.VISIBLE);
    }

    private void hideLinkCardEmpty() {
        mEmptyViewImage.setVisibility(View.GONE);
    }

    private void showOrHideLinkedBankEmpty() {
        if (mAdapter == null || mAdapter.getItemCount() <= 1) {
            showLinkCardEmpty();
        } else {
            hideLinkCardEmpty();
        }
    }

    private OnSwipeMenuItemClickListener mMenuItemClickListener = this::showConfirmRemoveSaveCard;

    private void showConfirmRemoveSaveCard(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
        if (mAdapter == null) {
            return;
        }
        String message = getString(R.string.txt_confirm_remove_card);
        DBaseMap bankInfo = mAdapter.getItem(adapterPosition);
        if (bankInfo instanceof DBankAccount) {
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
                            //removeLinkedBank(mAdapter.getItem(adapterPosition));
                            mPresenter.removeLinkedBank(mAdapter.getItem(adapterPosition));
                            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_DELETECARD);
                        }
                    }

                    @Override
                    public void onCancelEvent() {
                        closeable.smoothCloseMenu();
                    }
                });
    }

    protected SwipeMenuCreator mSwipeMenuCreator = (swipeLeftMenu, swipeRightMenu, viewType) -> {
        int width = getResources().getDimensionPixelSize(R.dimen.link_card_remove_width);
        int height = ViewGroup.LayoutParams.MATCH_PARENT;

        IconFontDrawable iconFontDrawable = new IconFontDrawable(getContext());
        iconFontDrawable.setIcon(R.string.general_delete_card);
        iconFontDrawable.setColor(Color.WHITE);
        iconFontDrawable.setResourcesSize(R.dimen.font_size_delete);

        SwipeMenuItem deleteItem = new SwipeMenuItem(getContext())
                .setBackgroundDrawable(R.color.red)
                .setText(getString(R.string.delete))
                .setImage(iconFontDrawable)
                .setTextColor(Color.WHITE)
                .setWidth(width)
                .setHeight(height);
        swipeRightMenu.addMenuItem(deleteItem);
    };

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

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void setListLinkedBank(List<DBaseMap> linkedBankList) {
        mAdapter.setData(linkedBankList);
        showOrHideLinkedBankEmpty();
    }

    @Override
    public void refreshLinkedBankList() {
        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            mAdapter.notifyDataSetChanged();
        }
        showOrHideLinkedBankEmpty();
    }

    @Override
    public void removeLinkedBank(DBaseMap linkedBank) {
        if (mAdapter != null) {
            mAdapter.remove(linkedBank);
        }
        showOrHideLinkedBankEmpty();
    }

    @Override
    public void showConfirmPayAfterLinkBank(DBaseMap mapBank) {
        String message = getString(R.string.confirm_continue_pay_after_link_card);
        if (mapBank instanceof DBankAccount) {
            message = getString(R.string.confirm_continue_pay_after_link_account);
        }
        DialogHelper.showNoticeDialog(getActivity(),
                message,
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
    public void onClickAddMoreBank() {
        if (mPresenter != null) {
            mPresenter.AddMoreBank();
            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_TAPADDCARD);
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
        super.showErrorDialog(message);
    }
}
