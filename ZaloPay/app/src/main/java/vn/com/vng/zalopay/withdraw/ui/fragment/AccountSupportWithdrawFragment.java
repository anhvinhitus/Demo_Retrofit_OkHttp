package vn.com.vng.zalopay.withdraw.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.withdraw.models.BankSupportWithdraw;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link AccountSupportWithdrawFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountSupportWithdrawFragment extends BaseFragment {
    private final static int COLUMN_COUNT = 2;

    @BindView(R.id.bankRecyclerView)
    RecyclerView mRecyclerView;

    private AccountSupportWithdrawAdapter mAdapter;

    @Inject
    User mUser;

    public AccountSupportWithdrawFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CardSupportWithdrawFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountSupportWithdrawFragment newInstance() {
        return new AccountSupportWithdrawFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_card_support_withdraw;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new AccountSupportWithdrawAdapter(getContext());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mRecyclerView.setNestedScrollingEnabled(false);
        //mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(COLUMN_COUNT, 2, false));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setFocusable(false);
    }

    public void refreshAccountSupportList(List<BankConfig> accountSupportList) {
        Timber.d("refreshListCardSupport accSupportList[%s]", accountSupportList);
        if (mAdapter == null) {
            return;
        }

        // TODO: 2/4/17 - longlv: waiting PaymentSDK to change getMapBankAccountList -> getMappedAccount.
        List<DBankAccount> mappedAccountList = CShareData.getInstance().getMapBankAccountList(mUser.zaloPayId);
        mAdapter.setData(mergeData(accountSupportList, mappedAccountList));
        mAdapter.notifyDataSetChanged();
    }

    private List<BankSupportWithdraw> mergeData(List<BankConfig> cardSupportList,
                                                List<DBankAccount> mappedAccountList) {
        List<BankSupportWithdraw> list = new ArrayList<>();
        if (cardSupportList == null || cardSupportList.size() <= 0) {
            return list;
        }
        for (int i = 0; i < cardSupportList.size(); i++) {
            BankConfig bankConfig = cardSupportList.get(i);
            if (bankConfig == null) {
                continue;
            }
            list.add(new BankSupportWithdraw(bankConfig, existInMappedCard(mappedAccountList, bankConfig.code)));
        }
        return list;
    }

    private boolean existInMappedCard(List<DBankAccount> mappedAccountList, String bankCode) {
        if (mappedAccountList == null || mappedAccountList.size() <= 0) {
            return false;
        }
        if (TextUtils.isEmpty(bankCode)) {
            return false;
        }
        for (int j = 0; j < mappedAccountList.size(); j++) {
            DBankAccount bankAccount = mappedAccountList.get(j);
            if (bankAccount == null) {
                continue;
            }
            if (bankCode.equals(bankAccount.bankcode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        CShareData.dispose();
        super.onDestroy();
    }
}
