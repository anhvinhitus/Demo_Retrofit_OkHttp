package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BankSupportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BankSupportFragment extends BaseFragment implements IBankSupportView {
    private final static int COLUMN_COUNT = 3;

    @BindView(R.id.tvLinkCard)
    View mTvLinkCard;

    @BindView(R.id.linkCardRecyclerView)
    RecyclerView mLinkCardRecyclerView;

    @BindView(R.id.tvLinkAccount)
    View mTvLinkAccount;

    @BindView(R.id.linkAccRecyclerView)
    RecyclerView mLinkAccRecyclerView;

    @Inject
    BankSupportPresenter mPresenter;

    private boolean mAutoLoadData;
    private BankSupportAdapter mLinkCardAdapter;
    private BankSupportAdapter mLinkAccAdapter;

    public BankSupportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CardSupportFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BankSupportFragment newInstance(boolean autoLoadData) {
        Bundle args = new Bundle();
        args.putBoolean(Constants.ARG_AUTO_LOAD_DATA, autoLoadData);
        BankSupportFragment fragment = new BankSupportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_card_support;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mAutoLoadData = bundle.getBoolean(Constants.ARG_AUTO_LOAD_DATA, false);
        Timber.d("initData mAutoLoadData[%s]", mAutoLoadData);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mLinkCardAdapter = new BankSupportAdapter(getContext());
        mLinkAccAdapter = new BankSupportAdapter(getContext());

        mLinkCardRecyclerView.setHasFixedSize(true);
        mLinkCardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mLinkCardRecyclerView.setNestedScrollingEnabled(false);
        //mLinkCardRecyclerView.addItemDecoration(new GridSpacingItemDecoration(COLUMN_COUNT, 2, false));
        mLinkCardRecyclerView.setAdapter(mLinkCardAdapter);
        mLinkCardRecyclerView.setFocusable(false);

        mLinkAccRecyclerView.setHasFixedSize(true);
        mLinkAccRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mLinkAccRecyclerView.setNestedScrollingEnabled(false);
        //mLinkAccRecyclerView.addItemDecoration(new GridSpacingItemDecoration(COLUMN_COUNT, 2, false));
        mLinkAccRecyclerView.setAdapter(mLinkAccAdapter);
        mLinkAccRecyclerView.setFocusable(false);

        Timber.d("onViewCreated mAutoLoadData[%s]", mAutoLoadData);
        if (mAutoLoadData) {
            getCardSupport();
        }
    }

    public void getCardSupport() {
        if (mPresenter != null) {
            mPresenter.getCardSupport();
        }
    }

    public int getCountLinkCardSupport() {
        if (mLinkCardAdapter == null) {
            return 0;
        }
        return mLinkCardAdapter.getItemCount();
    }

    public int getCountLinkAccountSupport() {
        if (mLinkAccAdapter == null) {
            return 0;
        }
        return mLinkAccAdapter.getItemCount();
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
        hideProgressDialog();
        mLinkCardAdapter = null;
        mLinkAccAdapter = null;
        //release cache
        CShareDataWrapper.dispose();
        GlobalData.initApplication(null);
        mPresenter.destroy();
        super.onDestroy();
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
    public void refreshBankSupports(List<ZPCard> cardSupportList) {
        if (!isAdded()) {
            Timber.d("Refresh Bank Supports error because fragment didn't add.");
            return;
        }
        hideProgressDialog();
        if (Lists.isEmptyOrNull(cardSupportList)) {
            Timber.d("Refresh Bank Supports error because list card null/empty.");
            return;
        }
        List<ZPCard> listSupportLinkCard = new ArrayList<>();
        List<ZPCard> listSupportLinkAccount = new ArrayList<>();
        for (ZPCard card: cardSupportList) {
            if (card == null) {
                continue;
            }
            if (card.isBankAccount()) {
                listSupportLinkAccount.add(card);
            } else {
                listSupportLinkCard.add(card);
            }
        }
        refreshLinkCardList(listSupportLinkCard);
        refreshLinkAccountList(listSupportLinkAccount);
    }

    private void refreshLinkCardList(List<ZPCard> cardSupportList) {
        if (mLinkCardAdapter == null) {
            return;
        }
        if (!Lists.isEmptyOrNull(cardSupportList)) {
            mTvLinkCard.setVisibility(View.VISIBLE);
            mLinkCardRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mTvLinkCard.setVisibility(View.GONE);
            mLinkCardRecyclerView.setVisibility(View.GONE);
        }
        mLinkCardAdapter.setData(cardSupportList);
    }

    private void refreshLinkAccountList(List<ZPCard> cardSupportList) {
        if (mLinkAccAdapter == null) {
            return;
        }
        if (!Lists.isEmptyOrNull(cardSupportList)) {
            mTvLinkAccount.setVisibility(View.VISIBLE);
            mLinkAccRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mTvLinkAccount.setVisibility(View.GONE);
            mLinkAccRecyclerView.setVisibility(View.GONE);
        }
        mLinkAccAdapter.setData(cardSupportList);
    }

    @Override
    public void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener) {
        if (!isAdded()) {
            return;
        }
        super.showRetryDialog(message, listener);
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
