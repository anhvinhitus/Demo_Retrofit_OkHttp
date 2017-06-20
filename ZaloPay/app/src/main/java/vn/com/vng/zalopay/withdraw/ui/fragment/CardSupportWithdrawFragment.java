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
import vn.com.vng.zalopay.ui.widget.GridSpacingItemDecoration;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.withdraw.models.BankSupportWithdraw;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link CardSupportWithdrawFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardSupportWithdrawFragment extends BaseFragment {
    private final static int COLUMN_COUNT = 3;

    @BindView(R.id.bankRecyclerView)
    RecyclerView mRecyclerView;

    private CardSupportWithdrawAdapter mAdapter;

    @Inject
    User mUser;

    public CardSupportWithdrawFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CardSupportWithdrawFragment.
     */
    public static CardSupportWithdrawFragment newInstance() {
        return new CardSupportWithdrawFragment();
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
        mAdapter = new CardSupportWithdrawAdapter(getContext());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(COLUMN_COUNT, 15, true));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setFocusable(false);
    }

    public void refreshCardSupportList(List<BankConfig> cardSupportList) {
        Timber.d("refreshListCardSupport : cardSupportList [%s]", cardSupportList);
        if (mAdapter == null) {
            return;
        }

        List<MapCard> mappedCardList = CShareDataWrapper.getMappedCardList(mUser);
        mAdapter.setData(mergeData(cardSupportList, mappedCardList));
        mAdapter.notifyDataSetChanged();
    }

    private List<BankSupportWithdraw> mergeData(List<BankConfig> cardSupportList,
                                                List<MapCard> mappedCardList) {
        List<BankSupportWithdraw> list = new ArrayList<>();
        if (cardSupportList == null || cardSupportList.size() <= 0) {
            return list;
        }
        for (int i = 0; i < cardSupportList.size(); i++) {
            BankConfig bankConfig = cardSupportList.get(i);
            if (bankConfig == null) {
                continue;
            }
            list.add(new BankSupportWithdraw(bankConfig, existInMappedCard(mappedCardList, bankConfig.code)));
        }
        return list;
    }

    private boolean existInMappedCard(List<MapCard> mappedCardList, String bankCode) {
        if (mappedCardList == null || mappedCardList.size() <= 0) {
            return false;
        }
        if (TextUtils.isEmpty(bankCode)) {
            return false;
        }
        for (int j = 0; j < mappedCardList.size(); j++) {
            MapCard mappedCard = mappedCardList.get(j);
            if (mappedCard == null) {
                continue;
            }
            if (bankCode.equals(mappedCard.bankcode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        CShareDataWrapper.dispose();
        super.onDestroy();
    }
}
