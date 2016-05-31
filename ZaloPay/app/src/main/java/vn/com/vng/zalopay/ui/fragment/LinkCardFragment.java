package vn.com.vng.zalopay.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.LinkCardActivity;
import vn.com.vng.zalopay.ui.adapter.LinkCardAdapter;
import vn.com.vng.zalopay.ui.presenter.LinkCardPresenter;
import vn.com.vng.zalopay.ui.view.ILinkCardView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LinkCardFragment extends BaseFragment implements ILinkCardView, LinkCardAdapter.OnClickBankCardListener, View.OnClickListener {

    private BottomSheetDialog mBottomSheetDialog;
    private BottomSheetBehavior mDialogBehavior;
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

    @BindView(R.id.progressContainer)
    View mLoadingView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        recyclerView.addItemDecoration(new SpacesItemDecoration(AndroidUtils.dp(12), AndroidUtils.dp(8)));
        recyclerView.setAdapter(mAdapter);

        initBottomSheet();
        showOrHidekLinkCardEmpty();
    }

    private void initBottomSheet() {
        View view = View.inflate(getContext(), R.layout.bottom_sheet_link_card_layout, null);
        View layoutMoneySource = view.findViewById(R.id.layoutMoneySource);
        View layoutDetail = view.findViewById(R.id.layoutDetail);
        View layoutRemoveLink = view.findViewById(R.id.layoutRemoveLink);

        layoutMoneySource.setOnClickListener(this);
        layoutDetail.setOnClickListener(this);
        layoutRemoveLink.setOnClickListener(this);

        mBottomSheetDialog = new BottomSheetDialog(getContext());
        mBottomSheetDialog.setContentView(view);
        mDialogBehavior = BottomSheetBehavior.from((View) view.getParent());

//        mBottomSheetDialog.show();
//        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//            }
//        });
    }

    private void showOrHidekLinkCardEmpty() {
        if (mAdapter == null || mAdapter.getItemCount() <= 1) {
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
    public void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mLoadingView.setVisibility(View.GONE);
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

    @Override
    public void onClickAddBankCard() {
        navigator.startLinkCardProducedureActivity(this);
    }

    @Override
    public void onClickMenu(BankCard bankCard) {
        mCurrentBankCard = bankCard;

        showBottomSheetDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LinkCardActivity.REQUEST_CODE) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                String carname = bundle.getString(Constants.CARDNAME);
                String first6CardNo = bundle.getString(Constants.FIRST6CARDNO);
                String last4CardNo = bundle.getString(Constants.LAST4CARDNO);
                String bankcode = bundle.getString(Constants.BANKCODE);
                long expiretime = bundle.getLong(Constants.EXPIRETIME);
                BankCard bankCard = new BankCard(carname, first6CardNo, last4CardNo, bankcode, expiretime);
                try {
                    Timber.tag("LinkCardFragment").d("onActivityResult first6CardNo: %s", first6CardNo);
                    bankCard.type = CShareData.getInstance(getActivity()).detectCardType(first6CardNo).toString();
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
                updateData(bankCard);
                return;
            }
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
            mBottomSheetDialog.dismiss();
        }
    }

    private static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int spaceHorizontal;
        private int spaceVertical;

        public SpacesItemDecoration(int spaceHorizontal, int spaceVertical) {
            this.spaceHorizontal = spaceHorizontal;
            this.spaceVertical = spaceVertical;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            outRect.left = spaceHorizontal;
            outRect.right = spaceHorizontal;
            outRect.bottom = spaceVertical;
            outRect.top = spaceVertical;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = 2 * spaceVertical;
            }
        }
    }

    private void showBottomSheetDialog() {
        mBottomSheetDialog.show();
    }

}
