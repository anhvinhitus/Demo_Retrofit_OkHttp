package vn.com.vng.zalopay.linkcard.ui;

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
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;

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

    private boolean mAutoLoadData;
    private CardSupportAdapter mAdapter;
    private IGetCardSupportListListener mGetCardSupportListListener;

    @Inject
    User mUser;

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
    public static CardSupportFragment newInstance(boolean autoLoadData) {
        Bundle args = new Bundle();
        args.putBoolean(Constants.ARG_AUTO_LOAD_DATA, autoLoadData);
        CardSupportFragment fragment = new CardSupportFragment();
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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new CardSupportAdapter(getContext());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mRecyclerView.setNestedScrollingEnabled(false);
        //mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(COLUMN_COUNT, 2, false));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setFocusable(false);

        mGetCardSupportListListener = new IGetCardSupportListListener() {
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
        };

        if (mAutoLoadData) {
            getCardSupport();
        }
    }

    private void refreshCardSupportList(List<ZPCard> cardSupportList) {
        hideProgressDialog();
        if (mAdapter == null) {
            return;
        }
        mAdapter.setData(cardSupportList);
    }

    public int getCountCardSupport() {
        if (mAdapter == null) {
            return 0;
        }
        return mAdapter.getItemCount();
    }

    public void getCardSupport() {
        Timber.d("Get card support");
        showProgressDialog();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mUser.zaloPayId;
        userInfo.accessToken = mUser.accesstoken;
        CShareData.getInstance().setUserInfo(userInfo).getCardSupportList(mGetCardSupportListListener);
    }

    @Override
    public void onDestroy() {
        hideProgressDialog();
        mGetCardSupportListListener = null;
        mAdapter = null;
        //release cache
        CShareData.dispose();
        GlobalData.initApplication(null);
        super.onDestroy();
    }
}
