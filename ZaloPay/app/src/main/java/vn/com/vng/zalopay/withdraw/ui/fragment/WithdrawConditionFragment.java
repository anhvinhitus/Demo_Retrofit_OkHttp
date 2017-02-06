package vn.com.vng.zalopay.withdraw.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.LinkBankPagerIndex;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.withdraw.models.BankType;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawConditionPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawConditionView;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link WithdrawConditionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WithdrawConditionFragment extends BaseFragment implements IWithdrawConditionView {

    @Inject
    WithdrawConditionPresenter mPresenter;

    @BindView(R.id.tvCardNote)
    View tvCardNote;

    @BindView(R.id.tvUserNote)
    View tvUserNote;

    @BindView(R.id.chkPhone)
    CheckBox chkPhone;

    @BindView(R.id.chkPin)
    CheckBox chkPin;

    @BindView(R.id.tvUpdateProfile)
    View tvUpdateProfile;

    CardSupportWithdrawFragment mCardSupportFragment;
    AccountSupportWithdrawFragment mAccSupportWithdrawFragment;

    @OnClick(R.id.tvUpdateProfile)
    public void onClickUpdateProfile() {
        navigator.startUpdateProfileLevel2Activity(getActivity());
    }

    @OnClick(R.id.tvAddCard)
    public void onClickAddCard() {
        startLinkCardActivity(LinkBankPagerIndex.LINK_CARD);
    }

    @OnClick(R.id.tvAddAccount)
    public void onClickAddAccount() {
        startLinkCardActivity(LinkBankPagerIndex.LINK_ACCOUNT);
    }

    private void startLinkCardActivity(LinkBankPagerIndex pageIndex) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.ARG_PAGE_INDEX, pageIndex.getValue());
        navigator.startLinkCardActivity(getContext(), bundle, false);
    }

    public WithdrawConditionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WithdrawConditionFragment.
     */
    public static WithdrawConditionFragment newInstance() {
        return new WithdrawConditionFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_withdraw_condition;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mCardSupportFragment = (CardSupportWithdrawFragment)
                getChildFragmentManager().findFragmentById(R.id.cardSupportWithdrawFragment);
        mAccSupportWithdrawFragment = (AccountSupportWithdrawFragment)
                getChildFragmentManager().findFragmentById(R.id.accSupportWithdrawFragment);
        showLoading();
    }

    @Override
    public void setProfileValid(boolean isValid) {
        Timber.d("setProfileValid[%s]", isValid);
        if (isValid) {
            chkPhone.setChecked(true);
            chkPin.setChecked(true);
            tvUserNote.setVisibility(View.GONE);
            tvUpdateProfile.setVisibility(View.GONE);
        } else {
            chkPhone.setChecked(false);
            chkPin.setChecked(false);
            tvUserNote.setVisibility(View.VISIBLE);
            tvUpdateProfile.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void refreshListCardSupport(List<BankConfig> list) {
        Timber.d("refresh CardSupportList[%s]", list);
        hideLoading();
        List<BankConfig> listSupportLinkCard = new ArrayList<>();
        List<BankConfig> listSupportLinkAccount = new ArrayList<>();
        if (!Lists.isEmptyOrNull(list)) {
            for (BankConfig bankConfig: list) {
                if (bankConfig == null) {
                    continue;
                }
                Timber.d("bank type[%s]", bankConfig.banktype);
                if (bankConfig.banktype == BankType.SUPPORT_LINK_CARD.getValue()) {
                    listSupportLinkCard.add(bankConfig);
                } else if (bankConfig.banktype == BankType.SUPPORT_LINK_ACCOUNT.getValue()) {
                    listSupportLinkAccount.add(bankConfig);
                }
            }
        }
        if (mCardSupportFragment != null) {
            mCardSupportFragment.refreshCardSupportList(listSupportLinkCard);
        }
        if (mAccSupportWithdrawFragment != null) {
            mAccSupportWithdrawFragment.refreshAccountSupportList(listSupportLinkAccount);
        }
    }

    public void hideCardNote() {
        tvCardNote.setVisibility(View.GONE);
    }

    @Override
    public void showConfirmDialog(String message, ZPWOnEventConfirmDialogListener listener) {
        super.showConfirmDialog(message,
                getString(R.string.txt_retry),
                getString(R.string.txt_close),
                listener);
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        CShareData.dispose();
        super.onDestroy();
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
        showToast(message);
    }
}
