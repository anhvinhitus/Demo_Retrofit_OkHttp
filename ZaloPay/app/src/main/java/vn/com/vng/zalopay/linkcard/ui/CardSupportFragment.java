package vn.com.vng.zalopay.linkcard.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.GridSpacingItemDecoration;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.listener.IGetCardSupportListListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link CardSupportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardSupportFragment extends BaseFragment {
    private final static int COLUMN_COUNT = 3;

    @BindView(R.id.bankRecyclerView)
    RecyclerView mRecyclerView;

    private CardSupportAdapter mAdapter;

    public CardSupportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CardSupportFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CardSupportFragment newInstance() {
        return new CardSupportFragment();
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
        showProgressDialog();
        mAdapter = new CardSupportAdapter(getContext());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(COLUMN_COUNT, 2, false));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setFocusable(false);

        getCardSupport();
    }

    private void refreshCardSupportList(List<ZPCard> cardSupportList) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.setData(cardSupportList);
        hideProgressDialog();
    }

    private void getCardSupport() {
        CShareData.getInstance().getCardSupportList(new IGetCardSupportListListener() {
            @Override
            public void onProcess() {
                Timber.d("getCardSupportList onProcess");
            }

            @Override
            public void onComplete(ArrayList<ZPCard> cardSupportList) {
                Timber.d("getCardSupportList onComplete cardSupportList[%s]", cardSupportList);
                refreshCardSupportList(cardSupportList);
            }

            @Override
            public void onError(String pErrorMess) {
                Timber.d("cardSupportHashMap onError [%s]", pErrorMess);
                hideProgressDialog();
                showRetryDialog(getString(R.string.exception_generic), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {

                    }

                    @Override
                    public void onOKevent() {
                        showProgressDialog();
                        getCardSupport();
                    }
                });
            }

            @Override
            public void onUpVersion(boolean forceUpdate, String latestVersion, String message) {
                Timber.d("cardSupportHashMap forceUpdate [%s] latestVersion [%s] message [%s]",
                        forceUpdate, latestVersion, message);
                AppVersionUtils.setVersionInfoInServer(forceUpdate, latestVersion, message);
                AppVersionUtils.showDialogUpgradeAppIfNeed(getActivity());
            }
        });
    }

    @Override
    public void onDestroy() {
        CShareData.dispose();
        super.onDestroy();
    }
}
