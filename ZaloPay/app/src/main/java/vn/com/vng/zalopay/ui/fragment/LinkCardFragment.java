package vn.com.vng.zalopay.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.adapter.LinkCardAdapter;
import vn.com.vng.zalopay.ui.presenter.LinkCardPresenter;
import vn.com.vng.zalopay.ui.view.ILinkCardView;
import vn.com.vng.zalopay.utils.BankCardUtil;
import vn.com.zalopay.wallet.entity.gatewayinfo.DMappedCard;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LinkCardFragment extends BaseFragment implements ILinkCardView,
        LinkCardAdapter.OnClickBankCardListener, View.OnClickListener {

    private Dialog mBottomSheetDialog;
    private BankCard mCurrentBankCard;

    public static LinkCardFragment newInstance() {

        Bundle args = new Bundle();

        LinkCardFragment fragment = new LinkCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_link_card;
    }

    @Inject
    Navigator navigator;

    @BindView(R.id.imgLinkCardEmpty)
    ImageView imgLinkCardEmpty;

    @BindView(R.id.tvEmpty)
    TextView tvEmpty;

    @BindView(R.id.listview)
    RecyclerView recyclerView;

    private LinkCardAdapter mAdapter;

    @Inject
    LinkCardPresenter presenter;

//    @BindView(R.id.progressContainer)
//    View mLoadingView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAdapter = new LinkCardAdapter(getContext(), this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
//        recyclerView.addItemDecoration(new SpacesItemDecoration(AndroidUtils.dp(12), AndroidUtils.dp(8)));
        recyclerView.setAdapter(mAdapter);

        initBottomSheet();
        showOrHidekLinkCardEmpty();
        presenter.checkShowIntroSaveCard();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_intro, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_intro) {
            startIntroActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initBottomSheet() {
        if (mBottomSheetDialog == null) {
            mBottomSheetDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar);
            mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mBottomSheetDialog.setContentView(R.layout.bottom_sheet_link_card_layout);
            mBottomSheetDialog.setTitle("");
            final Window window = mBottomSheetDialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            View root = mBottomSheetDialog.findViewById(R.id.root);
            View layoutMoneySource = mBottomSheetDialog.findViewById(R.id.layoutMoneySource);
            View layoutDetail = mBottomSheetDialog.findViewById(R.id.layoutDetail);
            View layoutRemoveLink = mBottomSheetDialog.findViewById(R.id.layoutRemoveLink);

            root.setOnClickListener(this);
            layoutMoneySource.setOnClickListener(this);
            layoutDetail.setOnClickListener(this);
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

    private void showOrHidekLinkCardEmpty() {
        if (mAdapter == null || mAdapter.getItemCount() <= 0) {
            showLinkCardEmpty();
        } else {
            hideLinkCardEmpty();
        }
    }

    private void showLinkCardEmpty() {
        imgLinkCardEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.VISIBLE);
        //start animation
        imgLinkCardEmpty.setBackgroundResource(R.drawable.link_card_empty_anim);
        AnimationDrawable frameAnimation = (AnimationDrawable) imgLinkCardEmpty.getBackground();
        frameAnimation.start();
    }

    private void hideLinkCardEmpty() {
        imgLinkCardEmpty.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.getListCard();
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        // break circular link between this and mAdapter
        mAdapter = null;
        super.onDestroy();
    }

    @Override
    public void startIntroActivity() {
        navigator.startIntroActivity(this);
    }

    @Override
    public void setData(List<BankCard> bankCards) {
        mAdapter.setData(bankCards);
        showOrHidekLinkCardEmpty();
    }


    @Override
    public void updateData(BankCard bankCard) {
        mAdapter.insert(bankCard);
        showOrHidekLinkCardEmpty();
    }

    @Override
    public void removeData(BankCard bankCard) {
        mAdapter.remove(bankCard);
        showOrHidekLinkCardEmpty();
    }

    @Override
    public void onAddCardSuccess(DMappedCard card) {
        if (card == null) {
            return;
        }
        BankCard bankCard = new BankCard(card.cardname, card.first6cardno,
                card.last4cardno, card.bankcode, card.expiretime);
        try {
            Timber.d("onActivityResult first6CardNo: %s", card.first6cardno);
            bankCard.type = presenter.detectCardType(card.bankcode, card.first6cardno);
            Timber.d("onActivityResult bankCard.type: %s", bankCard.type);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        updateData(bankCard);
    }

    @Override
    public void onTokenInvalid() {

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
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        hideLoading();
        showToast(message);
    }

    @OnClick(R.id.btn_add_card)
    public void onClickAddBankCard() {
        presenter.addLinkCard();
    }

    @Override
    public void onClickMenu(BankCard bankCard) {
        mCurrentBankCard = bankCard;

        showBottomSheetDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_INTRO) {
            presenter.addLinkCard();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        int itemId = v.getId();
        if (itemId == R.id.layoutMoneySource) {
            mBottomSheetDialog.dismiss();
        } else if (itemId == R.id.layoutDetail) {
            mBottomSheetDialog.dismiss();
        } else if (itemId == R.id.layoutRemoveLink) {
            presenter.removeLinkCard(mCurrentBankCard);
            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_DELETECARD);
            mBottomSheetDialog.dismiss();
        } else if (itemId == R.id.root) {
            mBottomSheetDialog.dismiss();
        }
    }

    private void showBottomSheetDialog() {
        if (mBottomSheetDialog == null) {
            initBottomSheet();
        }
        TextView tvCardNum = (TextView) mBottomSheetDialog.findViewById(R.id.tv_num_acc);
        if (mCurrentBankCard != null && tvCardNum != null) {
            tvCardNum.setText(BankCardUtil.formatBankCardNumber(mCurrentBankCard.first6cardno, mCurrentBankCard.last4cardno));
        }
        initBottomSheet();
        mBottomSheetDialog.show();
    }

}
