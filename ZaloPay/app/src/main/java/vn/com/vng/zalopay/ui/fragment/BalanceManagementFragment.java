package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zalopay.apploader.internal.ModuleName;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.ui.presenter.BalanceManagementPresenter;
import vn.com.vng.zalopay.ui.view.IBalanceManagementView;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BalanceManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceManagementFragment extends BaseFragment implements IBalanceManagementView {

    @Inject
    BalanceManagementPresenter mPresenter;

    @BindView(R.id.tv_balance)
    TextView tvBalance;

    @BindView(R.id.layoutAccountName)
    View layoutAccountName;

    @BindView(R.id.tvAccountName)
    TextView tvAccountName;

    @BindView(R.id.layoutDeposit)
    View layoutDeposit;

    @BindView(R.id.viewSeparate)
    View viewSeparate;

    @OnClick(R.id.layoutDeposit)
    public void onClickDeposit() {
        navigator.startDepositActivity(getContext());
    }

    @OnClick(R.id.layoutWithdraw)
    public void onClickWithdraw() {
        mPresenter.startWithdrawActivity();
    }

    @OnClick(R.id.tvQuestion)
    public void onClickQuestion() {
        navigator.startMiniAppActivity(getActivity(), ModuleName.FAQ);
    }

    private void onClickAccountName() {
        if (!TextUtils.isEmpty(getUserComponent().currentUser().zalopayname)) {
            return;
        }
        if (getUserComponent().currentUser().profilelevel < 2) {
            navigator.startUpdateProfileLevel2Activity(getActivity());
        }else{
            navigator.startEditAccountActivity(getContext());
        }
    }

    private View.OnClickListener mOnClickAccountName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickAccountName();
        }
    };

    public BalanceManagementFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BalanceManagementFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BalanceManagementFragment newInstance() {
        return new BalanceManagementFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_balance_management;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.setView(this);
        checkShowHideDeposit();
    }

    private void checkShowHideDeposit() {
        try {
            boolean isEnableDeposit = CShareData.getInstance(AndroidApplication.instance().getApplicationContext())
                    .isEnableDeposite();
            if (isEnableDeposit) {
                layoutDeposit.setVisibility(View.VISIBLE);
                viewSeparate.setVisibility(View.VISIBLE);
            } else {
                layoutDeposit.setVisibility(View.GONE);
                viewSeparate.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Timber.w(e, "check show/hide deposit exception: [%s]", e.getMessage());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_withdraw, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            navigator.startTransactionHistoryList(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        CShareData.dispose();
        super.onDestroy();
    }

    @Override
    public void updateBalance(long balance) {
        Timber.d("updateBalance balance [%s]", balance);
        if (tvBalance == null) {
            return;
        }
        tvBalance.setText(CurrencyUtil.formatCurrency(balance, false));
    }

    @Override
    public void updateUserInfo(User user) {
        if (tvAccountName == null) {
            return;
        }
        String zaloPayName = user.zalopayname;

        if (TextUtils.isEmpty(zaloPayName)) {
            tvAccountName.setHint(getString(R.string.not_update));
            layoutAccountName.setOnClickListener(mOnClickAccountName);
        } else {
            tvAccountName.setText(zaloPayName);
            tvAccountName.setCompoundDrawables(null, null, null, null);
            layoutAccountName.setOnClickListener(null);
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
    public void showRetry() {
    }

    @Override
    public void hideRetry() {
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }
}
