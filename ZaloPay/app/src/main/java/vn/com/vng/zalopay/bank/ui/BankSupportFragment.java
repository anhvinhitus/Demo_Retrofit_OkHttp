package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.LinkBankType;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
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

    @BindView(R.id.tv_title_list_bank)
    TextView mTvTitleListBank;

    @BindView(R.id.list_bank)
    RecyclerView mBankRecyclerView;

    @Inject
    BankSupportPresenter mPresenter;

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
    public static BankSupportFragment newInstance(boolean autoLoadData, LinkBankType bankType) {
        Bundle args = new Bundle();
        args.putBoolean(Constants.ARG_AUTO_LOAD_DATA, autoLoadData);
        args.putSerializable(Constants.ARG_LINK_BANK_TYPE, bankType);
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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mPresenter.iniData(getArguments());
        mLinkCardAdapter = new BankSupportAdapter(getContext());
        mLinkAccAdapter = new BankSupportAdapter(getContext());

        mBankRecyclerView.setHasFixedSize(true);
        mBankRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mBankRecyclerView.setNestedScrollingEnabled(false);
        //mBankRecyclerView.addItemDecoration(new GridSpacingItemDecoration(COLUMN_COUNT, 2, false));
        mBankRecyclerView.setAdapter(mLinkCardAdapter);
        mBankRecyclerView.setFocusable(false);

        mPresenter.getCardSupportIfNeed();
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
    public void refreshListBank(List<ZPCard> cardSupportList) {
        if (!isAdded()) {
            Timber.d("Refresh Bank Supports error because fragment didn't add.");
            return;
        }
        hideProgressDialog();
        if (mLinkCardAdapter == null) {
            Timber.d("Refresh Bank Supports error because adapter is null.");
            return;
        }
        if (Lists.isEmptyOrNull(cardSupportList)) {
            mLinkCardAdapter.setData(Collections.emptyList());
        } else {
            mLinkCardAdapter.setData(cardSupportList);
        }
    }

    @Override
    public void showRetryDialog(String message, ZPWOnEventConfirmDialogListener listener) {
        if (!isAdded()) {
            return;
        }
        super.showRetryDialog(message, listener);
    }

    @Override
    public void setTitleListBank(int strResource) {
        mTvTitleListBank.setText(strResource);
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
