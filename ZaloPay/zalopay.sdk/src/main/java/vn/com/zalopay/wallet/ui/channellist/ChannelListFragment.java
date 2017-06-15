package vn.com.zalopay.wallet.ui.channellist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import vn.com.zalopay.utility.PlayStoreUtils;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.listener.ZPWPaymentOpenNetworkingDialogListener;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.BaseFragment;
import vn.com.zalopay.wallet.ui.GenericFragment;
import vn.com.zalopay.wallet.view.adapter.RecyclerTouchListener;
import vn.com.zalopay.wallet.view.component.activity.MapListSelectionActivity;

/**
 * Created by chucvv on 6/12/17.
 */

public class ChannelListFragment extends GenericFragment<ChannelListPresenter> implements ChannelListContract.IView {
    private boolean delayClick = false;
    private View.OnClickListener mConfirmClick = view -> {
        if (!delayClick) {
            delayClick = true;
            new Handler().postDelayed(() -> delayClick = false, 1000);
            mPresenter.startPayment();
        }
    };
    private RecyclerView channel_list_recycler;
    private String mOriginTitle;
    private TextView amount_txt;
    private View amount_linearlayout;
    private TextView appname_txt;
    private View app_info_module;
    private View transfer_info_module;
    private TextView item_name;
    private TextView receiver_zaloname_txt;
    private View receiver_zalopay_relativelayout;
    private View receiver_zalopayname_relativelayout;
    private TextView receiver_zalopayname_txt;
    private TextView transfer_description_txt;
    private TextView transfer_amount_txt;
    private View transfer_fee_relativelayout;
    private View transfer_amounttotal_relativelayout;
    private TextView transfer_fee_txt;
    private TextView transfer_amounttotal_txt;
    private Button confirm_button;

    public static BaseFragment newInstance() {
        return new ChannelListFragment();
    }

    public static BaseFragment newInstance(Bundle args) {
        BaseFragment fragment = newInstance();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_channellist;
    }

    @Override
    protected void onViewBound(View view) {
        super.onViewBound(view);
        channel_list_recycler = (RecyclerView) view.findViewById(R.id.channel_list_recycler);
        amount_txt = (TextView) view.findViewById(R.id.amount_txt);
        amount_linearlayout = view.findViewById(R.id.amount_linearlayout);
        appname_txt = (TextView) view.findViewById(R.id.appname_txt);
        app_info_module = view.findViewById(R.id.app_info_module);
        transfer_info_module = view.findViewById(R.id.transfer_info_module);
        item_name = (TextView) view.findViewById(R.id.item_name);

        receiver_zalopay_relativelayout = view.findViewById(R.id.receiver_zaloname_relativelayout);
        receiver_zaloname_txt = (TextView) view.findViewById(R.id.receiver_zaloname_txt);
        receiver_zalopayname_relativelayout = view.findViewById(R.id.receiver_zalopayname_relativelayout);
        receiver_zalopayname_txt = (TextView) view.findViewById(R.id.receiver_zalopayname_txt);
        transfer_description_txt = (TextView) view.findViewById(R.id.transfer_description_txt);
        transfer_amount_txt = (TextView) view.findViewById(R.id.transfer_amount_txt);
        transfer_fee_relativelayout = view.findViewById(R.id.transfer_fee_relativelayout);
        transfer_amounttotal_relativelayout = view.findViewById(R.id.transfer_amounttotal_relativelayout);
        transfer_fee_txt = (TextView) view.findViewById(R.id.transfer_fee_txt);
        transfer_amounttotal_txt = (TextView) view.findViewById(R.id.transfer_amounttotal_txt);

        confirm_button = (Button) view.findViewById(R.id.confirm_button);
        confirm_button.setOnClickListener(mConfirmClick);
        setupRecyclerView();
    }

    @Override
    public void setTitle(String title) {
        if (TextUtils.isEmpty(mOriginTitle)) {
            mOriginTitle = title;
        }
        if (getActivity() != null) {
            getActivity().setTitle(title);
        }
    }

    protected void setupRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        //channel_list_recycler.setHasFixedSize(true);
        channel_list_recycler.setLayoutManager(mLayoutManager);
        //channel_list_recycler.setItemAnimator(new DefaultItemAnimator());
        channel_list_recycler.addOnItemTouchListener(new RecyclerTouchListener(getContext(), channel_list_recycler));
    }

    protected void showOrderAmount(long amount) {
        if (amount > 0) {
            String txtAmount = StringUtil.formatVnCurrence(String.valueOf(amount));
            amount_txt.setText(txtAmount);
        } else {
            amount_linearlayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected ChannelListPresenter initializePresenter() {
        return new ChannelListPresenter();
    }

    @Override
    protected void onDataBound(View view) {
    }

    @Override
    public void showLoading(String pTitle) {
        if (getActivity() != null) {
            getActivity().setTitle(pTitle);
            DialogManager.showProcessDialog(getActivity(), () -> {
            });
        }
    }

    @Override
    public void hideLoading() {
        DialogManager.closeProcessDialog();
        if (getActivity() != null) {
            getActivity().setTitle(mOriginTitle);
        }
    }

    @Override
    public void showError(String pMessage) {
        hideLoading();
        DialogManager.showSweetDialogCustom(getActivity(), pMessage, getResources().getString(R.string.dialog_close_button),
                SweetAlertDialog.WARNING_TYPE, this::callbackThenterminate);
    }

    @Override
    public void terminate() {
        Log.d(this, "recycle activity");
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void callbackThenterminate() {
        if (mPresenter != null) {
            mPresenter.terminate();
        }
        terminate();
    }

    @Override
    public void renderAppInfo(AppInfo appInfo) {
        boolean hasAppName = appInfo != null && !TextUtils.isEmpty(appInfo.appname);
        if (hasAppName) {
            appname_txt.setText(appInfo.appname);
        }
        appname_txt.setVisibility(hasAppName ? View.VISIBLE : View.GONE);
    }

    @Override
    public void renderOrderInfo(UserInfo userInfo, AbstractOrder order, @TransactionType int transtype) {
        switch (transtype) {
            case TransactionType.MONEY_TRANSFER:
                app_info_module.setVisibility(View.GONE);
                transfer_info_module.setVisibility(View.VISIBLE);
                renderTranferMoneyInfo(userInfo, order);
                renderFee(order);
                break;
            case TransactionType.WITHDRAW:
                item_name.setText(GlobalData.getStringResource(RS.string.zpw_string_withdraw_description));
                break;
            default:
                if (order != null) {
                    item_name.setText(order.description);
                }
            case TransactionType.LINK:
                break;
            case TransactionType.PAY:
                break;
            case TransactionType.TOPUP:
                break;
        }
    }

    private void renderFee(AbstractOrder order) {
        boolean hasFee = order.fee > 0;
        if (hasFee) {
            String formatPrice = StringUtil.formatVnCurrence(String.valueOf(order.fee));
            transfer_fee_txt.setText(formatPrice);
            String txtAmount = StringUtil.formatVnCurrence(String.valueOf(order.amount_total));
            transfer_amounttotal_txt.setText(txtAmount);
        }
        transfer_fee_relativelayout.setVisibility(hasFee ? View.VISIBLE : View.GONE);
        transfer_amounttotal_relativelayout.setVisibility(hasFee ? View.VISIBLE : View.GONE);
    }

    private void renderTranferMoneyInfo(UserInfo userInfo, AbstractOrder order) {
        //receiver zalo name
        boolean hasZaloName = !TextUtils.isEmpty(userInfo.zalo_name);
        if (hasZaloName) {
            receiver_zaloname_txt.setText(userInfo.zalo_name);
        }
        receiver_zalopay_relativelayout.setVisibility(hasZaloName ? View.VISIBLE : View.GONE);

        //zalopay name
        boolean hasZaloPayName = !TextUtils.isEmpty(userInfo.zalopay_name);
        if (hasZaloPayName) {
            receiver_zalopayname_txt.setText(userInfo.zalopay_name);
        }
        receiver_zalopayname_relativelayout.setVisibility(hasZaloPayName ? View.VISIBLE : View.GONE);

        //description
        boolean hasDesc = !TextUtils.isEmpty(order.description);
        if (hasDesc) {
            transfer_description_txt.setText(order.description);
        }
        transfer_description_txt.setVisibility(hasDesc ? View.VISIBLE : View.GONE);

        //amount
        if (order.amount > 0) {
            String txtAmount = StringUtil.formatVnCurrence(String.valueOf(order.amount));
            transfer_amount_txt.setText(txtAmount);
        }

    }

    @Override
    public void onBindingChannel(ChannelListAdapter pChannelAdapter) {
        channel_list_recycler.setAdapter(pChannelAdapter);
        if (!confirm_button.isEnabled()) {
            confirm_button.setEnabled(true);
        }
    }

    @Override
    public void showAppInfoNotFoundDialog() {
        DialogManager.showSweetDialogCustom(getActivity(),
                getResources().getString(R.string.zpw_not_allow_payment_app),
                getResources().getString(R.string.dialog_close_button),
                SweetAlertDialog.WARNING_TYPE, this::callbackThenterminate);
    }

    @Override
    public void showForceUpdateLevelDialog() {
        DialogManager.showSweetDialogOptionNotice(getActivity(),
                getResources().getString(R.string.zpw_string_alert_profilelevel_update),
                getResources().getString(R.string.dialog_upgrade_button),
                getResources().getString(R.string.dialog_cancel_button), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        callbackThenterminate();
                    }

                    @Override
                    public void onOKevent() {
                        mPresenter.setPaymentStatusAndCallback(PaymentStatus.LEVEL_UPGRADE_PASSWORD);
                        callbackThenterminate();
                    }
                });
    }

    @Override
    public void showUpdateLevelDialog(String message, String btnCloseText, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showSweetDialogOptionNotice(getActivity(),
                message,
                getResources().getString(R.string.dialog_upgrade_button),
                btnCloseText, pListener);
    }

    @Override
    public void showWarningLinkCardBeforeWithdraw() {
        hideLoading();
        DialogManager.showSweetDialogOptionNotice(getActivity(),
                getResources().getString(R.string.zpw_string_alert_linkcard_channel_withdraw),
                getResources().getString(R.string.dialog_linkcard_button),
                getResources().getString(R.string.dialog_close_button),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        callbackThenterminate();
                    }

                    @Override
                    public void onOKevent() {
                        mPresenter.setPaymentStatusAndCallback(PaymentStatus.DIRECT_LINKCARD);
                        callbackThenterminate();
                    }
                });
    }

    @Override
    public void showOpenSettingNetwokingDialog(ZPWPaymentOpenNetworkingDialogListener pListener) {
        hideLoading();
        DialogManager.showSweetDialog(getActivity(), SweetAlertDialog.NO_INTERNET,
                getString(R.string.zingpaysdk_alert_title_nointernet),
                getString(R.string.zingpaysdk_alert_content_nointernet), pIndex -> {
                    if (pIndex == 0 && pListener != null) {
                        pListener.onCloseNetworkingDialog();
                    } else if (pIndex == 1) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                        if (pListener != null) {
                            pListener.onOpenSettingDialogClicked();
                        }
                    }
                },
                getResources().getString(R.string.dialog_turn_off),
                getResources().getString(R.string.dialog_turn_on));
    }

    @Override
    public void showInfoDialog(String pMessage) {
        hideLoading();
        DialogManager.showSweetDialogCustom(getActivity(),
                pMessage, getResources().getString(R.string.dialog_close_button),
                SweetAlertDialog.INFO_TYPE, null);
    }

    @Override
    public void showSupportBankVersionDialog(String pMessage) {
        DialogManager.showSweetDialogConfirm(getActivity(),
                pMessage, getResources().getString(R.string.dialog_upgrade_button),
                getResources().getString(R.string.dialog_choose_again_button), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {

                    }

                    @Override
                    public void onOKevent() {
                        PlayStoreUtils.openPlayStoreForUpdate(GlobalData.getMerchantActivity(), BuildConfig.PACKAGE_IN_PLAY_STORE, "Zalo Pay", "force-app-update", "bank-future");
                        terminate();
                    }
                });
    }

    @Override
    public void showSelectionBankAccountDialog() {
        Intent intent = new Intent(getContext(), MapListSelectionActivity.class);
        intent.putExtra(MapListSelectionActivity.BANKCODE_EXTRA, CardType.PVCB);
        startActivity(intent);
    }
}
