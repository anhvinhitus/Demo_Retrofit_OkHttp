package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.PersonalPresenter;
import vn.com.vng.zalopay.ui.view.IPersonalView;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by Duke on 3/27/17.
 */

public class PersonalFragment extends BaseFragment implements IPersonalView {
    @BindView(R.id.tab_personal_iv_avatar)
    SimpleDraweeView ivAvatar;

    @BindView(R.id.tab_personal_tv_name)
    TextView tvName;

    @BindView(R.id.tab_personal_tv_zalopayid)
    TextView tvZaloPayId;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        ivAvatar.setImageURI(avatar);
    }

    @Override
    public void setDisplayName(String displayName) {
        tvName.setText(displayName);
    }

    @Override
    public void setZaloPayName(String zaloPayName) {
        if (TextUtils.isEmpty(zaloPayName)) {
            tvZaloPayId.setText(getString(R.string.zalopay_name_not_update));
        } else {
            tvZaloPayId.setText(String.format(getString(R.string.leftmenu_zalopayid), zaloPayName));
        }
    }

    @Override
    public void setBalance(long balance) {
        if (tvBalance != null) {
            String _temp = CurrencyUtil.formatCurrency(balance, true);

            SpannableString span = new SpannableString(_temp);
            span.setSpan(new RelativeSizeSpan(0.66f), _temp.indexOf(CurrencyUtil.CURRENCY_UNIT), _temp.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvBalance.setText(span);
        }
    }

    @Override
    public void setBankLinkText(int linkBankStatus, int cardAmount, int accAmount) {
        if (tvBankLink != null) {
            switch (linkBankStatus) {
                case Constants.LINK_BANK_NONE:
                    tvBankLink.setText(getString(R.string.personal_link_now_text));
                    break;
                case Constants.LINK_BANK_CARD_LINKED:
                    tvBankLink.setText(cardAmount + " thẻ");
                    break;
                case Constants.LINK_BANK_ACCOUNT_LINKED:
                    tvBankLink.setText(accAmount + " tài khoản");
                    break;
                case Constants.LINK_BANK_CARD_ACCOUNT_LINKED:
                    tvBankLink.setText("Đã liên kết thẻ và tài khoản");
                    break;
            }
        }
    }

    @OnClick(R.id.tab_personal_rl_info)
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
        showToast("Quick link bank clicked");
        switch (presenter.getLinkBankStatus()) {
            case Constants.LINK_BANK_NONE:
                ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BANK_QUICKACTION);
                presenter.addLinkCard(getActivity());
                break;
            case Constants.LINK_BANK_CARD_LINKED:
            case Constants.LINK_BANK_ACCOUNT_LINKED:
            case Constants.LINK_BANK_CARD_ACCOUNT_LINKED:
                ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BANK);
                navigator.startLinkCardActivity(getContext());
                break;
        }
    }

    @OnClick(R.id.tab_personal_rl_bill)
    public void onBillClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BILLING);
    }

    @OnClick(R.id.tab_personal_tv_bill_detail)
    public void onBillDetailClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_BILLING_QUICKACTION);
        showToast("Bill detail clicked");
    }

    @OnClick(R.id.tab_personal_rl_transaction_history)
    public void onTransHistoryClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_TRANSACTIONS);
        navigator.startTransactionHistoryList(getContext());
    }

    @OnClick(R.id.tab_personal_tv_support_center)
    public void onSupportCenterClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_SUPPORTCENTER);
        navigator.startMiniAppActivity(getActivity(), ModuleName.SUPPORT_CENTER);
    }

    @OnClick(R.id.tab_personal_tv_quick_feedback)
    public void onQuickFeedbackClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_FEEDBACK);
    }

    @OnClick(R.id.tab_personal_tv_app_info)
    public void onAppInfoClick() {
        ZPAnalytics.trackEvent(ZPEvents.TOUCH_ME_ABOUTAPP);
        navigator.startMiniAppActivity(getActivity(), ModuleName.ABOUT);
    }

    @OnClick(R.id.tab_personal_tv_logout)
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
//                    ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENULOGOUT);
                })
                .show();
    }
}