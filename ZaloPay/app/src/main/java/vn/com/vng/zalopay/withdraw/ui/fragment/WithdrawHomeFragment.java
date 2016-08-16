package vn.com.vng.zalopay.withdraw.ui.fragment;

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
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawHomePresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawHomeView;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link WithdrawHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WithdrawHomeFragment extends BaseFragment implements IWithdrawHomeView {

    @Inject
    WithdrawHomePresenter mPresenter;

    @BindView(R.id.tv_balance)
    TextView tvBalance;

    @BindView(R.id.tvAccountName)
    TextView tvAccountName;

    @OnClick(R.id.layoutDeposit)
    public void onClickDeposit() {
        navigator.startDepositActivity(getContext());
    }

    @OnClick(R.id.layoutWithdraw)
    public void onClickWithdraw() {
        navigator.startWithdrawActivity(getContext());
    }

    @OnClick(R.id.tvQuestion)
    public void onClickQuestion() {
        showToast("Chức năng sẽ sớm được ra mắt.");
    }

    public WithdrawHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WithdrawHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WithdrawHomeFragment newInstance() {
        return new WithdrawHomeFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_withdraw_home;
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_withdraw, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            navigator.startMiniAppActivity(getActivity(), ModuleName.TRANSACTION_LOGS);
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
            tvAccountName.setText(getString(R.string.zalopay_name_not_update));
        } else {
            tvAccountName.setText(zaloPayName);
            tvAccountName.setCompoundDrawables(null, null, null, null);
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
