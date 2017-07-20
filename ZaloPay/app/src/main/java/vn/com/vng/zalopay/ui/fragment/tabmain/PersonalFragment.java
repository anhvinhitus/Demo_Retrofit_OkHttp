package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.PersonalPresenter;
import vn.com.vng.zalopay.ui.view.IPersonalView;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.widget.FragmentLifecycle;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by datnt10 on 3/27/17.
 * Tab personal UI
 */

public class PersonalFragment extends UserBaseTabFragment implements IPersonalView, FragmentLifecycle {
    @BindView(R.id.profile_iv_avatar)
    SimpleDraweeView ivAvatar;

    @BindView(R.id.profile_tv_name)
    TextView tvName;

    @BindView(R.id.profile_tv_zalopay_name)
    TextView tvZaloPayName;

    @BindView(R.id.tab_personal_tv_balance_value)
    TextView tvBalance;

    @BindView(R.id.tab_personal_tv_bank_link_now)
    TextView tvBankLink;

    @Inject
    PersonalPresenter presenter;

    public static PersonalFragment newInstance() {

        Bundle args = new Bundle();

        PersonalFragment fragment = new PersonalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_tab_personal;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onDestroyView() {
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void setUserInfo(User user) {
        if (user == null) return;
        setAvatar(user.avatar);
        setDisplayName(user.displayName);
        setZaloPayName(user.zalopayname);
    }

    @Override
    public void setAvatar(String avatar) {
        if (TextUtils.isEmpty(avatar)) {
            return;
        }

        if (ivAvatar != null) {
            ivAvatar.setImageURI(avatar);
        }
    }

    @Override
    public void setDisplayName(String displayName) {
        if (tvName != null) {
            tvName.setText(displayName);
        }
    }

    @Override
    public void setZaloPayName(String zaloPayName) {
        if (tvZaloPayName != null) {
            if (TextUtils.isEmpty(zaloPayName)) {
                tvZaloPayName.setText(getString(R.string.zalopay_name_not_update));
            } else {
                tvZaloPayName.setText(String.format(getString(R.string.leftmenu_zalopayid), zaloPayName));
            }
        }
    }

    @Override
    public void setBalance(long balance) {
        if (tvBalance != null) {
            tvBalance.setText(CurrencyUtil.spanFormatCurrency(balance, false));
        }
    }

    @Override
    public void setBankLinkText(int accounts) {
        if (tvBankLink != null) {
            if (accounts > 0) {
                tvBankLink.setText(accounts + " liên kết");
            } else {
                tvBankLink.setText(getString(R.string.personal_link_now_text));
            }
        }
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @OnClick(R.id.personal_rl_setting)
    public void goToProtectAccount() {
        navigator.startProtectAccount(getActivity());
    }

    @OnClick(R.id.personal_profile_header_info)
    public void onProfileInfoClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_PROFILE);
        navigator.startProfileInfoActivity(getContext());
    }

    @OnClick(R.id.tab_personal_rl_balance)
    public void onBalanceClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BALANCE);
        navigator.startBalanceManagementActivity(getContext());
    }

    @OnClick(R.id.tab_personal_rl_bank)
    public void onLinkCardClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BANK);
        navigator.startLinkCardActivity(getContext());
    }

    @OnClick(R.id.tab_personal_tv_bank_link_now)
    public void onBankLinkNowClick() {
        if (presenter.getAccounts() > 0) {
            ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BANK);
            navigator.startLinkCardActivity(getContext());
        } else {
            ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BANK_QUICKACTION);
            presenter.addLinkCard();
        }
    }

//    @OnClick(R.id.tab_personal_rl_bill)
//    public void onBillClick() {
//        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BILLING);
//    }
//
//    @OnClick(R.id.tab_personal_tv_bill_detail)
//    public void onBillDetailClick() {
//        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BILLING_QUICKACTION);
//        showToast("Bill detail clicked");
//    }
//
//    @OnClick(R.id.tab_personal_rl_transaction_history)
//    public void onTransHistoryClick() {
//        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_TRANSACTIONS);
//        navigator.startTransactionHistoryList(getContext());
//    }

    @OnClick(R.id.tab_personal_tv_support_center)
    public void onSupportCenterClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_SUPPORTCENTER);
        navigator.startMiniAppActivity(getActivity(), ModuleName.SUPPORT_CENTER);
    }

//    @OnClick(R.id.tab_personal_tv_quick_feedback)
//    public void onQuickFeedbackClick() {
//        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_FEEDBACK);
//    }

    @OnClick(R.id.tab_personal_tv_app_info)
    public void onAppInfoClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_ABOUTAPP);
        navigator.startMiniAppActivity(getActivity(), ModuleName.ABOUT);
    }

    @OnClick(R.id.tab_personal_rl_logout)
    public void onLogOutClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_LOGOUT);
        showConfirmSignOut();
    }

    private void showConfirmSignOut() {
        new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog)
                .setContentText(getString(R.string.txt_confirm_sigout))
                .setCancelText(getString(R.string.cancel))
                .setTitleText(getString(R.string.confirm))
                .setConfirmText(getString(R.string.txt_leftmenu_sigout))
                .setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> {
                    sweetAlertDialog.dismiss();
                    presenter.logout();
                })
                .show();
    }

    @Override
    public void onStartFragment() {

    }

    @Override
    public void onStopFragment() {

    }
}